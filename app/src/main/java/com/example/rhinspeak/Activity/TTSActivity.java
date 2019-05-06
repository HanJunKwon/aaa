package com.example.rhinspeak.Activity;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.rhinspeak.NaverAPI.NaverSynthesis;
import com.example.rhinspeak.R;
import com.example.rhinspeak.Util.AudioWriterPCM;
import com.example.rhinspeak.Util.HttpCon;
import com.example.rhinspeak.Util.StaticUtil;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;

public class TTSActivity extends AppCompatActivity implements StaticUtil, View.OnClickListener {
    private NaverSynthesis naverSynthesis;
    private SynthesisHandler CSShandler; // 문자열을 보낸 후에 음성으로 변환된 결과를 받는 핸들러
    private AudioWriterPCM audioWriterPCM; // 네이버에서 제공하는 sdk 클래스
    private HttpCon httpCon; // CSS 연결라인을 담는 객체

    EditText edtText;
    Button btnTextToSpeech;

    String strEdtText;
    private String fileName = null; // 텍스트를 네이버 API로 전달하여 음성으로 변환된 mp3 데이터의 파일명

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tts);

        edtText = (EditText) findViewById(R.id.editText);
        btnTextToSpeech = (Button) findViewById(R.id.btnTextToSpeech);
        btnTextToSpeech.setOnClickListener(this);

        CSShandler = new SynthesisHandler(this);
        naverSynthesis = new NaverSynthesis(this, CSShandler);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btnTextToSpeech:
                strEdtText = edtText.getText().toString();
                new NaverSynthesisAsyncTask().execute();
                break;
        }
    }

    private void handleMessage(Message msg){
        switch(msg.what) {
            case R.id.clientReady: // 네이버 API 연결(준비단계)
                audioWriterPCM = new AudioWriterPCM(Environment.getExternalStorageDirectory().getAbsolutePath() + R.string.rhin_record_path);
                Toast.makeText(this, R.string.naver_api_connecte_successed, Toast.LENGTH_SHORT).show();
                break;
            case R.id.SynthesisConnect:
                Log.e("","SynthesisConnect");
                httpCon = new HttpCon(this, StaticUtil.clova_speech_synthesis_url);
                naverSynthesis.sendText();
                break;
            case R.id.SendText:
                Log.e("", "sendText");
                httpCon.connectUrl(edtText.getText().toString());
                naverSynthesis.startRecord();
                break;
        }
    }

    private class NaverSynthesisAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            try{
                String text = URLEncoder.encode(strEdtText, "UTF-8");
                URL url = new URL(StaticUtil.clova_speech_synthesis_url);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST"); // POST 방식으로 전환이 안 됨.

                con.setRequestProperty("X-NCP-APIGW-API-KEY-ID", StaticUtil.naver_ai_api_client_id);
                con.setRequestProperty("X-NCP-APIGW-API-KEY", StaticUtil.naver_ai_api_client_secret);

                String postParams = "speaker=mijin&speed=0&text="+text;
                con.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                wr.writeBytes(postParams);
                wr.flush();
                wr.close();

                int responseCode = con.getResponseCode();
                BufferedReader br;

                if(responseCode == 200){
                    // 정상호출
                    InputStream is = con.getInputStream();
                    int read = 0;

                    byte[] bytes = new byte[1024];

                    fileName = Long.valueOf(new Date().getTime()).toString();

                    // 변환된 음성파일 저장
                    File f = new File(StaticUtil.record_folder_name + fileName+".mp3");

                    // 저장할 디렉토리가 존재하지 않으면 생성함.
                    if(!StaticUtil.record_folder_name.mkdirs()){
                        StaticUtil.record_folder_name.mkdirs(); // 음성파일을 저장할 폴더를 생성함.
                        f.createNewFile(); // 생성된 폴더에 음성 파일 저장
                    }
                    else{
                        f.createNewFile(); // 음성 파일 저장
                    }

                    OutputStream outputStream = new FileOutputStream(f);
                    while((read = is.read(bytes)) != -1){
                        outputStream.write(bytes, 0, read);
                    }
                    is.close();
                }else{
                    br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();
                    while((inputLine = br.readLine()) != null){
                        response.append(inputLine);
                    }
                    br.close();
                    System.out.print(response.toString());
                }
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            } catch (ProtocolException e1) {
                e1.printStackTrace();
            } catch (MalformedURLException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            // R.raw에 파일을 저장한게 아니기 때문에 내부 Uri를 통해서 파일에 접근후에 재생해야 된다.
            /*
            R.raw, 내부 URI, 외부 URL을 통해서 파일을 재생하는 방법
            http://unikys.tistory.com/350
             */
            Uri fileUri = Uri.fromFile(new File(StaticUtil.record_folder_name+fileName+".mp3"));
            MediaPlayer mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            try {
                mediaPlayer.setDataSource(getApplicationContext(), fileUri);
                mediaPlayer.prepare();
                mediaPlayer.start();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    static class SynthesisHandler extends Handler {
        private final WeakReference<TTSActivity> mActivity;

        SynthesisHandler(TTSActivity activity) {
            mActivity = new WeakReference<TTSActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            TTSActivity activity = mActivity.get();
            if(activity != null){
                activity.handleMessage(msg);
            }
        }
    }
}
