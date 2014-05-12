package com.teebz.hrf.searchparsers;

import com.teebz.hrf.entities.Call;

import android.content.res.AssetManager;
import android.util.Log;

import java.io.InputStream;
import java.util.ArrayList;

public class CallSearcher {
    private static CallSearcher searcher;

    private ArrayList<Call> calls;

    //Implement a singleton to ensure that parsing only happens once.
    public static synchronized CallSearcher getSearcher(AssetManager manager) {
        if (searcher == null) {
            searcher = new CallSearcher(manager);
        }
        return searcher;
    }

    //Don't let anyone call the constructor outside this class.
    private CallSearcher(AssetManager manager) {
        try { //Get all of the sections.
            InputStream s = manager.open("calls.xml");
            CallXMLParser parse = new CallXMLParser(s);
            calls = parse.getAllCalls();
            s.close();
        } catch (Exception e) {
            Log.d("HockeyRuleFinder", "CallXMLParser failed parsing.");
        }
    }

    public ArrayList<Call> getCalls() {
        return calls;
    }

    public Call getCallByCallId (String callId) {
        for (Call c : calls){
            if (c.id.equals(callId)) {
                return c;
            }
        }
        return null;
    }
}
