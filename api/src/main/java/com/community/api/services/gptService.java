package com.community.api.services;

import com.community.api.entity.CustomProduct;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class gptService {

    private static final String API_KEY = "sk-...YbcA"; // Use environment variable in prod
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";

    public String getUpdateSummary(JsonObject customProductOld, JsonObject customProductOld1) throws IOException {
        OkHttpClient client = new OkHttpClient();

        String prompt = "Compare the following two product JSONs and summarize the updates or changes made. Highlight key differences in a user-friendly format.\n"
                + "Old Product:\n" + new ObjectMapper().writeValueAsString(customProductOld)
                + "\nNew Product:\n" + new ObjectMapper().writeValueAsString(customProductOld1);

        String requestBody = new ObjectMapper().writeValueAsString(Map.of(
                "model", "gpt-4o",
                "messages", List.of(
                        Map.of("role", "system", "content", "You are a helpful assistant."),
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.7
        ));

        Request request = new Request.Builder()
                .url(API_URL)
                .post(RequestBody.create(requestBody, MediaType.parse("application/json")))
                .addHeader("Authorization", "Bearer " + API_KEY)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            JsonNode responseBody = new ObjectMapper().readTree(response.body().string());
            return responseBody.get("choices").get(0).get("message").get("content").asText();
        }
    }
}
