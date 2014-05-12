package com.teebz.hrf.searchparsers;

import com.teebz.hrf.entities.Official;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class OfficialsXMLParser extends BaseParser {
    public OfficialsXMLParser(InputStream in) {
        super(in);
    }

    public ArrayList<Official> getAllOfficials() throws XmlPullParserException, IOException {
        ArrayList<Official> officials = new ArrayList<Official>();

        Parser.require(XmlPullParser.START_TAG, EmptyNamespace, "officials");
        while (Parser.next() != XmlPullParser.END_TAG) {
            if (Parser.getEventType() == XmlPullParser.END_DOCUMENT) {
                break;
            }
            if (Parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = Parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("official")) {
                officials.add(readOfficial());
            } else {
                skip();
            }
        }
        return officials;
    }

    public Official readOfficial() throws XmlPullParserException, IOException {
        Parser.require(XmlPullParser.START_TAG, EmptyNamespace, "official");
        //We are at the starting "official" tag, move to the inside.
        while (Parser.next() != XmlPullParser.END_TAG) {
            if (Parser.getEventType() == XmlPullParser.START_TAG) {
                break;
            }
        }

        String number = getValueFromNodeAndContinue();
        String name = getValueFromNodeAndContinue();
        String league = getValueFromNodeAndContinue();
        String memberSince = getValueFromNodeAndContinue();
        String regSeasonCount = getValueFromNodeAndContinue();
        String firstRegSeason = getValueFromNodeAndContinue();
        String firstRegGame = getValueFromNodeAndContinue();
        String playoffCount = getValueFromNodeAndContinue();
        String firstPlayoffSeason = getValueFromNodeAndContinue();
        String firstPlayoffGame = getValueFromNodeAndContinue();

        return new Official(number, name, league, memberSince, regSeasonCount,
                            firstRegSeason, firstRegGame, playoffCount,
                            firstPlayoffSeason, firstPlayoffGame);
    }
}
