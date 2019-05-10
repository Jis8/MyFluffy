package com.example.jisoo.myfluffy;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

import static com.example.jisoo.myfluffy.MyValues.*;


/** TODO LIST
 * 사진 경로 DB 저장 (미리보기 테두리 둥글게)
 * 중복 기록 알림 대화상자
 */
public class RecordActivity extends AppCompatActivity {

    Button btnCategory, btnSave;
    ImageButton ibtnPic[] = new ImageButton[3];
    int[] ibtnPicID = {R.id.ibtnPic1, R.id.ibtnPic2, R.id.ibtnPic3};
    LinearLayout linearDateTime, linearIB;
    RadioGroup rgToilet;
    AutoCompleteTextView edtTitle;
    ArrayList<String> titleList = new ArrayList<>();
    EditText edtContent;
    TextView tvToolbar, tvDate, tvTime1, tvTime2;
    String date, time1, time2;
    String category, title, content;
    String title_o, date_o, time1_o, time2_o, content_o;
    int rowID, rowID_o;
    private LocalDate C_DATE = LocalDate.now();
    private LocalTime C_TIME = LocalTime.now();
    Drawable round_bg;
    LocalDate mDate;
    LocalTime mTime, mTime1, mTime2;
    String mDateStr, mTime1Str, mTime2Str;
    int clickedID;
    int mode;
    DBAdapter mDB;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        // Permission Check
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
// Get the ActionBar here to configure the way it behaves.
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true); //커스터마이징 하기 위해 필요
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true); // 뒤로가기 버튼, 디폴트로 true만 해도 백버튼이 생김
//        actionBar.setHomeAsUpIndicator(R.drawable.button_back); //뒤로가기 버튼을 본인이 만든 아이콘으로 하기 위해 필요

        tvToolbar = (TextView) findViewById(R.id.tvToolbar);
//        btnCategory = (Button)findViewById(R.id.btnCategory);
        rgToilet = (RadioGroup)findViewById(R.id.rgToilet);
        btnSave = (Button)findViewById(R.id.btnSave);
        linearDateTime = (LinearLayout)findViewById(R.id.linearDateTime);
        edtTitle = (AutoCompleteTextView) findViewById(R.id.edtTitle);
        edtContent = (EditText)findViewById(R.id.edtContent);
        tvDate = (TextView)findViewById(R.id.tvDate);
        tvTime1 = (TextView)findViewById(R.id.tvTime);
        tvTime2 = (TextView)findViewById(R.id.tvTimeEnd);
        //ibtnPic[0] = (ImageButton)findViewById(ibtnPicID[0]);
        round_bg = (Drawable) getDrawable(R.drawable.round_bg);
        for(int i=0; i<ibtnPicID.length; i++){
            ibtnPic[i] = (ImageButton)findViewById(ibtnPicID[i]);

            ibtnPic[i].setClipToOutline(true);
        }
        /*
        int btnSize=ibtnPic[0].getLayoutParams().width;
        ibtnPic[0].setLayoutParams(new LinearLayout.LayoutParams(btnSize, btnSize));
        ibtnPic[1].setLayoutParams(new LinearLayout.LayoutParams(btnSize, btnSize));
        ibtnPic[2].setLayoutParams(new LinearLayout.LayoutParams(btnSize, btnSize));
*/

        mDB = new DBAdapter(this);
        mDB.open();

// 시간
        setTime(C_DATE, C_TIME, C_TIME); // 현재시간

// intent와 category 받기
        Intent intent = getIntent();
        if(intent != null){ // Main에서 들어올 때
            mode = intent.getIntExtra("Mode",1); // 1 새로저장(main) 2 수정(daily)
            category = intent.getStringExtra("Category");
            tvToolbar.setText(category + " 기록");

            if(category.equals("배변")) {
                edtTitle.setVisibility(View.INVISIBLE);
                rgToilet.setVisibility(View.VISIBLE);
            }

            if(mode == 2){ // 수정할 때 Log_Daily Dialog에서 들어옴
                tvToolbar.setText(category + " 기록 수정");
                btnSave.setText("수정");
                date_o = intent.getStringExtra("Date");
                title_o = intent.getStringExtra("Title");
                time1_o = intent.getStringExtra("Time1");
                Log.v("getIntent TEST", "Date : " + date_o + ", Title : " + title_o + ", Time1 : " + time1_o);

                Cursor cursor_o = mDB.fetchThisRecord(category, title_o, date_o, time1_o);

                mDate = LocalDate.parse(cursor_o.getString(2), DF_DEFAULT);
                mTime1 = LocalTime.parse(cursor_o.getString(3), TF_DEFAULT);
                mTime2 = LocalTime.parse(cursor_o.getString(4), TF_DEFAULT);
                setTime(mDate, mTime1, mTime2);

                RadioButton rb;
                title_o = cursor_o.getString(1);
                content_o = cursor_o.getString(5);

                if(category.equals(TOILET)){
                    rb = (title_o.equals("소변"))? (RadioButton) (rgToilet.getChildAt(0)) : (RadioButton) (rgToilet.getChildAt(1));
                    rb.setChecked(true);
                }else{
                    edtTitle.setText(title_o);
                }
                edtContent.setText(content_o);

                rowID_o = cursor_o.getInt(9);
                cursor_o.close();
            }
        }


        // 제목 자동완성
        Cursor c_title = mDB.fetchTitles(category);
        if(c_title != null && c_title.getCount() > 0) {
            while (!c_title.isAfterLast()) {
                titleList.add(c_title.getString(0));
                c_title.moveToNext();
            }
            edtTitle.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, titleList));
            c_title.close();
        }

        tvDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dp = new DatePickerDialog(RecordActivity.this, dpListener, C_DATE.getYear(), C_DATE.getMonthValue()-1, C_DATE.getDayOfMonth());
                dp.show();
            }
        });

        tvTime1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickedID = 1;
                TimePickerDialog tp = new TimePickerDialog(RecordActivity.this, tpListener, mTime1.getHour(), mTime1.getMinute(), false);
                tp.show();
            }
        });
        tvTime2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickedID = 2;
                TimePickerDialog tp = new TimePickerDialog(RecordActivity.this, tpListener, mTime2.getHour(), mTime2.getMinute(), false);
                tp.show();

            }
        });


// 이미지 버튼 클릭 - 갤러리 호출
        for(int i=0; i<ibtnPic.length; i++) {
            final int index = i;
            ibtnPic[index].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent galleryIntent = new Intent();
                    //galleryIntent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                    galleryIntent.setType("image/*");
                    galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(galleryIntent, index);
                }
            });

            ibtnPic[index].setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    /** TODO : 삭제 DIALOG 띄우기 */

                    return false;
                }
            });
        }


// 저장
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Title
                if(category.equals(TOILET))
                    title = ((RadioButton)findViewById(rgToilet.getCheckedRadioButtonId())).getText().toString();
                else
                    title = (edtTitle.getText().toString().length() == 0)? " " : edtTitle.getText().toString();
                // DateTime
                mDateStr = mDate.format(DF_DEFAULT);
                mTime1Str = tvTime1.getText().toString();
                mTime2Str = tvTime2.getText().toString();
                // Content
                content = (edtContent.getText().length() == 0)? " " : edtContent.getText().toString();

                if(mode == 2){ // 기록 수정으로 들어왔을 때
                    boolean isUpdated = mDB.updateRecord(rowID_o, category, title, mDateStr, mTime1Str, mTime2Str, content, (byte) 0, (byte) 0, (byte) 0);
                    Log.v("TEST", "IS ITEM UPDATED? " + isUpdated);
                    Toast.makeText(RecordActivity.this, "수정 완료", Toast.LENGTH_SHORT).show();

                }else{
                    Cursor c = mDB.fetchThisRecord(category, title, mDateStr, mTime1Str);
                    Log.v("TEST", "IS ITEM EXIST? " + c.getCount());

                    if(c.getCount() > 0) { // 기존 기록 있을 때
                        rowID = c.getInt(9);
                        boolean isUpdated = mDB.updateRecord(rowID, category, title, mDateStr, mTime1Str, mTime2Str, content, (byte) 0, (byte) 0, (byte) 0);
                        Log.v("TEST", "IS ITEM UPDATED? " + isUpdated);
                        if(isUpdated) Toast.makeText(RecordActivity.this, "수정 완료", Toast.LENGTH_SHORT).show();

                    }else{ // 새로 저장하기
                        mDB.createRecord(category, title, mDateStr, mTime1Str, mTime2Str, content, (byte) 0, (byte) 0, (byte) 0);
                        Toast.makeText(RecordActivity.this, "저장 완료", Toast.LENGTH_SHORT).show();
                    }
                    c.close();
                }
                mDB.close();
                finish();
            }
        });
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:{
                finish();
                //((MainActivity)MainActivity.CONTEXT).onResume();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }
    private void setTime(LocalDate Date, LocalTime Time, LocalTime TimeEnd){
        mDate = Date;
        mTime = Time;
        tvDate.setText(mDate.format(DF_FULL));
        tvTime1.setText(mTime.format(TF_DEFAULT));
        mTime1 = LocalTime.of(mTime.getHour(), mTime.getMinute());
        if(mTime == TimeEnd)
            mTime2 = mTime.plusMinutes(10);
        else
            mTime2 = TimeEnd;
        tvTime2.setText(mTime2.format(TF_DEFAULT));
    }

    // 날짜 선택
    private DatePickerDialog.OnDateSetListener dpListener = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            mDate = LocalDate.of(year, monthOfYear+1, dayOfMonth);
            tvDate.setText(mDate.format(DF_FULL));
        }
    };

    // 시간 선택
    private TimePickerDialog.OnTimeSetListener tpListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            if(clickedID == 1){ // 시작시간
                mTime1 = LocalTime.of(hourOfDay, minute);
                mTime2 = mTime1.plusMinutes(10); // 시작시간 10분 후로 종료시간 지정
                tvTime1.setText(mTime1.format(TF_DEFAULT));
                tvTime2.setText(mTime2.format(TF_DEFAULT));
            }else if(clickedID == 2){ // 종료시간
                mTime2 = LocalTime.of(hourOfDay, minute);
                tvTime2.setText(mTime2.format(TF_DEFAULT));
            }
        }
    };



    // 갤러리에서 선택한 사진 이미지버튼에 입히기
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK) {
            try {
                //Log.v("onActivityResult","index : "+index);
                InputStream in = getContentResolver().openInputStream(data.getData());
                Bitmap img = BitmapFactory.decodeStream(in);
                in.close();


                ibtnPic[requestCode].setBackground(round_bg);
                ibtnPic[requestCode].setImageBitmap(img);

            } catch (IOException e) {
            }
        }
    }


}
