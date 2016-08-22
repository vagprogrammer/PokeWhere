package com.javic.pokewhere.models;

/**
 * Created by franciscojimenezjimenez on 09/08/16.
 */
public class LocalPokeStop {

    private String id;
    private String type;
    private String name;
    private String description;
    private Boolean hasLure;
    private double distance;
    private boolean inRange;
    private boolean inRangeForLuredPokemon;
    private boolean canLoot;
    private double latitude;
    private double longitude;



    public LocalPokeStop(){
        //Empty constructor
        hasLure = false;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getHasLure() {
        return hasLure;
    }

    public void setHasLure(Boolean hasLure) {
        this.hasLure = hasLure;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public boolean isCanLoot() {
        return canLoot;
    }

    public void setCanLoot(boolean canLoot) {
        this.canLoot = canLoot;
    }

    public boolean isInRange() {
        return inRange;
    }

    public void setInRange(boolean inRange) {
        this.inRange = inRange;
    }

    public boolean isInRangeForLuredPokemon() {
        return inRangeForLuredPokemon;
    }

    public void setInRangeForLuredPokemon(boolean inRangeForLuredPokemon) {
        this.inRangeForLuredPokemon = inRangeForLuredPokemon;
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
