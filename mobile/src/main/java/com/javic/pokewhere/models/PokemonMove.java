package com.javic.pokewhere.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by vagprogrammer on 10/12/16.
 */

public class PokemonMove implements Parcelable {

    private String name;
    private int accuracy;
    private double critChance;
    private int energy;
    private int power;
    private int time;

    public PokemonMove() {
    }

    public PokemonMove(String name, int accuracy, double critChance, int energy, int power, int time) {
        this.name = name;
        this.accuracy = accuracy;
        this.critChance = critChance;
        this.energy = energy;
        this.power = power;
        this.time = time;
    }

    public PokemonMove(Parcel in) {
        name = in.readString();
        accuracy = in.readInt();
        critChance = in.readDouble();
        energy = in.readInt();
        power= in.readInt();
        time = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeInt(accuracy);
        parcel.writeDouble(critChance);
        parcel.writeInt(energy);
        parcel.writeInt(power);
        parcel.writeInt(time);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<PokemonMove> CREATOR = new Creator<PokemonMove>(){

        @Override
        public PokemonMove createFromParcel(Parcel parcel) {
            return new PokemonMove(parcel);
        }

        @Override
        public PokemonMove[] newArray(int size) {
            return new PokemonMove[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(int accuracy) {
        this.accuracy = accuracy;
    }

    public double getCritChance() {
        return critChance;
    }

    public void setCritChance(double critChance) {
        this.critChance = critChance;
    }

    public int getEnergy() {
        return energy;
    }

    public void setEnergy(int energy) {
        this.energy = energy;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }
}
