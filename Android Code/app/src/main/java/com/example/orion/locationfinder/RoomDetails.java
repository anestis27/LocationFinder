package com.example.orion.locationfinder;

public class RoomDetails {
    private String alias;
    private String room;
    private String roomName;
    private String level;

    public RoomDetails(String alias, String room, String roomName, String level) {
        this.alias = alias;
        this.room = room;
        this.roomName = roomName;
        this.level = level;
    }

    public String getAlias() {
        return alias;
    }

    public String getRoom() {
        return room;
    }

    public String getRoomName() {
        return roomName;
    }

    public String getLevel() {
        return level;
    }

    @Override
    public String toString() {
        return getRoomName()+" ("+getRoom()+")";
    }
}
