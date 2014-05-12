package com.teebz.hrf.searchparsers;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;
import android.util.Xml;

import java.io.IOException;
import java.io.InputStream;

public class BaseParser {
    protected static final String EmptyNamespace = null;
    protected XmlPullParser Parser;

    protected BaseParser(InputStream in) {
        try {
            Parser = Xml.newPullParser();
            Parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            Parser.setInput(in, null);
            Parser.nextTag();
        } catch (Exception e) {
            Log.d("HockeyRuleFinder", "BaseParser initialize failed.");
        }
    }

    //Method that skips XML entries, likely irrelevant to us.
    protected void skip() throws XmlPullParserException, IOException {
        if (Parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (Parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    protected String getValueFromNodeAndContinue() throws XmlPullParserException, IOException {
        //Make sure we have the correct parser state
        if (Parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }

        //We aren't reading the opening tag, we want the contents.
        Parser.next();

        //Yay value
        String response = Parser.getText();

        //Move the parser to the next good state.
        while (Parser.getEventType() != XmlPullParser.END_DOCUMENT && Parser.getEventType() != XmlPullParser.END_TAG) {
            Parser.next();
        }

        //We are at the end tag, move on
        Parser.next();
        Parser.next();

        //We are at the end of the document or we are at the next node. End!
        return response;
    }
}
