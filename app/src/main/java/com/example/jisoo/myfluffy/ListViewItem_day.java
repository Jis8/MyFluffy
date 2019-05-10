package com.example.jisoo.myfluffy;


class ListViewItem_day {

    private String strCategory; // drawable 변경해야할지도
    private String strTitle;
    private String strTime;
    private String strContent;

    ListViewItem_day(String strCategory, String strTitle, String strTime, String strContent) {
        this.strCategory = strCategory;
        this.strTitle = strTitle;
        this.strTime = strTime;
        this.strContent = strContent;
    }

    String getStrCategory() {
        return strCategory;
    }

    String getStrTitle() {
        return strTitle;
    }

    String getStrTime() {
        return strTime;
    }

    String getStrContent() {
        return strContent;
    }

}
