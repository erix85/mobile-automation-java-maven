package com.portfolio.mobileautomation.config;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.Sources;

@Sources({"file:src/test/resources/config.properties"})
public interface FrameworkConfig extends Config {

    @Key("appium.url")
    String appiumUrl();

    @Key("appium.ip")
    String appiumIp();

    @Key("appium.port")
    int appiumPort();

    @Key("emulator.name")
    String emulatorName();

    @Key("platform.name")
    String platformName();

    @Key("platform.version")
    String platformVersion();

    @Key("automation.name")
    String automationName();

    @Key("app.path")
    String appPath();

    @Key("app.package")
    String appPackage();

    @Key("app.activity")
    String appActivity();

    @Key("timeout.explicit")
    long explicitWaitTimeout();

    @Key("timeout.device.ready")
    long deviceReadyTimeout();

    @Key("timeout.app.install")
    long appInstallTimeout();

    @Key("no.reset")
    boolean noReset();

    @Key("full.reset")
    boolean fullReset();
}
