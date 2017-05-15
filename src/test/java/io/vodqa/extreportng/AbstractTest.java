package io.vodqa.extreportng;

import io.vodqa.extreportng.extras.SeleUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.io.TemporaryFilesystem;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.logging.Level;

/**
 * Created by L094540 on 12/05/2017.
 */
public abstract class AbstractTest {
    static WebDriver driver = null;

    private static final Logger log = LogManager.getLogger(AbstractTest.class.getName());

    @BeforeSuite
    public void driverSetUp() throws IOException {
        File propFile = new File(System.getProperty("user.dir") + "\\src\\test\\resources\\driver.properties");
        FileInputStream fis = new FileInputStream(propFile);
        ResourceBundle res = new PropertyResourceBundle(fis);

        LoggingPreferences preferences = new LoggingPreferences();
        preferences.enable("WARNING", Level.WARNING);

        DesiredCapabilities caps = DesiredCapabilities.firefox();
        caps.setCapability(CapabilityType.BROWSER_NAME, "firefox");
        caps.setCapability(CapabilityType.BROWSER_VERSION, "53.0");
        caps.setCapability(CapabilityType.LOGGING_PREFS, preferences);
        caps.setCapability(CapabilityType.ENABLE_PROFILING_CAPABILITY, true);
        caps.setCapability(CapabilityType.ELEMENT_SCROLL_BEHAVIOR, 1);
        caps.setPlatform(Platform.WINDOWS);
        caps.setCapability("marionette", true);

        FirefoxOptions options = new FirefoxOptions();
        options.addPreference("enableNativeEvents", true);
        options.addPreference("disableAddons", true);
        options.addPreference("javascript.enabled", true);
        options.addPreference("app.update.enabled", false);
        options.addPreference("app.update.service.enabled", false);
        options.addPreference("app.update.auto", false);
        options.addPreference("app.update.staging.enabled", false);
        options.addPreference("app.update.silent", false);
        options.addPreference("media.gmp-provider.enabled", false);
        options.addPreference("media.gmp-eme-adobe.enabled", false);
        options.addPreference("media.gmp-widevinecdm.enabled", false);
        options.addPreference("media.gmp.trial-create.enabled", false);
        options.addPreference("extensions.update.autoUpdate", false);
        options.addPreference("extensions.update.autoUpdateEnabled", false);
        options.addPreference("extensions.update.enabled", false);
        options.addPreference("extensions.update.autoUpdateDefault", false);
        options.addPreference("extensions.enabledAddons", "");
        options.addPreference("extensions.pocket.enabled", false);
        options.addPreference("extensions.logging.enabled", false);
        options.addPreference("services.sync.prefs.sync.extensions.update.enabled", false);
        options.addPreference("lightweightThemes.update.enabled", false);
        options.addPreference("browser.taskbar.lists.enabled", false);
        options.addPreference("browser.taskbar.lists.frequent.enabled", false);
        options.addPreference("browser.taskbar.lists.recent.enabled", false);
        options.addPreference("browser.taskbar.lists.tasks.enabled", false);

        options.setBinary(res.getString("browserPath"));
        options.setLogLevel(Level.WARNING);

        System.setProperty("webdriver.gecko.driver", res.getString("driverPath"));

        driver = new FirefoxDriver(options.addTo(caps));

        SeleUtil.setReportDriver(driver);
    }

    @AfterSuite
    public void tearDown() {
        if (driver != null) {
            TemporaryFilesystem.getDefaultTmpFS().deleteTemporaryFiles();
            driver.quit();
        }
    }
}
