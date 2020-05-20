package ch.m4th1eu.stan;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Functions {

    public static String time() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

        return "Il est " + sdf.format(cal.getTime());
    }

    public static String meteo() {
        try {
            String string = Utils.readUrl(new URL("https://www.prevision-meteo.ch/services/json/bussigny-sur-oron"));
            JsonObject convertedObject = new Gson().fromJson(string, JsonObject.class);

            String cityName = String.valueOf(convertedObject.getAsJsonObject("city_info").get("name")).replaceAll("\"", "");
            String condition = String.valueOf(convertedObject.getAsJsonObject("current_condition").get("condition")).replaceAll("\"", "");
            String temp = String.valueOf(convertedObject.getAsJsonObject("current_condition").get("tmp")).replaceAll("\"", "");


            return "Le temps est " + condition + ", il fait actuellement " + temp + " degrés à " + cityName;

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return null;
    }
}
