package com.dragotrade.dragotrade.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Patterns;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validation {

    public static boolean isEmailValid(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isPasswordValid(String password) {
        return password.length() >= 6;
    }

    public static boolean comparePassword(String password, String confirmPassword) {
        return password.equals(confirmPassword);
    }

    public static boolean isValidUsername(String name) {
        //        String regex = "^[A-Za-z]\\w{5,29}$";
        String regex = "^[\\p{L} .'-]+$";
        Pattern p = Pattern.compile(regex);
        if (name == null) {
            return false;
        }
        Matcher m = p.matcher(name);
        return m.matches();
    }

    public static boolean isConnectingToInternet(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null)
                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    return true;
                }
        }
        return false;
    }

//    public static boolean validateUsing_libphonenumber(String countryCode, String phNumber, Context context) {
//        if (!(String.valueOf(phNumber.charAt(0)).equals("0"))) {
//            PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.createInstance(context);
//            String isoCode = phoneNumberUtil.getRegionCodeForCountryCode(Integer.parseInt(countryCode));
//            Phonenumber.PhoneNumber phoneNumber = null;
//            try {
//                phoneNumber = phoneNumberUtil.parse(phNumber, isoCode);
//            } catch (NumberParseException e) {
//                System.err.println(e);
//            }
//            return phoneNumberUtil.isValidNumber(phoneNumber);
//        } else {
//            return false;
//        }
//    }


    @SuppressLint("SimpleDateFormat")
    public static String getCurrentDateAndTime() {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(Calendar.getInstance().getTime());
    }

    public static String expiryPlanDate(){
        String dt = getCurrentDateAndTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        try {
            c.setTime(sdf.parse(dt));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        c.add(Calendar.DATE, 60);  // number of days to add, can also use Calendar.DAY_OF_MONTH in place of Calendar.DATE
        SimpleDateFormat sdf1 = new SimpleDateFormat("dd/MM/yyyy");
        return sdf1.format(c.getTime());
    }
}
