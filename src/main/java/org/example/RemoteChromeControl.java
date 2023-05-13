package org.example;

import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;


import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class RemoteChromeControl {
    private static final Logger LOGGER = Logger.getLogger(RemoteChromeControl.class.getName());

    private static void randomWait(WebDriver driver) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        wait.until(ExpectedConditions.jsReturnsValue("return document.readyState === 'complete'"));
        try {
            Thread.sleep((long) (Math.random() * (7000 - 3000)) + 3000);
        } catch (InterruptedException e) {
            LOGGER.warning("Thread interrupted during random wait");
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) {
        String os = System.getProperty("os.name").toLowerCase();
        String chromeDriverPath = os.contains("win") ? "windows/chromedriver.exe" : os.contains("linux") ? "chromedriver_linux64/chromedriver" : null;
        if (chromeDriverPath == null) throw new RuntimeException("Unsupported OS: " + os);

        GUI login = new GUI();
        String[] credentials = login.getLoginCredentials();
        String targetProfile = login.getSearch();

        LOGGER.info("Starting Chrome driver");
        System.setProperty("webdriver.chrome.driver", chromeDriverPath);

        ChromeOptions options = new ChromeOptions();
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("profile.default_content_setting_values.notifications", 2);
        options.setExperimentalOption("prefs", prefs);
        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        try {
            LOGGER.info("Navigating to Facebook login page");
            driver.get("https://www.facebook.com");
            randomWait(driver);

            LOGGER.info("Attempting to accept cookies");
            driver.manage().addCookie(new Cookie("cookiesAgreed", "true"));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[contains(., 'alla cookies') or contains(., 'all cookies')]")));

            LOGGER.info("Clicking the 'Accept' button in the cookies popup");
            driver.findElement(By.xpath("//button[contains(., 'alla cookies') or contains(., 'all cookies')]")).click();
            randomWait(driver);

            driver.findElement(By.id("email")).sendKeys(credentials[0]);
            driver.findElement(By.id("pass")).sendKeys(credentials[1]);
            randomWait(driver);

            LOGGER.info("Logging in");
            driver.findElement(By.name("login")).click();
            randomWait(driver);

            String currentUrl = driver.getCurrentUrl();
            if (currentUrl.contains("https://www.facebook.com/login/?privacy_mutation_token=")) {
                LOGGER.severe("Failed to log in. Wrong username or password.");
                throw new Exception("Failed to log in. Wrong username or password.");
            }

        //Doing the search

        driver.navigate().to("https://www.facebook.com/search/top/?q="+targetProfile);
        randomWait(driver);
        LOGGER.info("Showing search results");


       LOGGER.info("Navigating to profile");
            driver.findElement(By.cssSelector("svg[aria-label='Din profil'], svg[aria-label='Your profile']")).click();
            randomWait(driver);

            LOGGER.info("Logging out");
            driver.findElement(By.xpath("//*[contains(text(),'Logga ut') or contains(text(),'Log Out')][1]")).click();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "An error occurred while executing the script", e);
        } finally {
            driver.quit();
        }
    }
}
