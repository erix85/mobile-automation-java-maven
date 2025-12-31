package com.portfolio.mobileautomation.retry;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RetryAnalyzer implements IRetryAnalyzer {

    private static final Logger logger = LoggerFactory.getLogger(RetryAnalyzer.class);
    private static final int MAX_RETRY_COUNT = 2; // Maximum number of times to retry a failed test
    private int retryCount = 0;

    @Override
    public boolean retry(ITestResult result) {
        if (retryCount < MAX_RETRY_COUNT) {
            logger.warn("Retrying test '{}' for the {} time(s).", result.getName(), retryCount + 1);
            retryCount++;
            return true;
        }
        return false;
    }
}
