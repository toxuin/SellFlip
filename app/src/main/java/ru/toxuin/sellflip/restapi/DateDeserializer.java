package ru.toxuin.sellflip.restapi;

import android.util.Log;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateDeserializer implements JsonDeserializer<Date> {

    private static final String SERVER_TIME_OFFSET = "+0000";
    private static final String TAG = "DATE_DESERILAIZER";

    @Override
    public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        String date = json.getAsString();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);

        try {
            return formatter.parse(date.replaceAll("Z$", SERVER_TIME_OFFSET));
        } catch (ParseException e) {
            Log.e(TAG, "Failed to parse Date due to:", e);
            return null;
        }
    }
}
