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

    private List<String> mPureContents;
    private List<String> mSearchContents;
    private List<String> mHtmlContents;

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

    public List<String> getPureContents() {
        return mPureContents;
    }

    public void setPureContents(List<String> pureContents) {
        this.mPureContents = pureContents;

        //When we set the pure (raw) contents, we also want to generate the search and HTML contents.
        mSearchContents = plainToSearchable(pureContents);
        mHtmlContents = plainToHTML(pureContents);
    }

    public List<String> getSearchContents() {
        return mSearchContents;
    }

    public List<String> getHtmlContents() {
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
    private List<String> plainToSearchable(List<String> paragraphs) {
        List<String> response = new ArrayList<String>();
        for (String s : paragraphs) {
            response.add(s);
        }

        for (int i = 0; i < response.size(); i++) {
            String par = response.get(i);
            //Remove all tags, just leave the text that should be searchable
            String linkPattern = "(\\[link=)(.{1,5})(\\])(.{1,5})(\\[/link\\])";
            par = par.replaceAll(linkPattern, "$4");
            //Remove the image tag here, need to test before/after
            String imagePattern = "(\\[image\\])(.*)(\\[/image\\])";
            par = par.replaceAll(imagePattern, "");
            //Place back in list
            response.set(i, par);
        }
        return response;
    }

    private List<String> plainToHTML(List<String> paragraphs) {
        List<String> response = new ArrayList<String>();
        for (String s : paragraphs) {
            response.add(s);
        }

        for (int i = 0; i < response.size(); i++) {
            String par = response.get(i);

            String pattern = "(\\[link=)(.{1,5})(\\])(.{1,5})(\\[/link\\])";
            par = par.replaceAll(pattern, "<a href=\"com.teebz.hrf://$2\">$4</a>");

            //If the paragraph starts with a roman numeral in parens, its a list item.
            String romanNumPattern = "\\([xiv]*\\)";
            Pattern compiledPattern = Pattern.compile(romanNumPattern);
            Matcher matcher = compiledPattern.matcher(par);
            if(matcher.find() && matcher.start() == 0){ //Found a result AND its at the start
                //Wrap the paragraph in italics
                par = "<i>" + par + "</i>";
            }

            //Place back in list
            response.set(i, par);
        }
        return response;
    }
    //endregion
}