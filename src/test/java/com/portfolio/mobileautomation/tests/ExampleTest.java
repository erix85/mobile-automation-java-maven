package com.portfolio.mobileautomation.tests;

import com.portfolio.mobileautomation.driver.DriverManager;
import com.portfolio.mobileautomation.listeners.AllureTestListener;
import com.portfolio.mobileautomation.retry.RetryAnalyzer;
import com.portfolio.mobileautomation.server.AppiumServerManager;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.testng.Assert.assertTrue;

@Listeners(AllureTestListener.class)
public class ExampleTest {

    private static final Logger logger = LoggerFactory.getLogger(ExampleTest.class);
    private AppiumDriver driver;

    @BeforeMethod
    public void setup() {
        logger.info("Setting up test environment.");
        AppiumServerManager.startAppiumServer();
        DriverManager.initDriver();
        driver = DriverManager.getDriver();
    }

    @Test(retryAnalyzer = RetryAnalyzer.class, description = "A simple example test to verify app launch")
    public void verifyAppLaunch() {
        logger.info("Starting verifyAppLaunch test.");
        // Example: Verify a text element on the launched app
        // Replace with an actual element from your app
        By welcomeText = By.id("com.ejemplo.app:id/welcome_text"); // Assuming your app has a welcome text with this ID
        assertTrue(driver.findElement(welcomeText).isDisplayed(), "Welcome text should be displayed.");
        logger.info("App launched and welcome text displayed successfully.");
    }

    @AfterMethod
    public void teardown() {
        logger.info("Tearing down test environment.");
        DriverManager.quitDriver();
        AppiumServerManager.stopAppiumServer();
    }
}
