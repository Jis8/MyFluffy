package com.example.jisoo.myfluffy;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

public class MonthYearPickerDIalog extends DialogFragment implements View.OnClickListener {
    final static String YEAR = "Year";
    final static String MONTH = "Month";

    int selectedYear, selectedMonth;
    int cYear, cMonth;
    private Button btnPrev, btnNext, btnOK, btnCancel;
    private MyDialogListener dialogListener;

    public static MonthYearPickerDIalog newInstance(int year, int month){
        MonthYearPickerDIalog myDlg = new MonthYearPickerDIalog();
        Bundle args = new Bundle();
        args.putInt(YEAR, year);
        args.putInt(MONTH, month);
        myDlg.setArguments(args);
        return myDlg;
    }

    public interface MyDialogListener {
        void onOKClicked();

        void onCancelClicked();

    }

    void setDialogListener(MyDialogListener dialogListener) {
        this.dialogListener = dialogListener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.dlg_mypicker, container, false);
        TextView tvYear = (TextView) v.findViewById(R.id.dlg_tvYear);
        btnPrev = (Button) v.findViewById(R.id.dlg_btnPrev);
        btnNext = (Button) v.findViewById(R.id.dlg_btnNext);
//        btnCancel = (Button) v.findViewById(R.id.dlg_btnCancel);
//        btnOK = (Button) v.findViewById(R.id.dlg_btnOK);
        RadioGridTableLayout rgMonths = (RadioGridTableLayout) v.findViewById(R.id.dlg_rgMonths);


        cYear = savedInstanceState.getInt(YEAR);
        cMonth = savedInstanceState.getInt(MONTH);

        tvYear.setText(cYear);
        RadioButton rbNow = (RadioButton) rgMonths.getChildAt(cMonth-1);
        rbNow.setChecked(true);

        int rbID = rgMonths.getCheckedRadioButtonId();
        RadioButton rb = (RadioButton) v.findViewById(rbID);




        selectedMonth = rgMonths.indexOfChild(rb);



//        btnCancel.setOnClickListener(this);
//        btnOK.setOnClickListener(this);


        return v;

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            /*case R.id.dlg_btnCancel:
                dialogListener.onCancelClicked();
                dismiss();
                break;
            case R.id.dlg_btnOK:
                dialogListener.onOKClicked();
                dismiss();
                break;*/
        }
    }



}
