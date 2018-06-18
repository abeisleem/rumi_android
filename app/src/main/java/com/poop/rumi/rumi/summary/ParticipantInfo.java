package com.poop.rumi.rumi.summary;

import org.json.JSONArray;

import java.util.ArrayList;

public class ParticipantInfo {

    private String name;
    private ArrayList<String> mItems;
    private ArrayList<Float> mOgPrices;    //for original prices
    private ArrayList<Float> mPrices;      //for split prices

    public static final float DECIMAL_PLACES = (float)100.0;

    private ArrayList<ParticipantTriad> mTriad;

    public ParticipantInfo(String name) {
        this.name = name;

        mItems = new ArrayList<>();
        mOgPrices = new ArrayList<>();
        mPrices = new ArrayList<>();

        mTriad = new ArrayList<>();
    }

    public void addItemPrice(String item, Float origPrice, Float splitPrice){

        mItems.add(item);
        mOgPrices.add(origPrice);
        mPrices.add(Math.round(splitPrice * DECIMAL_PLACES) / DECIMAL_PLACES);

        mTriad.add(new ParticipantTriad(item, origPrice, splitPrice));

    }


    public String getName() {
        return name;
    }

    public JSONArray getItems() {

//        return mItems;

        return new JSONArray(mItems);
    }

    public JSONArray getOgPrices() {

//        String [] retArr = new String[mOgPrices.size()];
//        for(int i = 0; i < mOgPrices.size(); i++){
//
//            retArr[i] = mOgPrices.get(i).toString();
//
//        }
//
//        return retArr;

        return new JSONArray(mOgPrices);

    }

    public JSONArray getOwedPrices() {

//        String [] retArr = new String[mPrices.size()];
//        for(int i = 0; i < mPrices.size(); i++){
//
//            retArr[i] = mPrices.get(i).toString();
//
//        }
//
//        return retArr;

        return new JSONArray(mPrices);

    }

    public ArrayList<ParticipantTriad> getTriadList(){ return mTriad;}

    public String printInfo(){

        StringBuilder sb = new StringBuilder();
        String sep = "";

        sb.append(name +"\n===============\n");


        for(int i = 0; i < mItems.size(); i++) {
            sb.append(sep).append(mItems.get(i)).append(" : ").append(mPrices.get(i).toString());
            sep = "\n";
        }

        return sb.toString().trim();
    }

    public Float getTotal(){

        Float sum = 0f;

        for(Float f : mPrices)
            sum += f;

        return sum;
    }


    // For purposes of passing into SummaryListAdapter
    public class ParticipantTriad {

        String Item;
        Float OgPrice;
        Float OwedPrice;


        ParticipantTriad(String item, Float ogprice, Float owedprice){

            this.Item = item;
            this.OgPrice = ogprice;
            this.OwedPrice = owedprice;

        }


        public String getItem() {
            return Item;
        }

        public Float getOgPrice() {
            return OgPrice;
        }

        public Float getOwedPrice() {
            return OwedPrice;
        }






    }


}
