package com.example.jisoo.myfluffy;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.time.LocalDate;
import java.util.ArrayList;

import static com.example.jisoo.myfluffy.MyValues.*;

/**
 * TODO LIST
 * 전체선택 버튼
 * ViewPager
 */
public class LogActivity extends AppCompatActivity implements LogMonthlyFragment.OnDaySetListener {
    private final static int DAILY = 1;
    private final static int MONTHLY = 2;
    private final static String TAG_DAILY = "Daily";
    private final static String TAG_MONTHLY = "Monthly";
    private LocalDate C_DATE = LocalDate.now();
    // toolbar widgets
    private Button tbBtnPrev, tbBtnNext, tbBtnNow, tbBtnSwitch;
    private TextView tbTvDate, tbTvYear;

    private LinearLayout tbLlDate, linearCategory;
    private ArrayList<String> selectedCategory;

    private DBAdapter mDB;
    static LocalDate mDate;
    Fragment frag;
    int mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        Toolbar toolbar = (Toolbar) findViewById(R.id.tb);
        setSupportActionBar(toolbar);
// Get the ActionBar here to configure the way it behaves.
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true); //커스터마이징 하기 위해 필요
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true); // 뒤로가기 버튼, 디폴트로 true만 해도 백버튼이 생김
//        actionBar.setHomeAsUpIndicator(R.drawable.button_back); //뒤로가기 버튼을 본인이 만든 아이콘으로 하기 위해 필요

        tbBtnPrev = (Button) findViewById(R.id.tbBtnPrev);
        tbBtnNext = (Button) findViewById(R.id.tbBtnNext);
        tbBtnNow = (Button) findViewById(R.id.tbBtnNow);
        tbBtnSwitch = (Button) findViewById(R.id.tbBtnSwitch);
        tbTvDate = (TextView) findViewById(R.id.tbTvDate);
        tbTvYear = (TextView) findViewById(R.id.tbTvYear);
        tbLlDate = (LinearLayout) findViewById(R.id.tbLlDate);
        linearCategory = (LinearLayout) findViewById(R.id.linearCategory);
        selectedCategory = new ArrayList<String>();

        mDB = new DBAdapter(this);
        mDB.open();
        mDate = C_DATE; // 기준(오늘) Date 설정

        // daily로 들어오는지 monthly로 들어오는지 받기
        Intent intent = getIntent();
        mode = intent.getIntExtra("Mode", DAILY);
        Log.v("Test_Log","intent mode : "+ mode);



        tbLlDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createDialogWithoutDateField().show();
            }
        });

        /**
         * 처음에는 전체선택 되어있게 cbAll.setChecked(true); cateCnt = 9;
         */
        // 전체 선택
        for (int i = 0; i < linearCategory.getChildCount(); i++) {
            final CheckBox cb = (CheckBox) linearCategory.getChildAt(i);
            cb.setChecked(true);
            selectedCategory.add(cb.getText().toString());
//            Log.v("Test","category : "+ selectedCategory.get(i));
        }

        // 들어온 mode에 따라 frag 부착
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        if(mode == DAILY){
            frag = LogDailyFragment.newInstance();
            fragmentTransaction.replace(R.id.frag_container, frag, "Daily");
            tbBtnSwitch.setText("달력");
        } else if(mode == MONTHLY){
            frag = LogMonthlyFragment.newInstance();
            fragmentTransaction.replace(R.id.frag_container, frag, "Monthly");
            tbBtnSwitch.setText("목록");

        }
        fragmentTransaction.commit();

        // 날짜 설정
        setDate(mDate);

        // 날짜 눌렀을 때 datePicker
        tbTvDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DatePickerDialog dp = new DatePickerDialog(LogActivity.this, dpListener, mDate.getYear(), mDate.getMonthValue() - 1, mDate.getDayOfMonth());
                dp.show();
                /*
                if(mode == DAILY) {
                    DatePickerDialog dp = new DatePickerDialog(LogActivity.this, dpListener, mDate.getYear(), mDate.getMonthValue() - 1, mDate.getDayOfMonth());
                    dp.show();
                    // monthly에서는 monthpicker 구현해야함
                }else {

                   *//* final AlertDialog.Builder myPicker = new AlertDialog.Builder(LogActivity.this);
                    final View viewDlgCategory = (View) View.inflate(LogActivity.this, R.layout.dlg_mypicker, null);
                    myPicker.setView(viewDlgCategory);
                    myPicker.setPositiveButton("선택 완료", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            TextView tvYear = (TextView) viewDlgCategory.findViewById(R.id.dlg_tvYear);
                            RadioGridTableLayout rg = (RadioGridTableLayout) viewDlgCategory.findViewById(R.id.dlg_rgMonths);
                            int rbID = rg.getCheckedRadioButtonId();
                            Log.v("DlgRecord", "rbID : " + rbID);

                            if (rbID != -1) {
                                RadioButton rb = (RadioButton) viewDlgCategory.findViewById(rbID);
                                int selectedMonth = Integer.parseInt(rb.getText().toString());
                                mDate = LocalDate.of(selectedYear, selectedMonth, mDate.getDayOfMonth());

                            } else {
                                Toast.makeText(LogActivity.this, "월을 선택해주세요.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    myPicker.setNegativeButton("취소", null);
                    myPicker.show();*//*
                }*/
            }
        });

        //이전 눌렀을 때
        tbBtnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mode == DAILY){
                    mDate = mDate.minusDays(1);
                } else if(mode == MONTHLY){
                    mDate = mDate.minusMonths(1);
                }
                setDate(mDate);
                refresh(frag);
            }
        });

        tbBtnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mode == DAILY){
                    mDate = mDate.plusDays(1);
                } else if(mode == MONTHLY){
                    mDate = mDate.plusMonths(1);
                }
                setDate(mDate);
                refresh(frag);
            }
        });

        // 오늘 눌렀을 때 오늘로 이동
        tbBtnNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDate = C_DATE;
                setDate(mDate);
                // 불필요한 새로고침 줄이게 현재 날짜랑 다를때만 바뀌게 하고싶음 (날짜/월 비교)
                refresh(frag);
            }
        });

        // 일간 - 월간 이동 버튼
        tbBtnSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mode == DAILY){
                    mode = MONTHLY;
                    tbBtnSwitch.setText("목록");
                    frag = LogMonthlyFragment.newInstance();
                } else if(mode == MONTHLY){
                    mode = DAILY;
                    tbBtnSwitch.setText("달력");
                    frag = LogDailyFragment.newInstance();
                }
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.frag_container, frag).commit();
                setDate(mDate);
                Log.v("Test_Log","tbBtnSwitch mode : "+ mode + ", frag : "+ frag);

            }
        });







        // 카테고리 선택
        for (int i = 0; i < linearCategory.getChildCount(); i++) {
            final CheckBox cb = (CheckBox) linearCategory.getChildAt(i);

            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        selectedCategory.add(buttonView.getText().toString());
                        Log.v("Test_Log checkbox Test", "isChecked : " + isChecked );
                    } else {
                        selectedCategory.remove(buttonView.getText().toString());
                        Log.v("Test_Log checkbox Test", "isChecked : " + isChecked );
                    }
                    refresh(frag);
                    Log.v("Test_Log","Log refresh(frag) : " + frag);

                }
            });
        }










    }


    public LocalDate getDate(){
        return mDate;
    }
    public ArrayList getCategory(){
        if(mode == DAILY){

        }else if(mode == MONTHLY){

        }
        return selectedCategory;
    }
    // 날짜 선택
    private DatePickerDialog.OnDateSetListener dpListener = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            mDate = LocalDate.of(year, monthOfYear+1, dayOfMonth);

            setDate(mDate);
            Log.v("Test_Log","DatePickerDialog : "+ mDate.format(DF_DEFAULT));
            refresh(frag);

        }
    };



    // fragment refresh
    private void refresh(Fragment frag) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.detach(frag).attach(frag).commit(); // 바꿀 frag 넣어야함
    }


    // toolbar 날짜 설정
    private void setDate(LocalDate mDate) {
        tbTvYear.setText(String.valueOf(mDate.getYear()));
        String date = "";

        if(mode == DAILY){
            date = mDate.format(DF_DAILY);
        }else if(mode == MONTHLY){
            date = mDate.format(DF_MONTHLY);
        }

        tbTvDate.setText(date);
    }

    // toolbar back button
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:{
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }


    // from monthly - mode change, day set
    @Override
    public void onDaySet(int mode, LocalDate date) {
        mDate = date;
//        this.mode = mode;
        tbBtnSwitch.callOnClick();

        setDate(mDate);
        Log.v("Test_Log", "onDaySet mDate : " + date);

//        frag = getSupportFragmentManager().findFragmentById(R.id.frag_container);
//        frag = LogDailyFragment.newInstance();
        Log.v("Test_Log", "onDaySet frag : " + frag);

    }


    private DatePickerDialog createDialogWithoutDateField() {
        DatePickerDialog dpd = new DatePickerDialog(this, null, 2014, 1, 24);
        try {
            java.lang.reflect.Field[] datePickerDialogFields = dpd.getClass().getDeclaredFields();
            for (java.lang.reflect.Field datePickerDialogField : datePickerDialogFields) {
                if (datePickerDialogField.getName().equals("mDatePicker")) {
                    datePickerDialogField.setAccessible(true);
                    DatePicker datePicker = (DatePicker) datePickerDialogField.get(dpd);
                    java.lang.reflect.Field[] datePickerFields = datePickerDialogField.getType().getDeclaredFields();
                    for (java.lang.reflect.Field datePickerField : datePickerFields) {
                        Log.i("test", datePickerField.getName());
                        if ("mDaySpinner".equals(datePickerField.getName())) {
                            datePickerField.setAccessible(true);
                            Object dayPicker = datePickerField.get(datePicker);
                            ((View) dayPicker).setVisibility(View.GONE);
                        }
                    }
                }
            }
        }
        catch (Exception ex) {
        }
        return dpd;
    }




}