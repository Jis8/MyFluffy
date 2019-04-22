package com.example.jisoo.myfluffy;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SplashActivity extends AppCompatActivity {

    DBAdapter mDB; // DB, 테이블 생성할 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {

        }

        mDB = new DBAdapter(this);
        Intent intent;
        mDB.open();

        // 등록 정보 있는 지 확인
        Cursor cursor = mDB.fetchInfo();
        if(cursor != null && cursor.getCount() > 0){ // 있으면 Main 이동
            intent = new Intent(this, MainActivity.class);
        }else{ // 없으면 SignUp 이동
            intent = new Intent(this, SignUpActivity.class);
            intent.putExtra("Mode", 1);
        }
        cursor.close();
        mDB.close();
        startActivity(intent);
        finish();
    }




}
