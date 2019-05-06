package com.example.rhinspeak.Activity;

import android.graphics.Color;
import android.media.Image;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rhinspeak.Model.ConsonantVowel;
import com.example.rhinspeak.Model.Exist;
import com.example.rhinspeak.Model.ImageInfo;
import com.example.rhinspeak.Model.Syllable;
import com.example.rhinspeak.NaverAPI.NaverRecognizer;
import com.example.rhinspeak.R;
import com.example.rhinspeak.Util.AudioWriterPCM;
import com.example.rhinspeak.Util.RetrofitService;
import com.example.rhinspeak.Util.SQLiteManager;
import com.example.rhinspeak.Util.StaticUtil;
import com.naver.speech.clientapi.SpeechRecognitionResult;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class STTActivity extends AppCompatActivity implements View.OnClickListener, StaticUtil {
    private final static String TAG = "STTActivity";
    private final static int IS_EXIST = 1;

    private Exist exist = new Exist();
    private static ImageInfo imageInfo = new ImageInfo();
    private RecognitionHandler handler; // 녹음한 내용올 보낸 후에 텍스트로 결과를 받는 핸들러
    private NaverRecognizer naverRecognizer;
    private AudioWriterPCM audioWriterPCM; // 네이버에서 제공하는 sdk 클래스
    private SQLiteManager sqLiteManager;
    private int imageId = 1;
    //private List<String> imageInfo;
    private ArrayList<Syllable> answerWord, patientWord; // 정답이랑 환자가 말하는 답

    private String mResult; // 음성 파일 텍스트로 분석한 결과 저장
    private Button btnRecord, btnPre, btnNext;
    private TextView tvResult, tvImageName;
    private TextView tvSyllable1, tvSyllable2, tvSyllable3, tvSyllable4, tvSyllable5;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stt);
        sqLiteManager = new SQLiteManager(this);
        sqLiteManager.getWritableDatabase();
        sqLiteManager.getReadableDatabase();

        handler = new RecognitionHandler(this);
        naverRecognizer = new NaverRecognizer(this, handler, StaticUtil.naver_ai_api_client_id);
        btnRecord = (Button) findViewById(R.id.btnRecord);
        btnRecord.setOnClickListener(this);
        btnPre = (Button) findViewById(R.id.btnPre);
        btnPre.setOnClickListener(this);
        btnNext = (Button) findViewById(R.id.btnNext);
        btnNext.setOnClickListener(this);
        tvResult = (TextView) findViewById(R.id.tvResult);
        imageView = (ImageView) findViewById(R.id.imgPicture);
        tvImageName = (TextView) findViewById(R.id.tvImageName);
        tvSyllable1 = (TextView) findViewById(R.id.tvSyllable1);
        tvSyllable2 = (TextView) findViewById(R.id.tvSyllable2);
        tvSyllable3 = (TextView) findViewById(R.id.tvSyllable3);
        tvSyllable4 = (TextView) findViewById(R.id.tvSyllable4);
        tvSyllable5 = (TextView) findViewById(R.id.tvSyllable5);

        answerWord = new ArrayList<>();
        patientWord = new ArrayList<>();

        nextImage();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnRecord:
                if (!naverRecognizer.getSpeechRecognizer().isRunning()) {
                    mResult = "";

                    tvResult.setText("Connecting...");
                    btnRecord.setText(R.string.record_stop);
                    naverRecognizer.recognize();
                } else {
                    btnRecord.setEnabled(false);
                    naverRecognizer.getSpeechRecognizer().stop();
                }
                break;

            case R.id.btnPre:
                preImage();
                break;
            case R.id.btnNext:
                nextImage();
                break;
        }
    }

    /**
     * Pre Button 로직
     */
    public void preImage() {
        Log.d(TAG, "pre Image()");

        if (imageId <= 1) {
            return;
        }

        Call<Exist> existCall = RetrofitService.getInstance().getService().isImageExist(imageId);
        existCall.enqueue(new Callback<Exist>() {
            @Override
            public void onResponse(Call<Exist> call, Response<Exist> response) {
                Log.d(TAG, "exist onResponse");
                Log.d(TAG, response.body().toString());
                getImageInfo("pre");
            }

            @Override
            public void onFailure(Call<Exist> call, Throwable t) {
                Log.d(TAG, t.getMessage());
            }
        });
        return;
    }

    /*
    Next Button 로직
     */
    public void nextImage() {
        Log.d(TAG, "next Image");

        // 이미지 존재 여부 정보 get
        Call<Exist> existCall = RetrofitService.getInstance().getService().isImageExist(imageId);
        existCall.enqueue(new Callback<Exist>() {
            @Override
            public void onResponse(Call<Exist> call, Response<Exist> response) {
                Log.d(TAG, "existCall onResponse");
                Log.d(TAG, response.body().toString());

                getImageInfo("next"); // 이미지가 존재 시 이미지를 가져오는 메소드 실행
            }

            @Override
            public void onFailure(Call<Exist> call, Throwable t) {
                Log.d(TAG, "existCall onFailure");
                Log.d(TAG, t.getMessage());
            }
        });
    }

    /**
     * 이미지가 존재할 때 서버의 이미지를 정보를 가져오는 전체적인 코드
     */
    public void getImageInfo(final String command) {
        Call<ImageInfo> imageInfoCall = RetrofitService.getInstance().getService().getImageInfo(imageId);
        imageInfoCall.enqueue(new Callback<ImageInfo>() {
            @Override
            public void onResponse(Call<ImageInfo> call, Response<ImageInfo> response) {
                Log.d(TAG, "imageInfoCall onResponse");
                Log.d(TAG, response.body().toString());
                ImageInfo imageInfo = response.body();

                // 이미지 이름 화면에 View
                tvImageName.setText(imageInfo.getImg_text());
                // 이미지 서버로부터 GET
                String path = "http://106.10.33.63/speech-pathology/img/question_img/"+imageInfo.getImg_name();
                Picasso.get()
                        .load(path)
                        .resize(240, 240)
                        .centerCrop()
                        .into(imageView);

                if(command.equals("pre")){
                    --imageId;
                } else if (command.equals("next")){
                    ++imageId;
                }
                else{
                    return;
                }
            }

            @Override
            public void onFailure(Call<ImageInfo> call, Throwable t) {
                Log.d(TAG, "onFailure");
                Log.e(TAG, t.getMessage());
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        naverRecognizer.getSpeechRecognizer().initialize();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mResult = "";
        tvResult.setText("");
        btnRecord.setText("녹음 시작");
        btnRecord.setEnabled(true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        naverRecognizer.getSpeechRecognizer().release();
    }

    static class RecognitionHandler extends Handler {
        /*
         WeakReference는 메모리가 충분하면 GC에 의해서 수거되지 않고 메모리에 여유가 없다면 GC(가비지 컬레터)에 수거된다.
         GC가 발생하면 해당 메모리 영역은 무조건 회수 되기 때문에 짧게 자주 사용하는 객체에 사용하는게 바람직하다.
        */

        /*
        안드로이드 메모리 누수가 발생하는 경우의 대부분은 Activity context에 관한 참조를 오랫동안 유지하기 때문이다.
        따라서 OOM이 발생하지 않도록 WeakRefence 객체를 사용하여 액티비티에 대한 Content를 짧게 갖게한다.
        Handler는 non-static 클래스이므로 외부 클래스 Activity 의 레퍼런스를 갖고 있기 때문에 애플리케이션이 종료되어도
        GC되지 않아 메모리 누수가 발생할 수 있다.
        자바에서 non static inner 클래스의 경우 outer class에 대한 reference를 참조하게 되면
         */
        private final WeakReference<STTActivity> mSTTActivity;

        RecognitionHandler(STTActivity activity) {
            mSTTActivity = new WeakReference<STTActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            STTActivity activity = mSTTActivity.get();
            if (activity != null) {
                activity.handleMessage(msg);
            }
        }
    }

    private void handleMessage(Message msg) {
        switch (msg.what) {
            case R.id.clientReady: // 네이버 API 연결(준비단계), 사용자의 음성을 받을 준비가 되어 있는 단계
                tvResult.setText("Connected...");
                audioWriterPCM = new AudioWriterPCM(Environment.getExternalStorageDirectory().getAbsolutePath() + R.string.rhin_record_path);
                audioWriterPCM.open("rhin_test");
                break;
            case R.id.audioRecording: // 음성 녹음 중인 상태
                audioWriterPCM.write((short[]) msg.obj);
                break;
            case R.id.partialResult: // 음성인식 서버로부터 인식 중간 결과를 받으면 호출됩니다. 중간 결과는 없거나 여러번 있을 수 있습니다.
                mResult = (String) (msg.obj);
                tvResult.setText(mResult);
                break;
            case R.id.finalResult: // 최종 인식 결과
                // 음절 단위 틀린 결과 TextView들 초기화
                for (int i = 0; i < patientWord.size(); i++) {
                    String tvName = "tvSyllable" + (i + 1);
                    int tvResourceId = getResources().getIdentifier(tvName, "id", getPackageName());
                    TextView tvTmp = (TextView) findViewById(tvResourceId);
                    tvTmp.setText("");
                    tvTmp.setTextColor(getResources().getColor(R.color.textViewDefault));
                }

                // 자음 모음 단위 틀린 결과 TextView들 초기화
                for(int i=0; i<10; ++i){
                    String tvName = "tvAnswerConsonantVowel" + (i + 1);
                    int tvResourceId = getResources().getIdentifier(tvName, "id", getPackageName());
                    TextView tvTmp = (TextView) findViewById(tvResourceId);
                    tvTmp.setText("");
                    tvTmp.setTextColor(getResources().getColor(R.color.textViewDefault));

                    tvName = "tvPatientConsonantVowel"+(i+1);
                    tvResourceId = getResources().getIdentifier(tvName, "id", getPackageName());
                    tvTmp = (TextView) findViewById(tvResourceId);
                    tvTmp.setText("");
                    tvTmp.setTextColor(getResources().getColor(R.color.textViewDefault));
                }

                SpeechRecognitionResult speechRecognitionResult = (SpeechRecognitionResult) msg.obj;
                List<String> results = speechRecognitionResult.getResults(); // 우선순위가 높은 순으로 최대 5개의 리스트를 가져온다.

                tvResult.setText(results.get(0)); // 환자 음성 분석한 결과를 textview에 보여준다.

                answerWord = wordSplit(tvImageName.getText().toString()); // 정답 문자열 분리
                patientWord = wordSplit(results.get(0)); // 환자 음성 분석 결과 문자열 분리

                // 환자 답과 정답 비교
                compareWord();
                audioWriterPCM.close();
                break;
            case R.id.recognitionError: // 인식 에러
                if (audioWriterPCM != null)
                    audioWriterPCM.close();

                mResult = "Error code" + msg.obj.toString();
                tvResult.setText(mResult);

                btnRecord.setText("녹음 시작");
                btnRecord.setEnabled(true);
                break;
            case R.id.clientInactive:
                if (audioWriterPCM != null)
                    audioWriterPCM.close();

                btnRecord.setText("녹음 시작");
                btnRecord.setEnabled(true);
                break;

            case R.id.ResultRecordStart:
                Log.e("", "ResultRecordStart");
                break;
        }
    }

    /**
     * 매개변수로 받은 문자열을 분리
     * 1차 분리: 1단어 -> 1음절
     * 2차 분리: 1음절 -> 초성/중성/종성
     */
    private ArrayList<Syllable> wordSplit(String orgWord) {
        // 매개변수로 들어온 문자열의 길이가 0 이면 null 반환
        if (orgWord.length() == 0) {
            return null;
        }

        ArrayList<Syllable> word = new ArrayList<>();

        for (int i = 0; i < orgWord.length(); i++) {
            char comVal = (char) (orgWord.charAt(i) - 0xAC00);

            if (comVal >= 0 && comVal <= 11172) {
                // 한글일 경우

                // 초성만 입력했을 시엔 초성은 무시해서 List에 추가함
                char uniVal = (char) comVal;

                // 유니코드 표에 맞춰 초성 중성 종성을 분리
                char cho = (char) ((((uniVal - (uniVal % 28)) / 28) / 21) + 0x1100);
                char jung = (char) ((((uniVal - (uniVal % 28)) / 28) % 21) + 0x1161);
                char jong = (char) ((uniVal % 28) + 0x11a7);

                // 음절 객체 생성
                Syllable syllable = new Syllable();
                syllable.setCho(cho);
                syllable.setJung(jung);
                syllable.setJong(jong);

                ArrayList<ConsonantVowel> consonantVowels = new ArrayList<ConsonantVowel>();
                ConsonantVowel choObj = new ConsonantVowel();
                choObj.setCharacter(cho);
                consonantVowels.add(choObj); // 초성 추가
                ConsonantVowel jungObj = new ConsonantVowel();
                jungObj.setCharacter(jung);
                consonantVowels.add(jungObj); // 중성 추가
                ConsonantVowel jongObj = new ConsonantVowel();
                jongObj.setCharacter(jong);
                consonantVowels.add(jongObj); // 종성 추가
                syllable.setConsonant_vowel(consonantVowels); // 초성 중성 종성 리스트 음절 객체에 추가

                // 초성 중성 종성
                word.add(syllable);

                if (cho != 4519) {
                    Log.d(TAG, "초성:" + cho);
                }
                if (jung != 4519) {
                    Log.d(TAG, "중성:" + jung);
                }
                if (jong != 4519) {
                    Log.d(TAG, "초성:" + jong);
                }
            } else {
                // 한글이 아닐 경우
                comVal = (char) (comVal + 0xAC00);
                Log.d(TAG, "영문:" + comVal);
            }
        }
        return word;
    }

    // 정답과 한자 답 글자 비교
    private void compareWord() {
        /**
         * 정답과 환자답의 글자 비교
         */

        int j = 0;
        for (int i = 0; i < answerWord.size(); i++) {

            ArrayList<ConsonantVowel> patientConsonantVowels = patientWord.get(i + j).getConsonant_vowel(); // i 번째 음절의 초성/중성/종성 객체 리스트를 가져옴
            ArrayList<ConsonantVowel> answerConsonantVowels = answerWord.get(i).getConsonant_vowel(); // i 번째 음절의 초성/중성/종성 객체 리스트를 가져옴

            // i번째 음절의 초성이 같은지 여부 확인
            if (patientConsonantVowels.get(0).getCharacter() == answerConsonantVowels.get(0).getCharacter()) {
                patientConsonantVowels.get(0).setCurrent(true);
            } else {
                patientConsonantVowels.get(0).setCurrent(false);
                patientWord.get(i + j).setIs_same(false);
            }

            // i번째 음절의 중성이 같은지 여부 확인
            if (patientConsonantVowels.get(1).getCharacter() == answerConsonantVowels.get(1).getCharacter()) {
                patientConsonantVowels.get(1).setCurrent(true);
            } else {
                patientConsonantVowels.get(1).setCurrent(false);
                patientWord.get(i + j).setIs_same(false);
            }


            if (patientWord.get(i + j).getIs_same()) {
                if (patientConsonantVowels.get(2).getCharacter() == answerConsonantVowels.get(2).getCharacter()) {
                    patientConsonantVowels.get(2).setCurrent(true);
                } else {
                    // i+j+1 번째 음절 객체를 가져옴
                    Syllable nextSyllable = patientWord.get(i + j + 1);
                    ArrayList<ConsonantVowel> nextSyllableElements = nextSyllable.getConsonant_vowel();

                    if (nextSyllableElements.get(0).getCharacter() == patientConsonantVowels.get(2).getCharacter()) { // i 번쨰 음절의 종성과 i+1번째 음절의 초성이 같은지 비교
                        // 같으면 하나의 음절을 발음하려고 했던 의도라고 판단
                        patientConsonantVowels.get(2).setCurrent(false);

                        // 다음 음절도 전체다 틀린거로 판단
                        nextSyllable.setIs_same(false);
                        // 다음 음절의 초성/중성/종성도 모두 틀린것으로 판단
                        nextSyllableElements.get(0).setCurrent(false);
                        nextSyllableElements.get(1).setCurrent(false);
                        nextSyllableElements.get(2).setCurrent(false);

                        ++j;
                    }
                }
            } else {
                if (patientConsonantVowels.get(2).getCharacter() == answerConsonantVowels.get(2).getCharacter())
                    patientConsonantVowels.get(2).setCurrent(true);
                else
                    patientConsonantVowels.get(2).setCurrent(false);
            }
        }

        // 네이버에서 받은 결과를 출력
        String strPatientWord = tvResult.getText().toString();
        for (int i = 0; i < strPatientWord.length(); i++) {
            patientWord.get(i).setSyllable(Character.toString(strPatientWord.charAt(i)));
        }

        // 틀린 부분을 표시 (음절 단위)
        for (int i = 0; i < patientWord.size(); i++) {
            String tvName = "tvSyllable" + (i + 1);
            int tvResourceId = getResources().getIdentifier(tvName, "id", getPackageName());
            TextView tvTmp = (TextView) findViewById(tvResourceId);
            tvTmp.setText(patientWord.get(i).getSyllable());

            if (patientWord.get(i).getIs_same() == false)
                tvTmp.setTextColor(Color.RED);
        }


        // 틀린 부분 자음 모음까지 상세하게 TextView로 표시
        int count = 1;
        if (patientWord.size() > answerWord.size()) {
            for (int i = 0; i < answerWord.size(); ++i) {
                // 환자가 입력한 답이 정답보다 긴 단어인 경우
                // index Error
                ArrayList<ConsonantVowel> patientCon_vo = patientWord.get(i).getConsonant_vowel();
                ArrayList<ConsonantVowel> answerCon_vo = answerWord.get(i).getConsonant_vowel();

                for (int k = 0; k < 3; ++k) {
                    String tvName = "";
                    TextView tvTmp;

                    if (k == 2 &&
                            (patientCon_vo.get(2).getCharacter() == 4519 &&
                                    answerCon_vo.get(2).getCharacter() == 4519)) {
                    } else {
                        tvName = "tvAnswerConsonantVowel" + count;
                        int tvResourceId = getResources().getIdentifier(tvName, "id", getPackageName());
                        tvTmp = (TextView) findViewById(tvResourceId);
                        tvTmp.setText(String.valueOf(answerCon_vo.get(k).getCharacter()));
                        if (answerCon_vo.get(k).getCharacter() == 4519)
                            tvTmp.setText("");

                        tvName = "tvPatientConsonantVowel" + count;
                        tvResourceId = getResources().getIdentifier(tvName, "id", getPackageName());
                        tvTmp = (TextView) findViewById(tvResourceId);
                        tvTmp.setText(String.valueOf(patientCon_vo.get(k).getCharacter()));
                        if (patientCon_vo.get(k).getCharacter() == 4519)
                            tvTmp.setText("");
                        if (!patientCon_vo.get(k).isCurrent())
                            tvTmp.setTextColor(Color.RED);
                        count++;
                    }
                }
            }

            // answerWord의 단어수가 더 짧기 때문에 patientWord에서 출력되지 못한 단어가 생긴다
            // 해당 단어는 for문을 사용하여 더 출력
            for (int i = answerWord.size(); i < patientWord.size(); ++i) {
                ArrayList<ConsonantVowel> patientCon_vo = patientWord.get(i).getConsonant_vowel();
                for (int k = 0; k < 3; k++) {
                    if (k == 2 &&
                            (patientCon_vo.get(2).getCharacter() == 4519)) {
                    } else {
                        String tvName = "tvPatientConsonantVowel" + count;
                        int tvResourceId = getResources().getIdentifier(tvName, "id", getPackageName());
                        TextView tvTmp = (TextView) findViewById(tvResourceId);
                        tvTmp.setText(String.valueOf(patientCon_vo.get(k).getCharacter()));
                        count++;
                    }
                }
            }
        } else if (patientWord.size() < answerWord.size()) {
            for (int i = 0; i < patientWord.size(); ++i) {
                // answerWord의 글자 수가 더 적기 때문에 patientWord 글자 수만큼 for문을 돌면서 비교하면 index에러
                ArrayList<ConsonantVowel> patientCon_vo = patientWord.get(i).getConsonant_vowel();
                ArrayList<ConsonantVowel> answerCon_vo = answerWord.get(i).getConsonant_vowel();

                for (int k = 0; k < 3; ++k) {
                    String tvName = "";
                    TextView tvTmp;

                    if (k == 2 &&
                            (patientCon_vo.get(2).getCharacter() == 4519 &&
                                    answerCon_vo.get(2).getCharacter() == 4519)) {
                    } else {
                        tvName = "tvAnswerConsonantVowel" + count;
                        int tvResourceId = getResources().getIdentifier(tvName, "id", getPackageName());
                        tvTmp = (TextView) findViewById(tvResourceId);
                        tvTmp.setText(String.valueOf(answerCon_vo.get(k).getCharacter()));
                        if (answerCon_vo.get(k).getCharacter() == 4519)
                            tvTmp.setText("");

                        tvName = "tvPatientConsonantVowel" + count;
                        tvResourceId = getResources().getIdentifier(tvName, "id", getPackageName());
                        tvTmp = (TextView) findViewById(tvResourceId);
                        tvTmp.setText(String.valueOf(patientCon_vo.get(i).getCharacter()));
                        if (patientCon_vo.get(k).getCharacter() == 4519)
                            tvTmp.setText("");
                        if (!patientCon_vo.get(k).isCurrent())
                            tvTmp.setTextColor(Color.RED);
                        count++;
                    }
                }
            }

            // answerWord의 단어수가 더 짧기 때문에 patientWord에서 출력되지 못한 단어가 생긴다
            // 해당 단어는 for문을 사용하여 더 출력
            for (int i = patientWord.size(); i < answerWord.size() - patientWord.size(); ++i) {
                ArrayList<ConsonantVowel> answerCon_vo = answerWord.get(i).getConsonant_vowel();
                for (int k = 0; k < 3; k++) {
                    if (k == 2 &&
                            (answerCon_vo.get(2).getCharacter() == 4519)) {
                    } else {
                        String tvName = "tvAnswerConsonantVowel" + (i + 1);
                        int tvResourceId = getResources().getIdentifier(tvName, "id", getPackageName());
                        TextView tvTmp = (TextView) findViewById(tvResourceId);
                        tvTmp.setText(String.valueOf(answerCon_vo.get(k).getCharacter()));
                        count++;
                    }
                }
            }
        } else {
            // 두개의 단어수가 동일할 때
            for (int i = 0; i < answerWord.size(); ++i) {
                ArrayList<ConsonantVowel> patientCon_vo = patientWord.get(i).getConsonant_vowel();
                ArrayList<ConsonantVowel> answerCon_vo = answerWord.get(i).getConsonant_vowel();

                for (int k = 0; k < 3; k++) {
                    String tvName = "";
                    TextView tvTmp;
                    if (k == 2 &&
                            (patientCon_vo.get(2).getCharacter() == 4519 &&
                                    answerCon_vo.get(2).getCharacter() == 4519)) {
                    } else {
                        tvName = "tvAnswerConsonantVowel" + count;
                        int tvResourceId = getResources().getIdentifier(tvName, "id", getPackageName());
                        tvTmp = (TextView) findViewById(tvResourceId);
                        tvTmp.setText(String.valueOf(answerCon_vo.get(k).getCharacter()));
                        if (answerCon_vo.get(k).getCharacter() == 4519)
                            tvTmp.setText("");

                        tvName = "tvPatientConsonantVowel" + count;
                        tvResourceId = getResources().getIdentifier(tvName, "id", getPackageName());
                        tvTmp = (TextView) findViewById(tvResourceId);
                        tvTmp.setText(String.valueOf(patientCon_vo.get(k).getCharacter()));
                        if (patientCon_vo.get(k).getCharacter() == 4519)
                            tvTmp.setText("");
                        if (!patientCon_vo.get(k).isCurrent())
                            tvTmp.setTextColor(Color.RED);
                        count++;
                    }
                }
            }
        }
    }
}