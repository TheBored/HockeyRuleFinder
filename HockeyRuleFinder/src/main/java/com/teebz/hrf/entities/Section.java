package com.teebz.hrf.entities;

public class Section {
    private Integer mSID;
    private Integer mLID;
    private String mNum;
    private String mName;
    private Rule[] mRules;

    public Section() {
        mSID = null;
        mLID = null;
        mNum = null;
        mName = null;
        mRules = null;
    }

    public int getSID() {
        return mSID;
    }

    public void setSID(int sid) {
        this.mSID = sid;
    }

    public Integer getLID() {
        return mLID;
    }

    public void setLID(int lid) {
        this.mLID = lid;
    }

    public String getNum() {
        return mNum;
    }

    public void setNum(String num) {
        this.mNum = num;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public Rule[] getRules() {
        return mRules;
    }

    public void setRules(Rule[] rules) {
        this.mRules = rules;
    }
}