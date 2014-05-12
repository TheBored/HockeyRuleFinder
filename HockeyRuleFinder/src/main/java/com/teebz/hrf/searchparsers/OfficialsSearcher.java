package com.teebz.hrf.searchparsers;

import com.teebz.hrf.entities.Official;

import android.content.res.AssetManager;
import android.util.Log;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class OfficialsSearcher {
    private static OfficialsSearcher searcher;

    private ArrayList<Official> officials;

    //Implement a singleton to ensure that parsing only happens once.
    public static synchronized OfficialsSearcher getSearcher(AssetManager manager) {
        if (searcher == null) {
            searcher = new OfficialsSearcher(manager);
        }
        return searcher;
    }

    //Don't let anyone call the constructor outside this class.
    private OfficialsSearcher(AssetManager manager) {
        try { //Get all of the sections.
            InputStream s = manager.open("officials.xml");
            OfficialsXMLParser parse = new OfficialsXMLParser(s);
            officials = parse.getAllOfficials();
            s.close();
        } catch (Exception e) {
            Log.d("HockeyRuleFinder", "OfficialsXMLParser failed parsing.");
        }

        //Lastly, sort ze list.
        Collections.sort(officials, new Comparator<Official>() {
            public int compare(Official o1, Official o2) {
                if (o1.number.equals(o2.number))
                    return 0;
                return Integer.parseInt(o1.number) < Integer.parseInt(o2.number) ? -1 : 1;
            }
        });
    }

    public ArrayList<Official> getAllOfficials() {
        return officials;
    }

    public Official getOfficialByJerseyNumber(String jerseyNumber) {
        for (Official o : officials) {
            if (o.number.equals(jerseyNumber)) {
                return o;
            }
        }
        return null;
    }
}
