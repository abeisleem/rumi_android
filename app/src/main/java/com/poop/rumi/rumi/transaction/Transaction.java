package com.poop.rumi.rumi.transaction;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;


public class Transaction implements Parcelable {

    private String item;
    private Float price;

    private ArrayList<String> names;


    // Alt + Insert/Constructor/Select all to make the constructor
    public Transaction(String item, Float price) {

        this.item = item;
        this.price = price;

        names = new ArrayList<>();

    }


    private Transaction(Parcel in) {
        item = in.readString();
        if (in.readByte() == 0) {
            price = null;
        } else {
            price = in.readFloat();
        }
        names = in.createStringArrayList();
    }

    public static final Creator<Transaction> CREATOR = new Creator<Transaction>() {
        @Override
        public Transaction createFromParcel(Parcel in) {
            return new Transaction(in);
        }

        @Override
        public Transaction[] newArray(int size) {
            return new Transaction[size];
        }
    };

    // Alt + Insert/Setter and Getter/Select all to make the constructor
    public ArrayList<String> getNames() {

        return names;
    }

    public String printNames(){

        StringBuilder sb = new StringBuilder();
        String sep = "";
        for(String s : names){
            sb.append(sep).append(s);
            sep = ", ";

        }

        return sb.toString().trim();
    }

    public void addName(String name) {

        names.add(name);
    }

    public void removeName(String name){

        names.remove(name);
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public Float getPrice() {
        return price;
    }

    public void setPrice(Float price) {
        this.price = price;
    }



    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(item);
        if (price == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeFloat(price);
        }
        dest.writeStringList(names);
    }
}


