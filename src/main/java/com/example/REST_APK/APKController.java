package com.example.REST_APK;

import org.apache.poi.xssf.usermodel.*;
import org.apache.tomcat.util.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
public class APKController {

    @GetMapping("/get-info")
    @ResponseBody
    @Autowired(required = false)
    public void getUnpOrPersNum(@RequestParam(value = "unp", required = false) String unp,
                                  @RequestParam(value = "personalNumber", required = false) String personalNumber) throws IOException, JSONException, ParseException {

        System.out.println("unp = " + unp + " pers = " + personalNumber);
         getToken(personalNumber, unp);

    }

    private void getToken(String personalNumber, String unp) throws IOException, JSONException, ParseException {

        URL url = new URL("https://gw.gov.by/api/token?scope=application&grant_type=client_credentials");

        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Accept", "application/json");
        con.setDoOutput(true);
        con.setRequestProperty("Authorization", "Bearer " + "9f6cfaef-0579-38ad-8237-37c2179a589e");

        String auth = "3cKipt2VvrdJkLE8cNdbgYw5hYYa" + ":" + "XyUyxv6_qVdxib6ccUu8QgyVgpga";
        byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.UTF_8));
        String authHeaderValue = "Basic " + new String(encodedAuth);
        con.setRequestProperty("Authorization", authHeaderValue);

        System.out.println("Status Code: " + con.getResponseCode());

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

        if (expires_in > 0) {
            if (unp == null) {
                searchInfoPerson(personalNumber, access_token);
            } else if (personalNumber == null) {
                searchInfoOrg(unp, access_token);
            }
        }

    }

    private void searchInfoPerson(String personalNumber, String access_token) throws IOException, JSONException, ParseException {

        URL url = new URL("https://gw.gov.by/api/account/1.0.0/umgt/account/v1/search");

        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");
        con.setDoOutput(true);
        System.out.println(access_token);
        con.setRequestProperty("Authorization", "Bearer " + access_token);

        String jsonInputString = "{\n" +
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

        System.out.println("Status Code: " + con.getResponseCode());

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

//        String account_type = "p";

        for (int i = 0; i < rates_object.length(); i++) {
            //rates_object.getJSONObject(i);

            if (rates_object.getJSONObject(i).getJSONObject("accountType").getString("accountType").equals("p") == true) {
                System.out.println("\nЗапись №" + i);
                String id = rates_object.getJSONObject(i).getString("id");
                System.out.println("id = " + id);
                String lastName = rates_object.getJSONObject(i).getString("lastName");
                System.out.println("name = " + lastName);
                String firstName = rates_object.getJSONObject(i).getString("firstName");
                System.out.println("firstName = " + firstName);
                String middleName = rates_object.getJSONObject(i).getString("middleName");
                System.out.println("middleName = " + middleName);
                transactionInfoPerson(id, access_token, lastName, firstName, middleName);
            }
        }

    }

    private void searchInfoOrg(String unp, String access_token) throws IOException, JSONException, ParseException {

        URL url = new URL("https://gw.gov.by/api/org-organization/1.0.0/org/organization/v1/search/x-short");

        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");
        con.setDoOutput(true);
        System.out.println(access_token);
        con.setRequestProperty("Authorization", "Bearer " + access_token);

        String jsonInputString = "{\n" +
                "       \"filter\": {\n" +
                "        \"unp\": [\n" +
                "            {\n" +
                "                \"value\":  \"" + unp + "\",\n" +
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

        System.out.println("Status Code: " + con.getResponseCode());

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
//        JSONArray rates_object2 = new JSONArray(rates_object.getJSONArray("data").toString());
        //System.out.println("data -" + rates_object);

        for (int i = 0; i < rates_object.length(); i++) {
            rates_object.getJSONObject(i);

            System.out.println("\nЗапись №" + i);
            String id = rates_object.getJSONObject(i).getString("id");
            System.out.println("id = " + id);
            String fullName = rates_object.getJSONObject(0).getJSONObject("fullName").getString("ru");
            System.out.println("fullName = " + fullName);
            transactionInfoOrg(id, access_token, fullName, unp);
        }
    }


    private String transactionInfoPerson(String accountId, String access_token, String lastName, String firstName, String middlleName) throws IOException, JSONException, ParseException {

        URL url = new URL("https://gw.gov.by/api/billing-transaction/1.0.0/billing/transaction/v1/search");

        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");
        con.setDoOutput(true);
        con.setRequestProperty("Authorization", "Bearer " + access_token);

        String jsonInputString = "{\n" +
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

        System.out.println("Status Code: " + con.getResponseCode());

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

        int rowLast = 0;

        InputStream is = getClass().getClassLoader().getResourceAsStream("Operation.xlsx");
        XSSFWorkbook xssfWorkbook = new XSSFWorkbook(is);

        for (int i = 0; i < rates_object.length(); i++) {
//
            String transactionTime = rates_object.getJSONObject(i).getString("transactionTms");

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            SimpleDateFormat output = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date d = sdf.parse(transactionTime);
            String formattedTime = output.format(d);

            String operationType = rates_object.getJSONObject(i).getString("operationType");
            String svcCode = rates_object.getJSONObject(i).getString("svcCode");

            String paymentMethodType = rates_object.getJSONObject(i).getString("paymentMethodType");
            String orderNumber = rates_object.getJSONObject(i).getString("orderNumber");
            String amount = rates_object.getJSONObject(i).getString("amount");
            String balanceAfter = rates_object.getJSONObject(i).getString("balanceAfter");
            String balanceBefore = rates_object.getJSONObject(i).getString("balanceBefore");

            XSSFCellStyle cellStyle = xssfWorkbook.createCellStyle();
            XSSFSheet sheet = xssfWorkbook.getSheetAt(0);
            cellStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
            cellStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
            cellStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);
            cellStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
            cellStyle.setWrapText(true);

            XSSFFont font = xssfWorkbook.createFont();
            font.setFontHeightInPoints((short) 12);
            font.setFontName("Times New Roman");
            cellStyle.setFont(font);

            rowLast = sheet.getLastRowNum();
            XSSFRow row = sheet.createRow(rowLast + 1);

            XSSFCell transactionTmsCell = row.createCell(0);
            transactionTmsCell.setCellStyle(cellStyle);
            transactionTmsCell.setCellValue(String.valueOf(formattedTime));
            sheet.autoSizeColumn(0);

            XSSFCell svcCodeCell = row.createCell(1);
            svcCodeCell.setCellStyle(cellStyle);
            svcCodeCell.setCellValue(svcCode);
            sheet.autoSizeColumn(1);

            XSSFCell fillNameCell = row.createCell(2);
            fillNameCell.setCellStyle(cellStyle);
            fillNameCell.setCellValue(lastName + " " + firstName + " " + middlleName);
            sheet.autoSizeColumn(2);

            XSSFCell userCell = row.createCell(4);
            userCell.setCellStyle(cellStyle);
            userCell.setCellValue("PERSON");

            XSSFCell operationTypeCell = row.createCell(5);
            operationTypeCell.setCellStyle(cellStyle);
            operationTypeCell.setCellValue(operationType);

            XSSFCell paymentMethodTypeCell = row.createCell(6);
            paymentMethodTypeCell.setCellStyle(cellStyle);
            paymentMethodTypeCell.setCellValue(paymentMethodType);

            XSSFCell orderNumberCell = row.createCell(7);
            orderNumberCell.setCellStyle(cellStyle);
            orderNumberCell.setCellValue(orderNumber);

            XSSFCell amountCell = row.createCell(8);
            amountCell.setCellStyle(cellStyle);
            amountCell.setCellValue(amount);
            sheet.autoSizeColumn(8);

            XSSFCell balanceAfterCell = row.createCell(9);
            balanceAfterCell.setCellStyle(cellStyle);
            balanceAfterCell.setCellValue(balanceAfter);
            sheet.autoSizeColumn(9);

            XSSFCell balanceBeforeCell = row.createCell(10);
            balanceBeforeCell.setCellStyle(cellStyle);
            balanceBeforeCell.setCellValue(balanceBefore);
            sheet.autoSizeColumn(10);

        }


        File tempFile = File.createTempFile("123", ".xlsx");

        try (OutputStream fileOut = Files.newOutputStream(tempFile.toPath())) {
            xssfWorkbook.write(fileOut);
        }

        System.out.println(ConBase64.convert(tempFile));
        return ConBase64.convert(tempFile);
    }


    private String transactionInfoOrg(String orgId, String access_token, String fullName, String unp) throws IOException, JSONException, ParseException {

        URL url = new URL("https://gw.gov.by/api/billing-transaction/1.0.0/billing/transaction/v1/search/x-org");

        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");
        con.setDoOutput(true);
        con.setRequestProperty("Authorization", "Bearer " + access_token);

        String jsonInputString = "{\n" +
                "       \"filter\": {\n"+
                "        \"orgId\":[\n" +
                "            {\n" +
                "                \"value\":\"" + orgId + "\",\n" +
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

        System.out.println("Status Code: " + con.getResponseCode());

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

        int rowLast = 0;

        InputStream is = getClass().getClassLoader().getResourceAsStream("Operation.xlsx");
        XSSFWorkbook xssfWorkbook = new XSSFWorkbook(is);

        for (int i = 0; i < rates_object.length(); i++) {
//
            String transactionTime = rates_object.getJSONObject(i).getString("transactionTms");

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            SimpleDateFormat output = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date d = sdf.parse(transactionTime);
            String formattedTime = output.format(d);

            String operationType = rates_object.getJSONObject(i).getString("operationType");
            String svcCode = rates_object.getJSONObject(i).getString("svcCode");

            String paymentMethodType = rates_object.getJSONObject(i).getString("paymentMethodType");
            String orderNumber = rates_object.getJSONObject(i).getString("orderNumber");
            String amount = rates_object.getJSONObject(i).getString("amount");
            String balanceAfter = rates_object.getJSONObject(i).getString("balanceAfter");
            String balanceBefore = rates_object.getJSONObject(i).getString("balanceBefore");

            XSSFCellStyle cellStyle = xssfWorkbook.createCellStyle();
            XSSFSheet sheet = xssfWorkbook.getSheetAt(0);
            cellStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
            cellStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
            cellStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);
            cellStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
            cellStyle.setWrapText(true);

            XSSFFont font = xssfWorkbook.createFont();
            font.setFontHeightInPoints((short) 12);
            font.setFontName("Times New Roman");
            cellStyle.setFont(font);

            rowLast = sheet.getLastRowNum();
            XSSFRow row = sheet.createRow(rowLast + 1);

            XSSFCell transactionTmsCell = row.createCell(0);
            transactionTmsCell.setCellStyle(cellStyle);
            transactionTmsCell.setCellValue(String.valueOf(formattedTime));
            sheet.autoSizeColumn(0);

            XSSFCell svcCodeCell = row.createCell(1);
            svcCodeCell.setCellStyle(cellStyle);
            svcCodeCell.setCellValue(svcCode);
            sheet.autoSizeColumn(1);

            XSSFCell fillNameCell = row.createCell(2);
            fillNameCell.setCellStyle(cellStyle);
            fillNameCell.setCellValue(fullName);
            sheet.autoSizeColumn(2);

            XSSFCell unpCell = row.createCell(3);
            unpCell.setCellStyle(cellStyle);
            unpCell.setCellValue(unp);
            sheet.autoSizeColumn(3);

            XSSFCell userCell = row.createCell(4);
            userCell.setCellStyle(cellStyle);
            userCell.setCellValue("ORG");

            XSSFCell operationTypeCell = row.createCell(5);
            operationTypeCell.setCellStyle(cellStyle);
            operationTypeCell.setCellValue(operationType);

            XSSFCell paymentMethodTypeCell = row.createCell(6);
            paymentMethodTypeCell.setCellStyle(cellStyle);
            paymentMethodTypeCell.setCellValue(paymentMethodType);

            XSSFCell orderNumberCell = row.createCell(7);
            orderNumberCell.setCellStyle(cellStyle);
            orderNumberCell.setCellValue(orderNumber);
            sheet.autoSizeColumn(7);


            XSSFCell amountCell = row.createCell(8);
            amountCell.setCellStyle(cellStyle);
            amountCell.setCellValue(amount);
            sheet.autoSizeColumn(8);

            XSSFCell balanceAfterCell = row.createCell(9);
            balanceAfterCell.setCellStyle(cellStyle);
            balanceAfterCell.setCellValue(balanceAfter);
            sheet.autoSizeColumn(9);

            XSSFCell balanceBeforeCell = row.createCell(10);
            balanceBeforeCell.setCellStyle(cellStyle);
            balanceBeforeCell.setCellValue(balanceBefore);
            sheet.autoSizeColumn(10);

        }

        File tempFile = File.createTempFile("123", ".xlsx");

        try (OutputStream fileOut = Files.newOutputStream(tempFile.toPath())) {
            xssfWorkbook.write(fileOut);
        }

        System.out.println(ConBase64.convert(tempFile));
        return ConBase64.convert(tempFile);
    }

}





