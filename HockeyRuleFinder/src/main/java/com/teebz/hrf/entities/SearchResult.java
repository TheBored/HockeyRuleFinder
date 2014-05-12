package com.teebz.hrf.entities;

public class SearchResult {
    public final String highlightText;
    public final int countFound;
    public final Rule ruleMatch;
    public final Section sectionMatch;

    public SearchResult(Rule rule, Section section, int countFound, String text) {
        this.ruleMatch = rule;
        this.sectionMatch = section;
        this.highlightText = text;
        this.countFound = countFound;
    }
}