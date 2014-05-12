package com.teebz.hrf.entities;

public class Section {
    public final String id;
    public final String name;
    public final Rule[] rules;

    public Section(String id, String name, Rule[] rules) {
        this.id = id;
        this.name = name;
        this.rules = rules;
    }
}