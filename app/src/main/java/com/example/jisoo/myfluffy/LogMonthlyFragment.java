package com.example.jisoo.myfluffy;


import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

import static com.example.jisoo.myfluffy.MyValues.*;


/**
 * TODO:
 * 아이콘 12개 만들자
 * 달력일 땐 식사,배변 선택 해제돼서 넘어오면 좋겠음
 */

/**
 * A simple {@link Fragment} subclass.
 */
public class LogMonthlyFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String DATE = "Date";
    private static final String CATEGORY = "Category";

    // TODO: Rename and change types of parameters
    private String mDateStr;
    private ArrayList<String> selectedCategory;
    private int day, dayFirst, dayMax;
    LocalDate mDate;
    TextView monthly_tv;
    private DBAdapter mDB;
    private GridAdapter gridAdapter;
    private ArrayList<String> dayList = new ArrayList<String>(); // 일자 저장할 리스트
    private int icons_none[] = new int[5]; // 빈 이미지 배열
    private ArrayList<ArrayList<Integer>> contentList = new ArrayList<>(); // 일자의 아이콘들 배열(icons[]) 저장하는 리스트

    private int iconID[] = {R.drawable.ic_none,
            R.drawable.ic_food, R.drawable.ic_toilet, R.drawable.ic_walk,
            R.drawable.ic_symptoms, R.drawable.ic_clinic, R.drawable.ic_medicine, R.drawable.ic_vaccine,
            R.drawable.ic_bath, R.drawable.ic_haircut, R.drawable.ic_diary, R.drawable.ic_etc};

    private OnDaySetListener dp;


    public LogMonthlyFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * //@param date Parameter 1.
     * @return A new instance of fragment LogDailyFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LogMonthlyFragment newInstance() {
        LogMonthlyFragment fragment = new LogMonthlyFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mDB = new DBAdapter(getContext());
            mDB.open();

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.v("Test_Monthly", "onCreateView");

        View v = inflater.inflate(R.layout.fragment_log_monthly, container, false);

        if (getActivity() != null && getActivity() instanceof LogActivity) {
            mDate = ((LogActivity) getActivity()).getDate();
            selectedCategory = ((LogActivity) getActivity()).getCategory();
        }



        for (int i = 0; i < 5; i++) {
            icons_none[i] = R.drawable.ic_none;
        }

        // 달력 그리드뷰
        GridView monthly_grid = (GridView) v.findViewById(R.id.monthly_grid);
        gridAdapter = new GridAdapter(getActivity().getApplicationContext(), dayList, contentList);
        monthly_grid.setAdapter(gridAdapter);

        // 아이템 클릭 이벤트 >> 해당 날짜 daily로 이동
        monthly_grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                day = (position + 1) - (dayFirst - 1); // position은 0부터 시작하고 dayFirst는 1부터 시작함... 뭐래 모르겠어 암튼 일케해야맞게나옴
                Log.v("Monthly_itemClick test", "dayFirst : " + dayFirst + ", dayMax : " + dayMax + ", day : " + day + ", position : " + position);
                if (0 < day && day <= dayMax) { // 있는 날짜 클릭했을 때만
                    Toast.makeText(getActivity().getApplicationContext(), mDate.getYear() + "년 " + mDate.getMonthValue() + "월" + day + "일 기록으로 이동합니다.", Toast.LENGTH_SHORT).show();
                    mDate = LocalDate.of(mDate.getYear(), mDate.getMonth(), day);
                    dp.onDaySet(1, mDate);
                }
            }
        });

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnDaySetListener) {
            dp = (OnDaySetListener) context;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setCalDate();
        setCalContent();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        dp = null;
    }

    public interface OnDaySetListener {
        void onDaySet(int mode, LocalDate date);
    }

    private void setCalContent() { //ArrayList<String> selectedCategory, ArrayList<String> dList

        if(contentList != null)
            contentList.clear();

        ArrayList<Integer> icons_n = new ArrayList<>();
        // 한달 loop 돌기
        for(int i=0; i<dayList.size(); i++) {

            if(dayList.get(i).equals("")) { // 공백일 때
                contentList.add(icons_n);
            }
            else{ // 해당 날짜 데이터 꺼내오기
                ArrayList<Integer> icons = new ArrayList<>();
                int day = Integer.parseInt(dayList.get(i));
                LocalDate calDate = LocalDate.of(mDate.getYear(), mDate.getMonthValue(), day);
                Cursor c = mDB.fetchCategory(selectedCategory, calDate.format(DF_DEFAULT), 0);
                Log.v("calDate TEST", "calDate : " + calDate.format(DF_DEFAULT) + "---------------------------------------------------------");

                // 카테고리 아이콘 대입하기
                int icID;
                while (!c.isAfterLast()) {
                    String s = c.getString(0);
                    switch (s) {
                        case SYMPTOMS: icID = R.drawable.ic_symptoms; break;
                        case CLINIC: icID = R.drawable.ic_clinic; break;
                        case BATH: icID = R.drawable.ic_bath; break;
                        case WALK: icID = R.drawable.ic_walk; break;
                        case DIARY: icID = R.drawable.ic_diary; break;
                        case HAIRCUT: icID = R.drawable.ic_haircut; break;
                        case VACCINE: icID = R.drawable.ic_vaccine; break;
                        case MEDICINE: icID = R.drawable.ic_medicine; break;
                        case ETC: icID = R.drawable.ic_etc; break;
                        case FOOD: icID = R.drawable.ic_food; break;
                        case TOILET: icID = R.drawable.ic_toilet; break;
                        default: icID = R.drawable.ic_none; break;
                    }
                    icons.add(icID);
                    c.moveToNext();
                }
                contentList.add(icons);
            }
        }
        gridAdapter.notifyDataSetChanged();
    }


    /**
     * 해당 월 달력 생성
     */
    private void setCalDate() {

        // 달력 날짜 초기화
        if(dayList != null)
            dayList.clear();

        // 1일 전 공백
        dayFirst = mDate.with(TemporalAdjusters.firstDayOfMonth()).getDayOfWeek().getValue(); /// 1일의 요일
        for (int i = 1; i < dayFirst; i++) {
            dayList.add(""); // 1일 전까지 공백 추가
//            Log.v("daylist1 TEST", "i : " + i);
        }

        // 1일부터 말일까지
        dayMax = mDate.with(TemporalAdjusters.lastDayOfMonth()).getDayOfMonth(); // 월의 마지막 날
        for (int i = 1; i <= dayMax; i++) {
            dayList.add(i + ""); // 날짜 넣기
//            Log.v("daylist2 TEST", "i : " + i);
        }

        int dayLast = mDate.with(TemporalAdjusters.lastDayOfMonth()).getDayOfWeek().getValue(); // 말일의 요일
        for (int i = dayLast; i < 7; i++) {
            dayList.add(""); // 말일 이후 공백 추가
//            Log.v("daylist3 TEST", "i : " + i);
        }

    }

    private class DayContent {
        String day;
        List<Integer> icons;

        DayContent(String day, List<Integer> icons) {
            this.day = day;
            this.icons = icons;
        }

        String getDay() {
            return day;
        }

        List<Integer> getIcons() {
            return icons;
        }

    }

    /**
     * 그리드뷰 어댑터
     */
    private class GridAdapter extends BaseAdapter {
        private final List<String> dList;
        private final List<ArrayList<Integer>> cList;

        private final LayoutInflater inflater;

        public GridAdapter(Context context, List<String> dList, List<ArrayList<Integer>> cList) {
            this.dList = dList;
            this.cList = cList;
            this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return dList.size();
        }

        @Override
        public DayContent getItem(int position) {
            DayContent dc = new DayContent(dList.get(position), cList.get(position));
            return dc;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            DayContent dc = getItem(position);
            List<Integer> icons = dc.getIcons();
            int[] iconID = {R.id.gitem_iv1, R.id.gitem_iv2, R.id.gitem_iv3, R.id.gitem_iv4, R.id.gitem_iv5};

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_monthly_grid, parent, false);
                holder = new ViewHolder();
                holder.tvDay = (TextView) convertView.findViewById(R.id.gitem_tvDate);
                for(int i=0; i<5; i++)
                    holder.ivIcon[i] = (ImageView) convertView.findViewById(iconID[i]);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.tvDay.setText(dc.getDay());

            // 이상증세 골라내기
            for(int i=0; i<icons.size(); i++){
                if(icons.get(i) == R.drawable.ic_symptoms){
                    holder.ivIcon[4].setImageResource(icons.get(i));
                    icons.remove(i);
                }
            }
            int range = (icons.size() > 4)? 4 : icons.size();

            // 아이콘 개수만큼 이미지뷰에 넣기
            for(int i=0; i<range; i++){
                holder.ivIcon[i].setImageResource(icons.get(i));
                Log.v("GridAdapter_ImageView TEST", "i : " + i + ", icon : " + icons.get(i));
            }


            //오늘 날짜 텍스트 컬러,배경 변경 나중에 디자인해서 적용하기
            String sToday = String.valueOf(LocalDate.now().getDayOfMonth());
            if(mDate.equals(LocalDate.now()) && sToday.equals(dc.getDay())){
                holder.tvDay.setTextColor(Color.BLACK);
                holder.tvDay.setBackgroundColor(Color.LTGRAY);
            }

            return convertView;
        }
    }

    private class ViewHolder {
        TextView tvDay;
        ImageView ivIcon[] = new ImageView[5];
    }

}
