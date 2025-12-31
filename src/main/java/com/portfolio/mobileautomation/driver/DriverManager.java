package com.portfolio.mobileautomation.driver;

import com.portfolio.mobileautomation.config.FrameworkConfig;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import org.aeonbits.owner.ConfigFactory;
import org.openqa.selenium.Dimension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

public final class DriverManager {

    private static final Logger logger = LoggerFactory.getLogger(DriverManager.class);
    private static final ThreadLocal<AppiumDriver> DRIVER_THREAD_LOCAL = new ThreadLocal<>();
    private static final FrameworkConfig CONFIG = ConfigFactory.create(FrameworkConfig.class);

    private DriverManager() {
        // Private constructor to prevent instantiation
    }

    public static void initDriver() {
        if (DRIVER_THREAD_LOCAL.get() == null) {
            logger.info("Initializing Appium Driver...");
            try {
                UiAutomator2Options options = getUiAutomator2Options();
                AppiumDriver driver = new AndroidDriver(new URL(CONFIG.appiumUrl()), options);
                DRIVER_THREAD_LOCAL.set(driver);
                logger.info("Appium Driver initialized successfully.");

                // Log device screen size
                Dimension size = getDriver().manage().window().getSize();
                logger.info("Device screen size: {}x{}", size.width, size.height);

            } catch (MalformedURLException e) {
                logger.error("Invalid Appium URL: {}", CONFIG.appiumUrl(), e);
                throw new RuntimeException("Invalid Appium URL", e);
            } catch (Exception e) {
                logger.error("Failed to initialize Appium Driver", e);
                throw new RuntimeException("Failed to initialize Appium Driver", e);
            }
        }
    }

    private static UiAutomator2Options getUiAutomator2Options() {
        UiAutomator2Options options = new UiAutomator2Options();
        options.setPlatformName(CONFIG.platformName());
        options.setAutomationName(CONFIG.automationName());
        options.setDeviceName(CONFIG.emulatorName());
        options.setPlatformVersion(CONFIG.platformVersion());
        options.setApp(CONFIG.appPath());
        options.setAppPackage(CONFIG.appPackage());
        options.setAppActivity(CONFIG.appActivity());
        options.setNoReset(CONFIG.noReset());
        options.setFullReset(CONFIG.fullReset());

        // Device Factory: Start AVD if not running
        options.setAvd(CONFIG.emulatorName());
        options.setAvdLaunchTimeout(Duration.ofSeconds(CONFIG.deviceReadyTimeout()));
        options.setAvdReadyTimeout(Duration.ofSeconds(CONFIG.deviceReadyTimeout()));
        options.setAppWaitForLaunch(false); // To handle app launch explicitly if needed

        options.setNewCommandTimeout(Duration.ofSeconds(CONFIG.explicitWaitTimeout())); // General command timeout

        logger.info("Appium capabilities set: {}", options.toJson());
        return options;
    }

    public static AppiumDriver getDriver() {
        return DRIVER_THREAD_LOCAL.get();
    }

    public static void quitDriver() {
        if (DRIVER_THREAD_LOCAL.get() != null) {
            logger.info("Quitting Appium Driver...");
            DRIVER_THREAD_LOCAL.get().quit();
            DRIVER_THREAD_LOCAL.remove();
            logger.info("Appium Driver quit successfully.");
        }
    }
}
