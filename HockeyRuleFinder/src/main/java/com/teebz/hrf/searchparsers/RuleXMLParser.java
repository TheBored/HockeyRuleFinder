package com.teebz.hrf.searchparsers;

import com.teebz.hrf.entities.Rule;
import com.teebz.hrf.entities.Section;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class RuleXMLParser extends BaseParser {
    public RuleXMLParser(InputStream in) {
        super(in);
    }

    public ArrayList<Section> getSections() throws XmlPullParserException, IOException {
        ArrayList<Section> sections = new ArrayList<Section>();

        Parser.require(XmlPullParser.START_TAG, EmptyNamespace, "league");
        while (Parser.next() != XmlPullParser.END_TAG) {
            if (Parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = Parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("section")) {
                sections.add(readSection());
            } else {
                skip();
            }
        }
        return sections;
    }

    private Section readSection() throws XmlPullParserException, IOException {
        Parser.require(XmlPullParser.START_TAG, EmptyNamespace, "section");

        String id = Parser.getAttributeValue(null, "sectionid");
        String sectionname = Parser.getAttributeValue(null, "sectionname");
        Rule[] sectionrules = null;

        List<Rule> rules = new ArrayList<Rule>();
        while (Parser.next() != XmlPullParser.END_TAG) {
            if (Parser.getEventType() == XmlPullParser.START_TAG) {
                String name = Parser.getName();
                if (Parser.getName().equals("rule")) {
                    Rule foundRule = readRule();
                    rules.add(foundRule);
                } else {
                    skip();
                }
            }
        }

        sectionrules = rules.toArray(new Rule[rules.size()]);

        Section s = new Section();
        s.setNum(id);
        s.setName(sectionname);
        s.setRules(sectionrules);

        return s;
    }

    private Rule readRule() throws XmlPullParserException, IOException {
        Parser.require(XmlPullParser.START_TAG, EmptyNamespace, "rule");
        String id = Parser.getAttributeValue(null, "ruleid");
        String rulename = Parser.getAttributeValue(null, "rulename");
        String img = Parser.getAttributeValue(null, "img");


        //If this is a leaf level rule, the rule has <par> nodes for each paragraph.
        //Otherwise, it has more rule nodes. In either case, collect the sub nodes.
        ArrayList<String> paragraphs = new ArrayList<String>();
        ArrayList<Rule> rules = new ArrayList<Rule>();
        while (Parser.next() != XmlPullParser.END_TAG) {
            if (Parser.getEventType() == XmlPullParser.START_TAG) {
                String name = Parser.getName();
                if (name.equals("par")) {
                    paragraphs.add(readParagraph());
                } else if (name.equals("rule")) {
                    Rule r = readRule();
                    rules.add(r);
                }
                else {
                    skip();
                }
            }
        }

        Rule r = new Rule();
        r.setNum(id);
        r.setName(rulename);
        r.setPureContents(paragraphs);
        r.setSubRules(rules);
        r.setImgName(img);

        return r;
    }

    private String readParagraph() throws XmlPullParserException, IOException {
        Parser.require(XmlPullParser.START_TAG, EmptyNamespace, "par");
        String text = null;

        Parser.next();
        text = Parser.getText();
        Parser.next();

        return text;
    }

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
}
