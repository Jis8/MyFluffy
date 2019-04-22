package com.example.jisoo.myfluffy;


import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class DayDetailDialog extends DialogFragment implements View.OnClickListener {
    final static String DATE = "Date";
    final static String CATEGORY = "Category";
    final static String TITLE = "Title";
    final static String TIME = "Time";

    DBAdapter mDB;

    private Button btnEdit, btnDelete, btnOK;
    private MyDialogListener dialogListener;

    public static DayDetailDialog newInstance(String date, String cate, String title, String time){
        DayDetailDialog dayDlg = new DayDetailDialog();
        Bundle args = new Bundle();
        args.putString(DATE, date);
        args.putString(CATEGORY, cate);
        args.putString(TITLE, title);
        args.putString(TIME, time);
        dayDlg.setArguments(args);
        return dayDlg;
    }

    public interface MyDialogListener {
        void onPositiveClicked();

        void onEditClicked();

        void onDeleteClicked();
    }

    void setDialogListener(MyDialogListener dialogListener) {
        this.dialogListener = dialogListener;
    }

    @Override
    public View onCreateView( LayoutInflater inflater,  ViewGroup container,  Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.dlg_day_detail, container, false);
        TextView dlg_tvCate = (TextView) v.findViewById(R.id.dlg_tvCate);
        TextView dlg_tvTitle = (TextView) v.findViewById(R.id.dlg_tvTitle);
        TextView dlg_tvDateTime = (TextView) v.findViewById(R.id.dlg_tvDateTime);
        TextView dlg_tvContent = (TextView) v.findViewById(R.id.dlg_tvContent);

        btnEdit = (Button) v.findViewById(R.id.btnEdit);
        btnDelete = (Button) v.findViewById(R.id.btnDelete);
        btnOK = (Button) v.findViewById(R.id.btnOK);

        btnEdit.setOnClickListener(this);
        btnDelete.setOnClickListener(this);
        btnOK.setOnClickListener(this);
        mDB = new DBAdapter(getContext());
        mDB.open();
        Cursor c_dlg = mDB.fetchThisRecord(getArguments().getString(CATEGORY), getArguments().getString(TITLE), getArguments().getString(DATE), getArguments().getString(TIME));


        dlg_tvCate.setText(c_dlg.getString(0));
        dlg_tvTitle.setText(c_dlg.getString(1));
        dlg_tvDateTime.setText(c_dlg.getString(2) + "  " + c_dlg.getString(3) + " - " + c_dlg.getString(4));
        dlg_tvContent.setText(c_dlg.getString(5));
        // 이미지도 불러와야함
        c_dlg.close();

        return v;

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnEdit:
                dialogListener.onEditClicked();
                dismiss();
                break;
            case R.id.btnDelete:
                dialogListener.onDeleteClicked();
                dismiss();
                break;
            case R.id.btnOK:
                dialogListener.onPositiveClicked();
                dismiss();
                break;
        }
    }

}