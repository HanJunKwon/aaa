package com.example.rhinspeak.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.rhinspeak.R;
import com.example.rhinspeak.Util.StaticUtil;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, StaticUtil {
    Button btnTTS, btnSTT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSTT = (Button) findViewById(R.id.btnSTT);
        btnSTT.setOnClickListener(this);
        btnTTS = (Button) findViewById(R.id.btnTTS);
        btnTTS.setOnClickListener(this);

        // 권한 체크
        checkPermission();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnSTT:
                // Speach To Text Activity 실행
                Intent STTintent = new Intent(MainActivity.this, STTActivity.class);
                startActivity(STTintent);
                break;
            case R.id.btnTTS:
                // Text To Speach Activity 실행
                Intent TTSintent = new Intent(MainActivity.this, TTSActivity.class);
                startActivity(TTSintent);
                break;
            default:
                break;
        }
    }

    private void checkPermission(){
        int writePermissionResult = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readPermissionResult = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int audioPermissionResult = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);

        if(writePermissionResult == PackageManager.PERMISSION_DENIED || readPermissionResult == PackageManager.PERMISSION_DENIED || audioPermissionResult == PackageManager.PERMISSION_DENIED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO}, StaticUtil.write_permission);
        }
    }
}
