package com.example.jisoo.myfluffy;

class ListViewItem_weight {

    private String strDate;
    private String strWeight;
    private String strDiff;
    private int imgID;

    ListViewItem_weight(String strDate, String strWeight, int imgID, String strDiff) {
        this.strDate = strDate;
        this.strWeight = strWeight;
        this.strDiff = strDiff;
        this.imgID = imgID;
    }

    String getStrDate() {
        return strDate;
    }


    String getStrWeight() {
        return strWeight;
    }


    String getStrDiff() {
        return strDiff;
    }


    int getImgID() {
        return imgID;
    }

}
