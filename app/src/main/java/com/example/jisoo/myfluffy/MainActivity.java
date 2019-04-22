package com.example.jisoo.myfluffy;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;

import static com.example.jisoo.myfluffy.MyValues.*;


/** TODO LIST
 * 프로필 이미지
 * 롱클릭 중복저장 경고 메세지
 * clinic 메뉴
 */
public class MainActivity extends AppCompatActivity {
    private ImageButton ibtnMainPic;
    private Button btnRecord, btnMonthly, btnClinic;
    private  Button[] btnQuick = new Button[3];
    private int[] btnQuickID = {R.id.btnFood, R.id.btnToilet, R.id.btnWalk};
    private TextView tvMainName, tvMainAge, tvMainWeight;
    private LinearLayout linearInfo, linearDaily;
    private TextView tvToday, tvNone, tvTodayMealCnt, tvTodayLastMeal, tvTodayPeeCnt, tvTodayPooCnt;

    private TextView[] tvTodayCate = new TextView[3];
    private TextView[] tvTodayTime = new TextView[3];
    private TextView[] tvTodayTitle = new TextView[3];
    private TextView[] tvTodayContent = new TextView[3];
    private LinearLayout[] linearToday = new LinearLayout[3];
    private int[] tvTodayCateID = {R.id.tvTodayCate1, R.id.tvTodayCate2, R.id.tvTodayCate3};
    private int[] tvTodayTimeID = {R.id.tvTodayTime1, R.id.tvTodayTime2, R.id.tvTodayTime3};
    private int[] tvTodayTitleID = {R.id.tvTodayTitle1, R.id.tvTodayTitle2, R.id.tvTodayTitle3};
    private int[] tvTodayContentID = {R.id.tvTodayContent1, R.id.tvTodayContent2, R.id.tvTodayContent3};
    private  int[] linearID = {R.id.linearToday1, R.id.linearToday2, R.id.linearToday3};
    private LocalDate C_DATE = LocalDate.now();
    private String C_DATE_STR = C_DATE.format(DF_DEFAULT);

    DBAdapter mDB;
    String category, title, mDateStr, mTime1Str, mTime2Str, content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ibtnMainPic = (ImageButton)findViewById(R.id.ibtnMainPic);
        tvMainName = (TextView)findViewById(R.id.tvMainName);
        tvMainAge = (TextView)findViewById(R.id.tvMainAge);
        tvMainWeight = (TextView)findViewById(R.id.tvMainWeight);

        linearInfo = (LinearLayout)findViewById(R.id.linearInfo);
        linearDaily = (LinearLayout)findViewById(R.id.linearDaily);

        tvToday = (TextView)findViewById(R.id.tvToday);
        tvNone = (TextView)findViewById(R.id.tvNone);
        tvTodayMealCnt = (TextView)findViewById(R.id.tvTodayMealCnt);
        tvTodayLastMeal = (TextView)findViewById(R.id.tvTodayLastMeal);
        tvTodayPeeCnt = (TextView)findViewById(R.id.tvTodayPeeCnt);
        tvTodayPooCnt = (TextView)findViewById(R.id.tvTodayPooCnt);

        for(int i=0; i<linearID.length; i++){
            linearToday[i] = (LinearLayout) findViewById(linearID[i]);
            tvTodayCate[i] = (TextView) findViewById(tvTodayCateID[i]);
            tvTodayTime[i] = (TextView) findViewById(tvTodayTimeID[i]);
            tvTodayTitle[i] = (TextView) findViewById(tvTodayTitleID[i]);
            tvTodayContent[i] = (TextView) findViewById(tvTodayContentID[i]);
        }
        for(int i=0; i<btnQuickID.length; i++){
            btnQuick[i] = (Button)findViewById(btnQuickID[i]);
        }

        btnRecord = (Button)findViewById(R.id.btnRecord);
        btnMonthly = (Button)findViewById(R.id.btnMonthly);
        btnClinic = (Button)findViewById(R.id.btnClinic);




        linearInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, InfoActivity.class);
                startActivity(intent);
            }
        });

        linearDaily.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LogActivity.class);
                intent.putExtra("Mode", 1);
                startActivity(intent);
            }
        });

        for(Button btn : btnQuick){
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (v.getId()){
                        case R.id.btnFood : category = "식사"; break;
                        case R.id.btnToilet: category = "배변"; break;
                        case R.id.btnWalk : category = "산책"; break;
                    }
                    Intent intent = new Intent(MainActivity.this, RecordActivity.class);
                    intent.putExtra("Mode", 1);
                    intent.putExtra("Category",category);
                    startActivity(intent);
                }
            });
            btn.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    quickSave(v);
                    return true;
                }
            });
        }

        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder dlgRecord = new AlertDialog.Builder(MainActivity.this);
                final View viewDlgCategory = (View)View.inflate(MainActivity.this, R.layout.dlg_category, null);
                dlgRecord.setView(viewDlgCategory);
                dlgRecord.setPositiveButton("선택 완료", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        RadioGridTableLayout rg = (RadioGridTableLayout) viewDlgCategory.findViewById(R.id.rg);
                        int rbID = rg.getCheckedRadioButtonId();
                        Log.v("DlgRecord", "rbID : "+ rbID);

                        if(rbID != -1){
                            RadioButton rb = (RadioButton)viewDlgCategory.findViewById(rbID);
                            category = rb.getText().toString();
                            Intent intent = new Intent(MainActivity.this, RecordActivity.class);
                            intent.putExtra("Mode", 1);
                            intent.putExtra("Category", category);
                            startActivity(intent);
                        }else{
                            Toast.makeText(MainActivity.this, "카테고리를 선택해주세요.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                dlgRecord.setNegativeButton("취소", null);
                dlgRecord.show();
            }
        });


        btnMonthly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LogActivity.class);
                intent.putExtra("Mode", 2);
                startActivity(intent);
            }
        });


        // 벙원메뉴
        btnClinic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
                //startActivity(intent);
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();

        // 기본 정보 DB에서 불러와 셋팅
        mDB = new DBAdapter(this);
        mDB.open();
        Cursor c_Info = mDB.fetchInfo();
        Cursor c_Weight = mDB.fetchWeight();
        byte[] img_o = c_Info.getBlob(0);
        if(img_o != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(img_o, 0, img_o.length);
            ibtnMainPic.setBackground((Drawable) getDrawable(R.drawable.round_bg));
            ibtnMainPic.setImageBitmap(bitmap);
        }
        LocalDate birthday = LocalDate.parse(c_Info.getString(2), DF_DEFAULT);
        tvMainName.setText(c_Info.getString(1));
        tvMainAge.setText(getAge(birthday) + ", " + getDday(birthday));
        tvMainWeight.setText(c_Weight.getFloat(1) + "kg");
        c_Info.close();
        c_Weight.close();

        // 오늘 기록 요약 불러오기
        //tvToday.setText();

        // 식사 기록
        Cursor cToday = mDB.fetchCategoryRecord("식사", C_DATE_STR, 1);
        LocalDate yesterday = C_DATE.minusDays(1);
        Cursor cYesterday = mDB.fetchCategoryRecord("식사", yesterday.format(DF_DEFAULT), 1);
        LocalDateTime lastMeal;
        Duration duration;

        if(cToday != null && cToday.getCount() > 0){ // 오늘 기록이 있을 때
            lastMeal = LocalDateTime.of(C_DATE, LocalTime.parse(cToday.getString(3), TF_DEFAULT));
            duration = Duration.between(lastMeal, LocalDateTime.now());
            tvTodayLastMeal.setText("(" + duration.toHours() + "시간 전)");

        }else{ // 오늘 기록이 없을 때 어제 기록 찾기
            if(cYesterday != null && cYesterday.getCount() > 0) {// 어제 기록 있을 때
                cYesterday.moveToFirst();
                lastMeal = LocalDateTime.of(yesterday, LocalTime.parse(cYesterday.getString(3), TF_DEFAULT));
                Log.v("mealcnt TEST", "LASTMEAL : " + cYesterday.getString(3));
                duration = Duration.between(lastMeal, LocalDateTime.now());
                tvTodayLastMeal.setText("(" + duration.toHours() + "시간 전)");

            }else // 어제 기록도 없을 때
                tvTodayLastMeal.setText("");
        }
        tvTodayMealCnt.setText(cToday.getCount() + "회" );

        // 배변 기록
        Cursor cPoo = mDB.fetchToiletRecord("대변", C_DATE_STR, 1);
        Cursor cPee = mDB.fetchToiletRecord("소변", C_DATE_STR, 1);
        tvTodayPeeCnt.setText(cPee.getCount() + "회" );
        tvTodayPooCnt.setText(cPoo.getCount() + "회" );
        cPee.close(); cPoo.close();

        // 최근 3건 기록
        cToday = mDB.fetchRecord(C_DATE_STR, 1);
        if(cToday != null && cToday.getCount() > 0){
            tvNone.setVisibility(View.INVISIBLE);
            for(LinearLayout l : linearToday) l.setVisibility(View.INVISIBLE);
            int max = (cToday.getCount() > 3)? 3: cToday.getCount();
            for(int i=0; i<max; i++){
                linearToday[i].setVisibility(View.VISIBLE);
                tvTodayCate[i].setText(cToday.getString(0));
                tvTodayTime[i].setText(cToday.getString(3));
                tvTodayTitle[i].setText(cToday.getString(1));
                tvTodayContent[i].setText(cToday.getString(5));
                cToday.moveToNext();
            }

        }else {
            tvNone.setVisibility(View.VISIBLE);
            for(LinearLayout l : linearToday) l.setVisibility(View.INVISIBLE);
        }

        cToday.close();


    }


    private void quickSave(View v) {
        switch (v.getId()){
            case R.id.btnFood : category = "식사"; break;
            case R.id.btnToilet: category = "배변"; break;
            case R.id.btnWalk : category = "산책"; break;
        }
        title = (category.equals("배변"))? "소변" : "";
        content = "";
        mDateStr = LocalDate.now().format(DF_DEFAULT);
        mTime1Str = LocalTime.now().format(TF_DEFAULT);
        mTime2Str = LocalTime.now().plusMinutes(10).format(TF_DEFAULT);

        Cursor c = mDB.fetchThisRecord(category, title, mDateStr, mTime1Str);
        Log.v("TEST", "IS ITEM EXIST? " + c.getCount());

        if(c.getCount() > 0) { // 기존 기록 있을 때
            int rowID = c.getInt(9);
            boolean isUpdated = mDB.updateRecord(rowID, category, title, mDateStr, mTime1Str, mTime2Str, content, (byte) 0, (byte) 0, (byte) 0);
            Log.v("TEST", "IS ITEM UPDATED? " + isUpdated);
            if(isUpdated) Toast.makeText(MainActivity.this, category + " 기록 수정 완료", Toast.LENGTH_SHORT).show();

        }else{ // 새로 저장하기
            mDB.createRecord(category, title, mDateStr, mTime1Str, mTime2Str, content, (byte) 0, (byte) 0, (byte) 0);
            Toast.makeText(MainActivity.this, category + " 기록 저장 완료", Toast.LENGTH_SHORT).show();
        }


        onResume();
    }


    public String getAge(LocalDate birthday){
        Period period = Period.between(birthday, C_DATE);
        return period.getYears() + "년 " + period.getMonths() + "개월";
    }

    public String getDday(LocalDate birthday){
        return "D+" + ChronoUnit.DAYS.between(birthday, C_DATE);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDB.close();
    }



}
