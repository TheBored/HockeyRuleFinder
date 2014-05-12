package com.teebz.hrf.entities;

import java.io.Serializable;

public class Call implements Serializable {
    public final String id;
    public final String name;
    public final String desc;
    public final String imgName;
    public final String assocRuleId;

    public Call(String id, String name, String desc, String imgName, String assocRuleId) {
        this.id = id;
        this.name = name;
        this.desc = desc;
        this.assocRuleId = assocRuleId;
        this.imgName = imgName;
    }
}