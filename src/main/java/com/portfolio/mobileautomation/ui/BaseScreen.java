package com.portfolio.mobileautomation.ui;

import com.portfolio.mobileautomation.config.FrameworkConfig;
import com.portfolio.mobileautomation.driver.DriverManager;
import com.google.common.collect.ImmutableList;
import io.appium.java_client.AppiumDriver;
import org.aeonbits.owner.ConfigFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Pause;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

import static java.time.Duration.ofMillis;
import static java.time.Duration.ofSeconds;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

public abstract class BaseScreen {

    private static final Logger logger = LoggerFactory.getLogger(BaseScreen.class);
    private static final FrameworkConfig CONFIG = ConfigFactory.create(FrameworkConfig.class);
    protected final AppiumDriver driver;
    protected final WebDriverWait wait;

    public BaseScreen() {
        this.driver = DriverManager.getDriver();
        this.wait = new WebDriverWait(driver, ofSeconds(CONFIG.explicitWaitTimeout()));
    }

    protected WebElement waitForElementToBeVisible(By by) {
        logger.debug("Waiting for element to be visible: {}", by);
        return wait.until(visibilityOfElementLocated(by));
    }

    protected WebElement waitForElementToBeClickable(By by) {
        logger.debug("Waiting for element to be clickable: {}", by);
        return wait.until(ExpectedConditions.elementToBeClickable(by));
    }

    protected BaseScreen click(By by) {
        waitForElementToBeClickable(by).click();
        logger.info("Clicked on element: {}", by);
        return this;
    }

    protected BaseScreen type(By by, String text) {
        WebElement element = waitForElementToBeVisible(by);
        element.clear();
        element.sendKeys(text);
        logger.info("Typed '{}' into element: {}", text, by);
        return this;
    }

    protected String getText(By by) {
        String text = waitForElementToBeVisible(by).getText();
        logger.info("Retrieved text '{}' from element: {}", text, by);
        return text;
    }

    protected boolean isElementDisplayed(By by) {
        try {
            return waitForElementToBeVisible(by).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Performs a scroll gesture.
     * @param startX X coordinate to start scroll
     * @param startY Y coordinate to start scroll
     * @param endX X coordinate to end scroll
     * @param endY Y coordinate to end scroll
     * @param durationMillis Duration of the scroll in milliseconds
     * @return Current screen object for chaining
     */
    protected BaseScreen scroll(int startX, int startY, int endX, int endY, long durationMillis) {
        logger.info("Performing scroll gesture from ({}, {}) to ({}, {}) with duration {}ms", startX, startY, endX, endY, durationMillis);
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence scroll = new Sequence(finger, 1);
        scroll.addAction(finger.createPointerMove(ofMillis(0), PointerInput.Origin.viewport(), startX, startY));
        scroll.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        scroll.addAction(new Pause(finger, ofMillis(600))); // Small pause to simulate touch
        scroll.addAction(finger.createPointerMove(ofMillis(durationMillis), PointerInput.Origin.viewport(), endX, endY));
        scroll.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        driver.perform(ImmutableList.of(scroll));
        return this;
    }

    /**
     * Scrolls down the screen.
     * @return Current screen object for chaining
     */
    protected BaseScreen scrollDown() {
        logger.info("Scrolling down the screen.");
        Dimension size = driver.manage().window().getSize();
        int startX = size.width / 2;
        int startY = (int) (size.height * 0.8);
        int endY = (int) (size.height * 0.2);
        return scroll(startX, startY, startX, endY, 1000);
    }

    /**
     * Scrolls up the screen.
     * @return Current screen object for chaining
     */
    protected BaseScreen scrollUp() {
        logger.info("Scrolling up the screen.");
        Dimension size = driver.manage().window().getSize();
        int startX = size.width / 2;
        int startY = (int) (size.height * 0.2);
        int endY = (int) (size.height * 0.8);
        return scroll(startX, startY, startX, endY, 1000);
    }

    /**
     * Performs a horizontal swipe gesture.
     * @param startX X coordinate to start swipe
     * @param startY Y coordinate to start swipe
     * @param endX X coordinate to end swipe
     * @param durationMillis Duration of the swipe in milliseconds
     * @return Current screen object for chaining
     */
    protected BaseScreen swipeHorizontal(int startX, int startY, int endX, long durationMillis) {
        logger.info("Performing horizontal swipe gesture from ({}, {}) to ({}, {}) with duration {}ms", startX, startY, endX, startY, durationMillis);
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence swipe = new Sequence(finger, 1);
        swipe.addAction(finger.createPointerMove(ofMillis(0), PointerInput.Origin.viewport(), startX, startY));
        swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        swipe.addAction(new Pause(finger, ofMillis(600)));
        swipe.addAction(finger.createPointerMove(ofMillis(durationMillis), PointerInput.Origin.viewport(), endX, startY));
        swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        driver.perform(ImmutableList.of(swipe));
        return this;
    }

    /**
     * Swipes left across the screen.
     * @return Current screen object for chaining
     */
    protected BaseScreen swipeLeft() {
        logger.info("Swiping left across the screen.");
        Dimension size = driver.manage().window().getSize();
        int startX = (int) (size.width * 0.8);
        int endX = (int) (size.width * 0.2);
        int startY = size.height / 2;
        return swipeHorizontal(startX, startY, endX, 1000);
    }

    /**
     * Swipes right across the screen.
     * @return Current screen object for chaining
     */
    protected BaseScreen swipeRight() {
        logger.info("Swiping right across the screen.");
        Dimension size = driver.manage().window().getSize();
        int startX = (int) (size.width * 0.2);
        int endX = (int) (size.width * 0.8);
        int startY = size.height / 2;
        return swipeHorizontal(startX, startY, endX, 1000);
    }
}
