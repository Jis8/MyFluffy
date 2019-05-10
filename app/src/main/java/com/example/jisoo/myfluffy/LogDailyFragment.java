package com.example.jisoo.myfluffy;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;


import java.time.LocalDate;
import java.util.ArrayList;

import static com.example.jisoo.myfluffy.MyValues.*;



public class LogDailyFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    private static final String DATE = "Date";
    private static final String CATEGORY = "Category";

    // TODO: Rename and change types of parameters
    private String mDateStr;
    private ArrayList<String> selectedCategory;
    FloatingActionButton fabAdd;

    private ArrayList<ListViewItem_day> listViewItemList;
    private ListViewAdapter listViewAdapter;

    LocalDate mDate;
    private DBAdapter mDB;

    public LogDailyFragment() {
        // Required empty public constructor
    }

    public static LogDailyFragment newInstance() {
        LogDailyFragment fragment = new LogDailyFragment();
        Bundle args = new Bundle();
//        args.putString(DATE, date);
//        args.putSerializable(CATEGORY, selectedCategory);
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
        Log.v("Test_Daily","onCreateView");

        View v = inflater.inflate(R.layout.fragment_log_daily, container, false);
        TextView daily_tv1 = (TextView) v.findViewById(R.id.daily_tv1);

        if(getActivity() != null && getActivity() instanceof LogActivity){
            mDate = ((LogActivity)getActivity()).getDate();
            selectedCategory = ((LogActivity)getActivity()).getCategory();
        } // 액티비티에서 날짜, 선택 카테고리 받아오기

        mDateStr = mDate.format(DF_DEFAULT);
        Log.v("Test_Daily","onCreateView_mDateStr : "+ mDateStr);
//        for(int i=0; i<selectedCategory.size(); i++)
//            Log.v("Test","LogDailyFragment onCreateView_category : "+ selectedCategory.get(i));


//리스트뷰, 리스트뷰 어뎁터 초기화
        ListView dailyList = (ListView) v.findViewById(R.id.daily_list);
        listViewItemList = new ArrayList<>();
        listViewAdapter = new ListViewAdapter(listViewItemList);
        dailyList.setAdapter(listViewAdapter);
//        daily_tv1.setText(mDateStr); // 확인용

//        setData(); //onResume()에 넣었음
//        Log.v("Test_Daily","onCreateView_setData()");

        fabAdd = (FloatingActionButton) v.findViewById(R.id.fabAdd);

        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), RecordActivity.class);
                intent.putExtra("Mode", 1);
                intent.putExtra("Category", FOOD); // 카테고리 선택 창 띄우기
                startActivity(intent);
            }
        });


        /** TODO
            대화상자에서 BACK BUTTON 동작 안함
         */
        // 아이템 클릭 이벤트 > 상세 dialog
        dailyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                final String date = mDate.format(DF_DEFAULT);
                final String cate = ((TextView) view.findViewById(R.id.tvCategory)).getText().toString();
                final String title = ((TextView) view.findViewById(R.id.tvTitle)).getText().toString();
                final String time = ((TextView) view.findViewById(R.id.tvTime)).getText().toString();

                FragmentTransaction ft = getFragmentManager().beginTransaction();
                Fragment prev = getFragmentManager().findFragmentByTag("dialog");
                if (prev != null)
                    ft.remove(prev);
                ft.addToBackStack(null); // 요거 왜필요하지?

                DayDetailDialog dayDlg = DayDetailDialog.newInstance(date, cate, title, time);
                dayDlg.setDialogListener(new DayDetailDialog.MyDialogListener() {
                    @Override
                    public void onPositiveClicked() {

                    }

                    @Override
                    public void onEditClicked() {
                        Intent intent = new Intent(getContext(), RecordActivity.class);
                        intent.putExtra("Mode", 2);
                        intent.putExtra("Date", date);
                        intent.putExtra("Category", cate);
                        intent.putExtra("Title", title);
                        intent.putExtra("Time1", time);
                        startActivity(intent);
                    }

                    @Override
                    public void onDeleteClicked() {
                        AlertDialog.Builder dlg = new AlertDialog.Builder(view.getContext());
                        dlg.setMessage("기록을 삭제할까요?");
                        dlg.setPositiveButton("예", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                boolean checkdelete = mDB.deleteRecord(cate, title, date, time);
                                Log.v("TEST", "IS ITEM DELETED FROM DB? " + checkdelete);
                                setData();
                            }
                        });
                        dlg.setNegativeButton("취소", null);
                        dlg.setCancelable(false);
                        dlg.show();
                    }
                });
                dayDlg.setCancelable(false);
                dayDlg.show(ft, "dialog");
            }
        });

        return v;
    }


    @Override
    public void onResume() {
        super.onResume();
        setData();
    }

    /**
     * 리스트뷰 데이터 넣기
     */
    public void setData() {

        if (!listViewAdapter.isEmpty())
            listViewAdapter.clearItem();

        Cursor c = mDB.fetchDailyRecord(selectedCategory, mDateStr, 0); // 선택된 날짜, 카테고리 데이터 가져오기
        Log.v("Test_Daily","setData() : "+ mDateStr);

        /**
         * TODO
         * 선택 카테고리말고 전체 기록 가져온 다음 리스트 저장하고 정렬해서 뽑을 수는 없을까
         * 그럼 업데이트를 어떻게 즉각 반영하지?????
         * BroadcastReceiver로 DB 변경될 때만 실행??? 굳이 필요한가..?
         */
        while(!c.isAfterLast()){
            String content = c.getString(5);
            if (content.contains("\n")) {
                String contents[] = c.getString(5).split("\n");
                content = contents[0];
            }

            listViewItemList.add(new ListViewItem_day(c.getString(0), c.getString(1), c.getString(3), content));
            c.moveToNext();
        }

        listViewAdapter.notifyDataSetChanged();
        Log.v("Test_Daily","setData()_listViewAdapter.notifyDataSetChanged()");


        for (int i = 0; i < listViewItemList.size(); i++) {
            Log.v("Test_Daily_LISTVIEW_TEST", "-\n"
                    + "selectedCategory : " + listViewItemList.get(i).getStrCategory() + "\n"
                    + "title : " + listViewItemList.get(i).getStrTitle() + "\n"
                    + "time : " + listViewItemList.get(i).getStrTime() + "\n"
                    + "content : " + listViewItemList.get(i).getStrContent());
        }
    }

    /**
     *  리스트뷰 어댑터
     */
    private class ListViewAdapter extends BaseAdapter {

        public ArrayList<ListViewItem_day> listViewItemList = new ArrayList<>();

        public ListViewAdapter(ArrayList<ListViewItem_day> listViewItemList) {
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
                convertView = inflater.inflate(R.layout.listitem_day, parent, false);
            }

            TextView tvCategory = (TextView) convertView.findViewById(R.id.tvCategory);
            TextView tvTime = (TextView) convertView.findViewById(R.id.tvTime);
            TextView tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
            TextView tvContent = (TextView) convertView.findViewById(R.id.tvContent);

            // Data Set(filteredItemList)에서 position에 위치한 데이터 참조 획득
            ListViewItem_day listViewItem = listViewItemList.get(position);

            tvCategory.setText(listViewItem.getStrCategory());
            tvTime.setText(listViewItem.getStrTime());
            tvTitle.setText(listViewItem.getStrTitle());
            tvContent.setText(listViewItem.getStrContent());

            return convertView;
        }

        public void addItem(String text1, String text2, String text3, String text4) {
            ListViewItem_day item = new ListViewItem_day(text1, text2, text3, text4);
            listViewItemList.add(item);
        }

        public void clearItem() {
            listViewItemList.clear();
        }
    }

}
