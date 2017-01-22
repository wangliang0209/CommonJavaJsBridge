package com.msj.jsbridge.fragment.entity;

import java.util.List;

/**
 * @author Vincent.M
 * @date 17/1/11
 * @copyright ©2017 孟祥程 All Rights Reserved
 * @desc
 */
public class Date {

    private String IS_USER;

    private String NAME;

    List<DateInner> dates;

    public String getIS_USER() {
        return IS_USER;
    }

    public void setIS_USER(String IS_USER) {
        this.IS_USER = IS_USER;
    }

    public String getNAME() {
        return NAME;
    }

    public void setNAME(String NAME) {
        this.NAME = NAME;
    }

    public List<DateInner> getDates() {
        return dates;
    }

    public void setDates(List<DateInner> dates) {
        this.dates = dates;
    }
}
