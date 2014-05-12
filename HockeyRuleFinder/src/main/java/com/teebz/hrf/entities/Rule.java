package com.teebz.hrf.entities;

import java.io.Serializable;
import java.util.List;

public class Rule implements Serializable {
    public final String id;
    public final String name;

    public final List<String> pureContents;
    public final List<String> searchableContents;
    public final List<String> htmlContents;

    public final List<Rule> subRules;
    public final String imgName;

    public Rule(String id, String name, List<String> pureContents,
                List<String> searchContents, List<String> htmlContents,
                List<Rule> subRules, String imgName) {
        this.id = id;
        this.name = name;
        this.pureContents = pureContents;
        this.searchableContents = searchContents;
        this.htmlContents = htmlContents;
        this.subRules = subRules;
        this.imgName = imgName;
    }
}