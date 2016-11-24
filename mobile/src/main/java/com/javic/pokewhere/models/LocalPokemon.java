package com.javic.pokewhere.models;

/**
 * Created by vagprogrammer on 27/07/16.
 */
public class LocalPokemon {

    private long id;
    private String data;
    private long expiration_time;
    private int number;
    private String name;
    private double latitude;
    private double longitude;
    private String uid;
    private Boolean is_alive;

    public LocalPokemon() {

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public long getExpiration_time() {
        return expiration_time;
    }

    public void setExpiration_time(long expiration_time) {
        this.expiration_time = expiration_time;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Boolean getIs_alive() {
        return is_alive;
    }

    public void setIs_alive(Boolean is_alive) {
        this.is_alive = is_alive;
    }
}
