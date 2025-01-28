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
    @Override
    public Date deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        String date = jp.getText();
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            dateFormat.setLenient(false);

            // First check if the format matches yyyy-MM-dd
            if (!date.matches("\\d{4}-\\d{2}-\\d{2}")) {
                setValidationState(0);  // Invalid format
                return new Date();
            }

            String[] dateParts = date.split("-");
            int month = Integer.parseInt(dateParts[1]);
            if (month < 1 || month > 12) {
                setValidationState(-2);  // Invalid month
                return new Date();
            }

            // Try to parse the date
            try {
                Date parsedDate = dateFormat.parse(date);
                setValidationState(1);  // Valid date
                return parsedDate;
            } catch (ParseException e) {
                setValidationState(-1);  // Invalid date (correct format but invalid value)
                return new Date();
            }
        } catch (Exception e) {
            setValidationState(0);  // Any other parsing error treated as format error
            return new Date();
        }
    }

    public static int validationState;

    public void setValidationState(int state) {
        this.validationState = state;
    }
}