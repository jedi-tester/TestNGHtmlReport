package io.vodqa.extreportng;

import com.aventstack.extentreports.Status;
import io.vodqa.extreportng.extras.SeleUtil;
import io.vodqa.extreportng.listener.TNGReportListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.Test;

/**
 * Created by L094540 on 12/05/2017.
 */
public class TestClass2_Sel extends AbstractTest {
    private static final Logger log = LogManager.getLogger(TestClass2_Sel.class.getName());

    private TNGReportListener report = TNGReportListener.getReportInstance();

    PageObject page;

    @Test
    public void test1_screenshot() throws Exception {
        log.debug("Driver is: " + driver);
        driver.get("https://www.google.com");
        report.addLogToTest(Status.INFO, "Log Message for " + TNGReportListener.getMethodName(),
                "screenshotname");
    }

    @Test
    public void test2_highlight() throws Exception {
        page = PageFactory.initElements(driver, PageObject.class);

        new WebDriverWait(driver, 10)
                .until(ExpectedConditions.visibilityOf(page.googleSearch));

        report.addLogToTest(Status.INFO, "Log Message for " + TNGReportListener.getMethodName(),
                "screenshotname", page.googleSearch, true);
    }

}
