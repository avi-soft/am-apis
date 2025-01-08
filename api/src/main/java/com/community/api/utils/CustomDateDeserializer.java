package com.community.api.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CustomDateDeserializer extends JsonDeserializer<Date> {
   public static boolean isValidDate= true;
    @Override
    public Date deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        String date = jp.getText();
        try {
            // Just store the string value as a Date for now - validation will happen later
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            dateFormat.setLenient(false);
            isValidDate(true);
            return dateFormat.parse(date);
        } catch (ParseException e) {
            // Return a default date to allow the request to proceed
            // The actual validation will happen in the service layer
            isValidDate(false);
            return new Date();
        }
    }

    public boolean isValidDate(boolean value)
    {
        isValidDate= value;
        return isValidDate;
    }
}