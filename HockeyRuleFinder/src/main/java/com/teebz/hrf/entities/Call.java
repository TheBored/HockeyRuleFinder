package com.teebz.hrf.entities;

import java.io.Serializable;

public class Call implements Serializable {
    public final String id;
    public final String name;
    public final String desc;
    public final String imgName;
    public final String assocRuleNum;

    public Call(String id, String name, String desc, String imgName, String assocRuleNum) {
        this.id = id;
        this.name = name;
        this.desc = desc;
        this.assocRuleNum = assocRuleNum;
        this.imgName = imgName;
    }
}