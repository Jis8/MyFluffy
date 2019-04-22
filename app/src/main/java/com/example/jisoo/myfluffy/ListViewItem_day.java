package com.example.jisoo.myfluffy;


public class ListViewItem_day {

    private String strCategory; // drawable 변경해야할지도
    private String strTitle;
    private String strTime;
    private String strContent;

    public ListViewItem_day(String strCategory, String strTitle, String strTime, String strContent) {
        this.strCategory = strCategory;

        this.strTitle = strTitle;
        this.strTime = strTime;
        this.strContent = strContent;
    }

    public String getStrCategory() {
        return strCategory;
    }

    public String getStrTitle() {
        return strTitle;
    }

    public String getStrTime() {
        return strTime;
    }

    public String getStrContent() {
        return strContent;
    }

}
