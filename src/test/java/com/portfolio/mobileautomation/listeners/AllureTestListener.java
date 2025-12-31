package com.portfolio.mobileautomation.listeners;

import com.portfolio.mobileautomation.driver.DriverManager;
import io.qameta.allure.Attachment;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AllureTestListener implements ITestListener {

    private static final Logger logger = LoggerFactory.getLogger(AllureTestListener.class);

    @Override
    public void onTestFailure(ITestResult result) {
        logger.error("Test failed: {}", result.getName());
        saveScreenshot();
    }

    @Attachment(value = "Screenshot on failure", type = "image/png")
    private byte[] saveScreenshot() {
        if (DriverManager.getDriver() == null) {
            logger.warn("Driver is null, cannot capture screenshot.");
            return new byte[0];
        }
        logger.info("Capturing screenshot...");
        return ((TakesScreenshot) DriverManager.getDriver()).getScreenshotAs(OutputType.BYTES);
    }

    // Other ITestListener methods (optional, can be overridden if needed)
    @Override
    public void onStart(org.testng.ITestContext context) {
        logger.info("Test Suite started: {}", context.getName());
    }

    @Override
    public void onFinish(org.testng.ITestContext context) {
        logger.info("Test Suite finished: {}", context.getName());
    }

    @Override
    public void onTestStart(ITestResult result) {
        logger.info("Test started: {}", result.getName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        logger.info("Test passed: {}", result.getName());
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        logger.warn("Test skipped: {}", result.getName());
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        // Not implemented for this framework
    }
}
