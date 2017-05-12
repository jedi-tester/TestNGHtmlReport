package io.vodqa.extreportng;

import com.aventstack.extentreports.Status;
import io.vodqa.extreportng.listener.TNGReportListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.Test;

/**
 * Created by L094540 on 12/05/2017.
 */
public class TestClass2_Sel extends AbstractTest {
    private static final Logger log = LogManager.getLogger(TestClass2_Sel.class.getName());

    private TNGReportListener report = TNGReportListener.getReportInstance();

    @Test
    public void test1_screenshot() throws Exception {
        log.debug("Driver is: " + driver);
        driver.get("https://www.google.com");
        report.addLogToTest(Status.INFO, "LogMessageString", "screenshotname");


    }

}
