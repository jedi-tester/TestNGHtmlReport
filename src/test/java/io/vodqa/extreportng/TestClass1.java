package io.vodqa.extreportng;

import com.aventstack.extentreports.Status;
import io.vodqa.extreportng.listener.TNGReportListener;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Created by L094540 on 11/05/2017.
 */
public class TestClass1 {
    private TNGReportListener report = TNGReportListener.getReportInstance();

    private String pass = "pass";
    private String fail = "fail";

    @Test
    public void test1_pass() {
        report.addLogToTest(Status.INFO, "This is a log info message added to test");

        Assert.assertEquals(pass, "pass", "This method should pass and message is NOT displayed");
    }

    @Test
    public void test2_fail() {
        Assert.assertEquals(fail, "pass", "This method should fail and message is displayed");
    }

}
