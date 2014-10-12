package com.teebz.hrf.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Rule implements Serializable {
    private Integer mRID;
    private Integer mSID;
    private Integer mParent_RID;

    private String mNum;
    private String mName;

    private String mPureContents;
    private String mSearchContents;
    private String mHtmlContents;

    private List<Rule> mSubRules;
    private String mImgName;

    public Rule() {
        this.mRID = null;
        this.mSID = null;
        this.mParent_RID = null;

        this.mNum = null;
        this.mName = null;
        this.mPureContents = null;
        this.mSearchContents = null;
        this.mHtmlContents = null;
        this.mSubRules = null;
        this.mImgName = null;
    }

    //region Get/Sets
    public Integer getRID() {
        return mRID;
    }

    public void setRID(Integer rid) {
        this.mRID = rid;
    }

    public Integer getSID() {
        return mSID;
    }

    public void setSID(Integer sid) {
        this.mSID = sid;
    }

    public Integer getParent_RID() {
        return mParent_RID;
    }

    public void setParent_RID(Integer parent_RID) {
        this.mParent_RID = parent_RID;
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

    public String getPureContents() {
        return mPureContents;
    }

    public void setPureContents(String pureContents) {
        this.mPureContents = pureContents;

        //When we set the pure (raw) contents, we also want to generate the search and HTML contents.
        mSearchContents = plainToSearchable(pureContents);
        mHtmlContents = plainToHTML(pureContents);
    }

    public String getSearchContents() {
        return mSearchContents;
    }

    public String getHtmlContents() {
        return mHtmlContents;
    }

    public List<Rule> getSubRules() {
        return mSubRules;
    }

    public void setSubRules(List<Rule> subRules) {
        this.mSubRules = subRules;
    }

    public String getImgName() {
        return mImgName;
    }

    public void setImgName(String imgName) {
        this.mImgName = imgName;
    }
    //endregion

    //region Private Helper Methods
    private String plainToSearchable(String paragraphs) {
        //Remove all tags, just leave the text that should be searchable
        String linkPattern = "(\\[link=)(.{1,5})(\\])(.{1,5})(\\[/link\\])";
        paragraphs = paragraphs.replaceAll(linkPattern, "$4");
        //Remove the image tag here, need to test before/after
        String imagePattern = "(\\[image\\])(.*)(\\[/image\\])";
        paragraphs = paragraphs.replaceAll(imagePattern, "");

        return paragraphs;
    }

    private String plainToHTML(String paragraphs) {
        //Insert the links used to direct users to related rules.
        String pattern = "(\\[link=)(.{1,5})(\\])(.{1,5})(\\[/link\\])";
        paragraphs = paragraphs.replaceAll(pattern, "<a href=\"com.teebz.hrf://$2\">$4</a>");

        //Lines that begin with roman numerals in parens (e.g. "(i)" or "(iv)") have italics wrapped around them.
        Pattern startPattern = Pattern.compile("^\\([xiv]*\\)(.*)$", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        Matcher startMatch = startPattern.matcher(paragraphs);
        StringBuffer sbStart = new StringBuffer();
        while (startMatch.find()) {
            //If we found enough text in group 2, replace out group 1.
            startMatch.appendReplacement(sbStart, "<i>$0</i>");
        }
        startMatch.appendTail(sbStart);
        paragraphs = sbStart.toString();

        //Swap newline formats.
        paragraphs = paragraphs.replace("\n", "<br /><br />");

        return paragraphs;
    }
    //endregion
}