package com.poop.rumi.rumi;


import java.text.DateFormat;
import java.util.Calendar;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


//TODO: make implements Parcelable
public class Receipt implements Serializable{


    public final String TAG = "ReceiptClass";

    private String currUser;
    private String currUserToken;

    private String receiptImagePath;

    private String storeName;

    private String dateOfCapture;

    private ArrayList<String> items;

    private ArrayList<Float> prices;


    // Looking for items with symbols
    private final Pattern symPttrn = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);

    // Looking for string within items with at least 12 digits (as fruit items in Walmart
    // include  2 chars (`KF`) at the end of the code
    private final Pattern dgtPttrn = Pattern.compile("[0-9]{12,}");

    private static transient Matcher m = null;

    // Passing image file path allows for extracting the date the receipt was captured given the format
    // of the image file path
    @RequiresApi(api = Build.VERSION_CODES.N)
    public Receipt(String currUserToken, String currUser, String imagePath) {

        this.receiptImagePath = imagePath;
        this.currUserToken = currUserToken;
        this.currUser = currUser;

        this.storeName = " ";
        this.dateOfCapture = dateToString();

        Log.d(TAG, "dateOfCapture = " + dateOfCapture);

        this.items = new ArrayList<>();
        this.prices = new ArrayList<>();
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    private String dateToString() {

        DateFormat df = new SimpleDateFormat("E MMM dd yyyy");

        Date today = Calendar.getInstance().getTime();

        return df.format(today);
    }


    public void setStoreName(String storeName) {

        this.storeName = storeName;
    }

    public void addItems(String itemsIn) {

//        Log.d(TAG, "in addItems");

//        Log.d(TAG, "RAWITEMS:\n" + itemsIn);


        // Splitting items by new line
        ArrayList<String> lines = new ArrayList<>(Arrays.asList(itemsIn.split("\n")));

        // For splitting by space
        ArrayList<String> spaces;

        String str;

        boolean chk = false;

        // Discarding items with symbols

        LINES:
        for (int idx = 0; idx < lines.size(); ++idx) {

            Log.d(TAG, "CHECK = " + (chk? "flipped" : "" ));
            chk = false;
            str = lines.get(idx);

            m = symPttrn.matcher(str);

            if (m.find()) {

                // subtracting from idx as to not skip lines
                lines.remove(idx);
                --idx;
                chk = true;
//                Log.d(TAG, "FOUND SYMPTRN: " + str);

                continue;


            }

            // TODO: ideally, have these constructed and initialized under var declarations
            boolean hasSave = Pattern.compile(Pattern.quote("save"), Pattern.CASE_INSENSITIVE).matcher(str).find();
            boolean hasSaving = Pattern.compile(Pattern.quote("saving"), Pattern.CASE_INSENSITIVE).matcher(str).find();
            boolean hasPromotion= Pattern.compile(Pattern.quote("promotion"), Pattern.CASE_INSENSITIVE).matcher(str).find();
            boolean hasSubtotal= Pattern.compile(Pattern.quote("subtotal"), Pattern.CASE_INSENSITIVE).matcher(str).find();
            boolean hasTotal= Pattern.compile(Pattern.quote("total"), Pattern.CASE_INSENSITIVE).matcher(str).find();
            boolean hasDebit= Pattern.compile(Pattern.quote("debit"), Pattern.CASE_INSENSITIVE).matcher(str).find();
            boolean hasChange= Pattern.compile(Pattern.quote("change"), Pattern.CASE_INSENSITIVE).matcher(str).find();
            boolean hasTax= Pattern.compile(Pattern.quote("tax"), Pattern.CASE_INSENSITIVE).matcher(str).find();

            if(hasSave || hasSaving || hasPromotion || hasSubtotal || hasTotal || hasDebit || hasChange || hasTax) {

                lines.remove(idx);
                --idx;

                continue;
            }

//            Log.d(TAG, "AT STRING: " + str);

            int numInts = 0;
            spaces = new ArrayList<>(Arrays.asList(str.split(" ")));

            for(int i = 0; i < spaces.size(); i++)
            {
                str = spaces.get(i);

                m = dgtPttrn.matcher(str);

                if(m.find() || str.length() == 1) // check length equal 1 for that F at the end
                {
                    // subtracting i as to not skip strings
                    spaces.remove(i--);

//                    Log.d(TAG, "FOUND DIGITPTRN: "+ str);

                }

                if(str.matches("[\\d]") && (++numInts > 1))
                {
                    lines.remove(idx);
                    --idx;

                    continue LINES;

                }

            }

//            for(String s : spaces)
//                Log.d(TAG, "AFTER CLEAR: " + s);

            StringBuilder sb = new StringBuilder();
            String sep = "";
            for(String s: spaces) {
                sb.append(sep).append(s);
                sep = " ";


            }

            str = sb.toString().trim();

            if(!str.equals(""))
                items.add(str);
        }

    }

    public void addPrices(String pricesIn) {


        Log.d(TAG, "in addPrices()");

        Log.d(TAG, pricesIn);

        ArrayList<String> lines = new ArrayList<>(Arrays.asList(pricesIn.split("\n")));

        String str;

        for (int idx = 0; idx < lines.size(); ++idx) {

            str = lines.get(idx);

            Log.d("PRICECHECK", "PRICE WAS: " + str);

            str = str.replaceAll("[ ]*[0-9]{12,}[ ]*", "")
                    .replaceAll("[A-Za-z][ ]*[$]", "")
                    .replaceAll("[A-Za-z]", "")
                    .replaceAll("[$]", "")
                    .replaceAll("[%]", "")
                    .replaceAll("[ ]", "");


            Log.d("PRICECHECK", "PRICE BECOMING: " + str);

            DecimalFormat df = new DecimalFormat("#.00");

            Float fl;
            try {

                fl = Float.parseFloat(str);
                df.format(fl);
                Log.d("PRICECHECK", "PRICE IS: " + String.valueOf(fl));

                prices.add(fl);
            }
            catch (Exception e){
                Log.w("PRICECHECK", "Can't convert passed string to float");

            }




        }

    }

    public void finalize(){


    }

    public String getReceiptImagePath() { return receiptImagePath; }

    public String getCurrUserToken() { return currUserToken; }

    public String getCurrUser() { return currUser; }

    public String getStoreName() {
        return storeName;
    }

    public String getDateOfCapture() {
        return dateOfCapture;
    }


    public ArrayList<String> getItems() {
        return items;
    }

    public ArrayList<Float> getPrices() {
        return prices;
    }

    public String printItems(){

        StringBuilder sb = new StringBuilder();
        String sep = "";
        for(String s : items){
            sb.append(sep).append(s);
            sep = " \n";

        }

        return sb.toString().trim();
    }


    public String printPrices(){

        StringBuilder sb = new StringBuilder();
        String sep = "";

        for(Float fl : prices) {
            sb.append(sep).append(String.valueOf(fl));
            sep = " \n";
        }

        return sb.toString().trim();
    }

    public int numItems(){

        return items.size();
    }

    public int numPrices(){

        return prices.size();
    }
}