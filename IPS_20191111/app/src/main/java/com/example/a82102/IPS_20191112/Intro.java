package com.example.a82102.IPS_20191112;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class Intro extends AppCompatActivity {

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_intro); // intro 화면 시작

            Handler handler = new Handler(); // UI 전환을 위한 메세지 전달역할
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(getApplicationContext(), Will_buy.class);
                    startActivity(intent);
                    finish();
                }
            }, 3000); //3초 뒤에 Runner객체 실행하도록 함

    }
}
