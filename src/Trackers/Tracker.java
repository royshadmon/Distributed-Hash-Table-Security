package Trackers;
import java.util.List;
import java.util.ArrayList;

import API.ChordNode;
import API.ChordTracker;

public class Tracker implements ChordTracker {

    private List<Integer> idList;

    Tracker () {
        idList = new ArrayList<>();
    }

    public Integer assignId() {
        int nextId = idList.get(idList.size()-1) + 1;
        idList.add(nextId);
        return nextId;
    }




}
