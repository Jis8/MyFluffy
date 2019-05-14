package com.example.jisoo.myfluffy;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.time.LocalDate;
import java.util.ArrayList;

import static com.example.jisoo.myfluffy.MyValues.*;


/**
 * TODO LIST
 * 그래프 MAX, MIN 지정 (SORT, 새로 입력된 값 기존 최대/최소랑 비교 > 범위 내에 없을 때에만 새로 값 넣기)
 * 기본 정보 항목 정하기
 * 리스트 최대 개수 정하고 클릭 시 다른 액티비티에서 리스트 보여주기
 */
public class InfoActivity extends AppCompatActivity {

    private ImageView ivInfoPic;
    private TextView tvInfoName, tvInfoAge, tvInfoGender, tvNowWeight;
    private Button btnEditInfo, btnInputWeight;

    private ArrayList<ListViewItem_weight> listViewItemList;
    private ListViewAdapter listViewAdapter;

    private LineChart lineChart;
    private ArrayList<Entry> entries;
    private LineDataSet lineDataSet;
    private LineData lineData;


    private float yMax, yMin;
    DBAdapter mDBHelper;
    int row;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        ivInfoPic = (ImageView) findViewById(R.id.ivInfoPic);
        tvInfoName = (TextView) findViewById(R.id.tvInfoName);
        tvInfoAge = (TextView) findViewById(R.id.tvInfoAge);
        tvInfoGender = (TextView) findViewById(R.id.tvInfoGender);
        tvNowWeight = (TextView) findViewById(R.id.tvNowWeight);
        btnEditInfo = (Button) findViewById(R.id.btnEditInfo);
        btnInputWeight = (Button) findViewById(R.id.btnInputWeight);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true); // 뒤로가기 버튼
//        actionBar.setHomeAsUpIndicator(R.drawable.button_back);


        mDBHelper = new DBAdapter(this);
        mDBHelper.open();


        //리스트뷰, 리스트뷰 어뎁터 초기화
        ListView listView = (ListView) findViewById(R.id.listview);
        listViewItemList = new ArrayList<>();
        listViewAdapter = new ListViewAdapter(listViewItemList);
        listView.setAdapter(listViewAdapter);

        setData(); // 리스트뷰, 현재 체중 셋팅

        lineChart = (LineChart) findViewById(R.id.chart);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(false);

        drawChart(); // 차트 그리기


        btnEditInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(InfoActivity.this, SignUpActivity.class);
                intent.putExtra("Mode", 2);
                startActivity(intent);
            }
        });
        btnInputWeight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WeightDialog dialog = new WeightDialog(InfoActivity.this);
                dialog.setDialogListener(new MyDialogListener() {  // MyDialogListener 를 구현
                    @Override
                    public void onPositiveClicked(String date, float weight) {

                        mDBHelper.createWeight(date, weight); // 입력값 DB에 추가(날짜체크해서 update/insert 결정)
                        setData(); // 리스트뷰 셋팅
                        setEntries(); // 그래프

                    }

                    @Override
                    public void onNegativeClicked() {
                    }
                });
                dialog.show();
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, final View view, final int position, long id) {
                AlertDialog.Builder dlg = new AlertDialog.Builder(InfoActivity.this);
                dlg.setMessage("기록을 삭제할까요?");
                dlg.setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        TextView txt = view.findViewById(R.id.tvDate);
                        String date = txt.getText().toString();
                        Log.v("listview click test", "date of clicked item : " + date);
                        mDBHelper.deleteWeight(date); // 삭제되면 true 안되면 false 반환함

                        setData(); // DB를 리스트뷰에 넣기
                        setEntries(); // 그래프

                    }
                });
                dlg.setNegativeButton("취소", null);
                dlg.setCancelable(false);
                dlg.show();

                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setInfo();
    }

    private void setInfo() {
        Cursor c_Info = mDBHelper.fetchInfo();
        byte[] img = c_Info.getBlob(0);
        if(img != null) {
            Bitmap bitmap_o = BitmapFactory.decodeByteArray(c_Info.getBlob(0), 0, c_Info.getBlob(0).length);
            ivInfoPic.setImageBitmap(bitmap_o);
        }
        tvInfoName.setText(c_Info.getString(1));
        tvInfoAge.setText(c_Info.getString(2));
        tvInfoGender.setText(c_Info.getString(3));
        c_Info.close();
    }

    private void setData() {
        if(!listViewAdapter.isEmpty()) {
            listViewAdapter.clearItem(); // 리스트뷰 비우기
        }
        Cursor c1 = mDBHelper.fetchWeight(); // 기준 데이터
        Cursor c2 = mDBHelper.fetchWeight(); // 비교 데이터
        c2.moveToNext();

        // 날짜기준 내림차순이므로 0이 가장 최근 값
        for (int r = 0; r < c1.getCount(); r++) {
            Log.v("setData test", "r / row : " + (r+1) + " / " + c1.getCount() + " ------------------------ LOOP 도는 중---------------------------");

            String DBdate = c1.getString(0);
            float DBweight = c1.getFloat(1);
            String weightStr = String.format("%.1f kg", DBweight);
            float diff = (c2.isAfterLast())? 0 : c2.getFloat(1) - DBweight;
            String strdiff = (diff != 0f) ? String.format("%.1f kg", Math.abs(diff)) : "-"; // 절대값으로 str 저장

            // 차이에 따라 이미지 지정
            int imgID = R.drawable.ic_none;
            if (diff < 0) imgID = R.drawable.info_ic_up;
            else if (diff > 0) imgID = R.drawable.info_ic_down;

            listViewItemList.add(new ListViewItem_weight(DBdate, weightStr, imgID, strdiff));

            c1.moveToNext();
            c2.moveToNext();
        }

        c1.moveToFirst();
        tvNowWeight.setText(c1.getFloat(1) + "");
        listViewAdapter.notifyDataSetChanged();

        c1.close();
        c2.close();
    }

    private void setEntries() {
        boolean isEdited = false;
        if(!entries.isEmpty()) {
            entries.clear();
            lineDataSet.clear();
            isEdited = true;
            Log.v("setEntry TEST", "ENTRIES CLEARED");
        }

        Cursor c = mDBHelper.fetchWeight();
        row = c.getCount();
        ArrayList<Float> entryWeight = new ArrayList<>();
        int range = (row > 10) ? 10 : row; // ENTRY 범위 최대 10개

        for(int r=0; r<range; r++){
            entryWeight.add(r, c.getFloat(1));
            Log.v("setData test", "r / range : " + r + "/" + range + ", " + "LIST ADDED : " + r + ", " + c.getFloat(1));
            c.moveToNext();
        }
        // 거꾸로 넣어 보아요...
        for(int i=0; i<range; i++){
            entries.add(new Entry(i, entryWeight.get(range-1-i)));
            Log.v("setData test", "i / range : " + i + "/" + range + ", " + "ENTRY ADDED : " + i + ", " + entryWeight.get(range-1-i));
        }
        c.close();
        if(isEdited) {
            lineData.notifyDataChanged();
            lineChart.notifyDataSetChanged();
            lineChart.invalidate();
        }
    }

    public void drawChart() {
        entries = new ArrayList<>();
        setEntries();
        Log.v("senEntries TEST", "senEntries DONE ---------------------------------------------------------------");
        lineDataSet = new LineDataSet(entries, "Data Set");
        lineDataSet.setLineWidth(2);
        lineDataSet.setCircleRadius(6);
        lineDataSet.setCircleColor(Color.parseColor("#FFA1B4DC"));
        lineDataSet.setCircleHoleColor(Color.BLUE);
        lineDataSet.setColor(Color.parseColor("#FFA1B4DC"));
        lineDataSet.setDrawCircleHole(true);
        lineDataSet.setDrawCircles(true);
        lineDataSet.setDrawHorizontalHighlightIndicator(false);
        lineDataSet.setDrawHighlightIndicators(false);
        lineDataSet.setDrawValues(false);

        lineData = new LineData(lineDataSet);
        lineChart.setData(lineData);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.BLACK);
        xAxis.enableGridDashedLine(8, 24, 0);
        xAxis.setAxisMinimum(-1f);
        xAxis.setAxisMaximum(10f);

        YAxis yLAxis = lineChart.getAxisLeft();
        yLAxis.setTextColor(Color.BLACK);
        //yLAxis.setAxisMinimum();

        YAxis yRAxis = lineChart.getAxisRight();
        yRAxis.setDrawLabels(false);
        yRAxis.setDrawAxisLine(false);
        yRAxis.setDrawGridLines(false);

        Description description = new Description();
        description.setText("");

        lineChart.setDoubleTapToZoomEnabled(false);
        lineChart.setDrawGridBackground(false);
        lineChart.setDescription(description);
        lineChart.animateY(1000, Easing.EasingOption.EaseInCubic);
        lineChart.invalidate();
    }

    private class WeightDialog extends Dialog implements View.OnClickListener {
        private Context context;
        private EditText edtWeight;
        private Button btnCancel, btnOK;
        private DatePicker dpWeight;
        private String date;
        private float weight;
        private MyDialogListener dialogListener;

        public WeightDialog(@NonNull Context context) {
            super(context);
            this.context = context;
        }

        void setDialogListener(MyDialogListener dialogListener) {
            this.dialogListener = dialogListener;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.dlg_weight);

            edtWeight = (EditText) findViewById(R.id.edtInputWeight);
            dpWeight = (DatePicker) findViewById(R.id.datePickerWeight);
            btnCancel = (Button) findViewById(R.id.btnCancel);
            btnOK = (Button) findViewById(R.id.btnOK);
            btnCancel.setOnClickListener(this);
            btnOK.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnCancel:
                    dismiss();
                    break;
                case R.id.btnOK:
                    date = LocalDate.of(dpWeight.getYear(), dpWeight.getMonth()+1, dpWeight.getDayOfMonth()).format(DF_DEFAULT);
                    if(edtWeight.getText().toString().equals("")){
                        Toast.makeText(context, "체중을 입력해주세요.", Toast.LENGTH_SHORT).show();
                        btnInputWeight.callOnClick();
                    }else{
                        weight = Float.parseFloat(edtWeight.getText().toString());
                        dialogListener.onPositiveClicked(date, weight);
                    }
                    dismiss();
                    break;
            }
        }
    }

    public interface MyDialogListener {
        void onPositiveClicked(String date, float weight);
        void onNegativeClicked();
    }

    public class ListViewAdapter extends BaseAdapter {

        public ArrayList<ListViewItem_weight> listViewItemList;

        public ListViewAdapter(ArrayList<ListViewItem_weight> listViewItemList) {
            this.listViewItemList = listViewItemList;
        }

        @Override
        public int getCount() {
            return listViewItemList.size();
        }

        @Override
        public Object getItem(int position) {
            return listViewItemList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final int pos = position;
            final Context context = parent.getContext();

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.listitem_weight, parent, false);
            }

            TextView tvDate = (TextView) convertView.findViewById(R.id.tvDate);
            TextView tvWeight = (TextView) convertView.findViewById(R.id.tvWeight);
            ImageView ivIndicator = (ImageView) convertView.findViewById(R.id.ivIndicator);
            TextView tvDiff = (TextView) convertView.findViewById(R.id.tvDiff);

            ListViewItem_weight listViewItem = listViewItemList.get(position);

            tvDate.setText(listViewItem.getStrDate());
            tvWeight.setText(listViewItem.getStrWeight());
            ivIndicator.setImageResource(listViewItem.getImgID());
            tvDiff.setText(listViewItem.getStrDiff());

            return convertView;
        }

        public void addItem(String text1, String text2, int imgID, String text3) {
            ListViewItem_weight item = new ListViewItem_weight(text1, text2, imgID, text3);
            listViewItemList.add(item);
        }

        public void clearItem() {
            listViewItemList.clear();
        }
    }

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDBHelper.close();
    }
}




