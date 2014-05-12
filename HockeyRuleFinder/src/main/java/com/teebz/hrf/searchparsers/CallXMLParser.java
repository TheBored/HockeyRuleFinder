package com.teebz.hrf.searchparsers;

import com.teebz.hrf.entities.Call;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class CallXMLParser extends BaseParser {
    public CallXMLParser(InputStream in)  {
        super(in);
    }

    public ArrayList<Call> getAllCalls() throws XmlPullParserException, IOException {
        ArrayList<Call> calls = new ArrayList<Call>();

        Parser.require(XmlPullParser.START_TAG, EmptyNamespace, "calls");
        while (Parser.next() != XmlPullParser.END_TAG) {
            if (Parser.getEventType() == XmlPullParser.END_DOCUMENT) {
                break;
            }
            if (Parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = Parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("call")) {
                calls.add(readCall());
            } else {
                skip();
            }
        }
        return calls;
    }

    private Call readCall() throws XmlPullParserException, IOException {
        Parser.require(XmlPullParser.START_TAG, EmptyNamespace, "call");

        String id = "";
        String callname = "";
        String desc = "";
        String imgName = "";
        String assocRuleId = "";

        while (Parser.next() != XmlPullParser.END_TAG) {
            //Move through file until we hit a start tag.
            if (Parser.getEventType() == XmlPullParser.START_TAG) {
                break;
            }
        }

        id = getValueFromNodeAndContinue();
        callname = getValueFromNodeAndContinue();
        desc = getValueFromNodeAndContinue();
        imgName = getValueFromNodeAndContinue();
        assocRuleId = getValueFromNodeAndContinue();

        return new Call(id, callname, desc, imgName, assocRuleId);
    }
}
