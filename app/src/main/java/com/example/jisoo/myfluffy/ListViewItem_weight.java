package com.example.jisoo.myfluffy;

import android.graphics.drawable.Drawable;

public class ListViewItem_weight {

    private String strDate;
    private String strWeight;
    private String strDiff;
    private int imgID;

    public ListViewItem_weight(String strDate, String strWeight, int imgID, String strDiff) {
        this.strDate = strDate;
        this.strWeight = strWeight;
        this.strDiff = strDiff;
        this.imgID = imgID;
    }

    public String getStrDate() {
        return strDate;
    }

    public void setStrDate(String strDate) {
        this.strDate = strDate;
    }

    public String getStrWeight() {
        return strWeight;
    }

    public void setStrWeight(String strWeight) {
        this.strWeight = strWeight;
    }

    public String getStrDiff() {
        return strDiff;
    }

    public void setStrDiff(String strDiff) {
        this.strDiff = strDiff;
    }

    public int getImgID() {
        return imgID;
    }

    public void setImgIndicator(Drawable imgIndicator) {
        this.imgID = imgID;
    }
}
