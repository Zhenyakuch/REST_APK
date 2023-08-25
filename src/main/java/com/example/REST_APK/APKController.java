package com.example.REST_APK;

import org.apache.tomcat.util.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@RestController
public class APKController {

    @GetMapping("/get-info")
    @ResponseBody
    public String getUnpOrPersNum(@RequestParam String unp,
                                  @RequestParam String personalNumber) throws IOException, JSONException {

        System.out.println("unp = " + unp + " pers = " + personalNumber);
        
      // return searchInfoPerson(personalNumber);
        return getToken(personalNumber);

    }

    private String getToken(String personalNumber) throws IOException, JSONException {

        URL url = new URL("https://gw.gov.by/api/token?scope=application&grant_type=client_credentials");

        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
       // con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");
        con.setDoOutput(true);
        con.setRequestProperty("Authorization","Bearer "+"9f6cfaef-0579-38ad-8237-37c2179a589e");

        String auth = "3cKipt2VvrdJkLE8cNdbgYw5hYYa" + ":" + "XyUyxv6_qVdxib6ccUu8QgyVgpga";
        byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.UTF_8));
        String authHeaderValue = "Basic " + new String(encodedAuth);
        con.setRequestProperty("Authorization", authHeaderValue);

        System.out.println("Status Code: "+con.getResponseCode());

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        //print in String
        System.out.println(response.toString());
        //Read JSON response and print
        JSONObject myResponse = new JSONObject(response.toString());
        System.out.println("result after Reading JSON Response");

//        if( con.getResponseCode()==200)
//        {
//            success = true;
//        }
            int expires_in = myResponse.getInt("expires_in");
            String access_token = myResponse.getString("access_token");

            if(expires_in>0){
                searchInfoPerson(personalNumber, access_token);
            }


        return null;
    }

    private String searchInfoPerson(String personalNumber, String access_token) throws IOException, JSONException {

        URL url = new URL("https://gw.gov.by/api/account/1.0.0/umgt/account/v1/search");

        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");
        con.setDoOutput(true);
        System.out.println( access_token);
        con.setRequestProperty("Authorization","Bearer "+ access_token);

        String jsonInputString =  "{\n" +
                "       \"filter\": {\n" +
                "        \"personalNumber\": [\n" +
                "            {\n" +
                "                \"value\":  \"" + personalNumber + "\",\n" +
                "                \"operation\": \"equals\"\n" +
                "            }\n" +
                "        ]\n" +
                "    }\n" +
                "}\n" +
                "\n";

        //System.out.println(jsonInputString);
        try (OutputStream os = con.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        System.out.println("Status Code: "+con.getResponseCode());

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        //print in String
        System.out.println(response.toString());
        //Read JSON response and print
        JSONObject myResponse = new JSONObject(response.toString());
        System.out.println("result after Reading JSON Response");

        JSONArray rates_object = new JSONArray(myResponse.getJSONArray("data").toString());
        //System.out.println("data -" + rates_object);

        for(int i = 0;  i < rates_object.length(); i++)
        {
            rates_object.getJSONObject(i);

            System.out.println("\nЗапись №"+i);
            String id = rates_object.getJSONObject(i).getString("id");
            System.out.println("id = " + id);
            String name = rates_object.getJSONObject(i).getString("lastName");
            System.out.println("name = " + name);
            String firstName = rates_object.getJSONObject(i).getString("firstName");
            System.out.println("firstName = " + firstName);
            String middleName = rates_object.getJSONObject(i).getString("middleName");
            System.out.println("middleName = " + middleName);
            transactionInfoPerson(id, access_token);
        }


       return null;
    }


    private String transactionInfoPerson(String accountId, String access_token) throws IOException, JSONException {

        URL url = new URL("https://gw.gov.by/api/billing-transaction/1.0.0/billing/transaction/v1/search");

        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");
        con.setDoOutput(true);
        con.setRequestProperty("Authorization","Bearer "+ access_token);

        String jsonInputString =  "{\n" +
                "       \"filter\": {\n" +
                "        \"paymentMethodType\": [\n" +
                "            {\n" +
                "                \"value\": \"CORRECTION\",\n" +
                "                \"operation\": \"equals\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"accountId\":[\n" +
                "            {\n" +
                "                \"value\":\"" + accountId + "\",\n" +
                "               \"operation\": \"equals\"\n" +
                "            }\n" +
                "            ]\n" +
                "    }\n" +
                "}\n}\n" +
                "\n";

       // System.out.println(jsonInputString);
        try (OutputStream os = con.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        System.out.println("Status Code: "+con.getResponseCode());

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        //print in String
        System.out.println(response.toString());
        //Read JSON response and print
        JSONObject myResponse = new JSONObject(response.toString());
        //System.out.println("result after Reading JSON Response");

        JSONArray rates_object = new JSONArray(myResponse.getJSONArray("data").toString());
       // System.out.println("data -" + rates_object);


        return null;
    }
}





