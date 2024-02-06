package com.yury.trade.tradeHelper.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class TradierService {

    public JsonNode history(String symbol, String start, String end) throws IOException {

        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("symbol", symbol);
        paramsMap.put("interval", "daily");
        paramsMap.put("start", start);
        paramsMap.put("end", end);

        HttpUriRequest request = getUriRequest("history", getHeaders(), paramsMap);

        return callService(request);
    }

    private JsonNode callService(HttpUriRequest request) throws IOException {
        final HttpResponse response = HttpClientBuilder.create().build().execute(request);
        final String jsonString = EntityUtils.toString(response.getEntity());

        return new ObjectMapper().readTree(jsonString);
    }

    private Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer 9oZlNnSug7EurOqxW1GPEnCdAVAU");
        headers.put("Accept", "application/json");
        return headers;
    }

    private HttpUriRequest getUriRequest(String url, Map<String, String> headers, Map<String, String> parameters) {
        RequestBuilder requestBuilder = RequestBuilder.get("https://api.tradier.com/v1/markets/" + url);

        for (Map.Entry<String, String> header : headers.entrySet()) {
            requestBuilder.addHeader(header.getKey(), header.getValue());
        }

        for (Map.Entry<String, String> parameter : parameters.entrySet()) {
            requestBuilder.addParameter(parameter.getKey(), parameter.getValue());
        }

        return requestBuilder.build();
    }

}
