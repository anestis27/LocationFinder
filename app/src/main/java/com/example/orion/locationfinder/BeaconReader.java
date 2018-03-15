package com.example.orion.locationfinder;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BeaconReader {

    private Map<String, RoomDetails> map;

    public BeaconReader(Context context) throws JSONException {
        map = new HashMap<>();
        JSONObject root = new JSONObject(readJSON(context));
        JSONArray beacons = root.getJSONArray("beacons");
        for(int i=0; i<beacons.length(); i++) {
            JSONObject beacon = beacons.getJSONObject(i);
            RoomDetails rd = new RoomDetails(
                    beacon.getString("alias"),
                    beacon.getString("room"),
                    beacon.getString("roomName"),
                    beacon.getString("level")
            );
            map.put(rd.getAlias(), rd);
        }
    }

    private String readJSON(Context context) {
        String json = null;
        try {
            InputStream is = context.getAssets().open("beacons.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    public RoomDetails getRoom(String alias) {
        return map.get(alias);
    }

    public List<RoomDetails> getRooms() {
        return new ArrayList<RoomDetails>(map.values());
    }
}
