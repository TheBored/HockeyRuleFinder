package com.teebz.hrf.entities;

public class League {
    private Integer mLID;
    private String mName;
    private String mAcronym;

    public Integer getLID() {
        return mLID;
    }

    public void setLID(Integer lid) {
        this.mLID = lid;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public String getAcronym() {
        return mAcronym;
    }

    public void setAcronym(String acronym) {
        this.mAcronym = acronym;
    }
}
