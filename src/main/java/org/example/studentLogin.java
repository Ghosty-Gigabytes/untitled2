package org.example;

import jep.SharedInterpreter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

import static org.example.tesseractJEP.captchaExtract;

public class studentLogin {
    private final HttpClient httpClient;
    private HttpRequest httpRequest;
    private HttpResponse<String> httpResponse;
    private CookieManager cookieManager;
    private String username;
    private String password;
    private final int maxRetries = 5;

    public studentLogin() throws IOException, InterruptedException {
        cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        httpClient = HttpClient.newBuilder()
                .cookieHandler(cookieManager)
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();
        httpRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://imsnsit.org/imsnsit"))
                .setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.6099.119 Safari/537.36")
                .setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
                .setHeader("Accept-Language", "en-US,en;q=0.5")
                //.setHeader("Connection", "keep-alive")
                .setHeader("Upgrade-Insecure-Requests", "1")
                .setHeader("Sec-Fetch-Dest", "document")
                .setHeader("Sec-Fetch-Mode", "navigate")
                .setHeader("Sec-Fetch-Site", "same-origin")
                .GET()
                .build();
        httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        this.authPage();
    }

    protected void authPage() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://imsnsit.org/imsnsit/student_login110.php"))
                .setHeader("Referer", "https://www.imsnsit.org/imsnsit/student_login.php")
                .setHeader("Content-Type", "application/x-www-form-urlencoded")
                .setHeader("Origin", "https://www.imsnsit.org")
                .setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.6099.119 Safari/537.36")
                .setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
                .setHeader("Accept-Language", "en-US,en;q=0.5")
                //.setHeader("Connection", "keep-alive")
                .setHeader("Upgrade-Insecure-Requests", "1")
                .setHeader("Sec-Fetch-Dest", "document")
                .setHeader("Sec-Fetch-Mode", "navigate")
                .setHeader("Sec-Fetch-Site", "same-origin")
                .GET()
                .build();
        httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        //System.out.println(httpResponse.body());

        //System.out.println(captchaAnswer);
        this.login(captchaExtract(httpResponse), hrandFetcher(httpResponse));
    }

    private String hrandFetcher(HttpResponse<String> httpResponse) throws IOException {
        Document document = Jsoup.parse((String) httpResponse.body());
        Elements hrandElement = document.selectXpath("/html/body/form/table/tbody/tr[5]/td/font/input[2]");
        //System.out.println(hrand);
        return hrandElement.getFirst().attr("value");
    }

    private void login(String captchaAnswer, String hrand) throws IOException, InterruptedException {
        this.credentialfetcher();
        HttpResponse<String> authResponse;
        authResponse = this.auth(captchaAnswer, hrand);
        int attempt = 0;
        while (attempt <= maxRetries) {
            String body = Jsoup.parse(authResponse.body()).text();
            if (body.contains("HRAND") || body.contains("Please do not share your password")) {
                if (body.contains("Invalid Security Number")) {
                    System.out.println("Invalid CAPTCHA, trying again...");
                }
                if (body.contains("You are not authorized") || body.contains("Invalid password") || body.contains("Your password does not match")) {
                    System.out.println("Check your username and password, please try again...");
                    this.credentialfetcher();

                } else {
                    System.out.println("Login Failed, unknown error, trying again...");
                }
            } else {
                System.out.println("Auth Success");
                break;
            }
            //System.out.println("Auth failed (attempt " + (attempt + 1) + "), re-authenticating...");
            authResponse = this.auth(captchaExtract(httpResponse), hrandFetcher(httpResponse));
            attempt++;
        }
        if (attempt > maxRetries) {
            throw new RuntimeException("Max retries reached (" + maxRetries + "), auth still failing.");
        }

    }

    private void credentialfetcher() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Please enter your username : ");
        username = sc.nextLine();
        System.out.println();
        System.out.print("Please enter your password : ");
        password = sc.nextLine();
        System.out.println();
        if (username.isBlank() || password.isBlank()) {
            System.out.println("Empty Credentials Error");
            this.credentialfetcher();
        }
    }

    private HttpResponse<String> auth(String captchaAnswer, String hrand) throws IOException, InterruptedException {
        Map<String, String> data = Map.of(
                "f", "",
                "uid", username,
                "pwd", password,
                "HRAND_NUM", hrand,
                "fy", "2024-25",
                "comp", "NETAJI SUBHAS UNIVERSITY OF TECHNOLOGY",
                "cap", captchaAnswer,
                "logintype", "student"
        );
        String formBody = data.entrySet().stream()
                .map(entry -> URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8) + "=" +
                        URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));

        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofString(formBody);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://imsnsit.org/imsnsit/student_login.php"))
                .setHeader("Referer", "https://www.imsnsit.org/imsnsit/student_login.php")
                .setHeader("Content-Type", "application/x-www-form-urlencoded")
                .setHeader("Origin", "https://www.imsnsit.org")
                .setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.6099.119 Safari/537.36")
                .setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
                .setHeader("Accept-Language", "en-US,en;q=0.5")
                //.setHeader("Connection", "keep-alive")
                .setHeader("Upgrade-Insecure-Requests", "1")
                .setHeader("Sec-Fetch-Dest", "frame")
                .setHeader("Sec-Fetch-Mode", "navigate")
                .setHeader("Sec-Fetch-Site", "same-origin")
                .POST(bodyPublisher)
                .build();
        httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        //System.out.println(httpResponse.body().toString());
        return httpResponse;
    }



}
