package com.javic.pokewhere.models;

/**
 * Created by vagprogrammer on 15/08/16.
 */
public class LocalGym {

    private String id;
    private String name;
    private int team;
    private String description;
    private boolean isInBattle;
    private long points;
    private double latitude;
    private double longitude;

    public LocalGym() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTeam() {
        return team;
    }

    public void setTeam(int team) {
        this.team = team;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isInBattle() {
        return isInBattle;
    }

    public void setInBattle(boolean inBattle) {
        isInBattle = inBattle;
    }

    public long getPoints() {
        return points;
    }

    public void setPoints(long points) {
        this.points = points;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
