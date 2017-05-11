package io.vodqa.extreportng.listener;

import com.aventstack.extentreports.AnalysisStrategy;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.google.common.base.Preconditions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.*;
import org.testng.xml.XmlSuite;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by SergioLeone on 11/05/2017.
 */

public class TNGReportListener implements ISuiteListener, ITestListener, IInvokedMethodListener, IReporter {

    private static final Logger log = LogManager.getLogger(TNGReportListener.class.getName());

    private static final String REPORTER_ATTRIBUTE = "extentTestNgReporter";
    private static final String SUITE_ATTRIBUTE = "extentTestNgSuite";
    private ExtentReports extent;
    private List<String> testRunnerOutput;
    private ExtentHtmlReporter htmlReporter;
    private static TNGReportListener instance;

    public TNGReportListener() {
        setReportInstance(this);
        testRunnerOutput = new ArrayList<>();
        System.setProperty("reportPath", System.getProperty("user.dir") + "\\test-output\\" + getCurrentDateAndTime());
        String reportPathStr = System.getProperty("reportPath");
        File reportPath;

        try {
            reportPath = new File(reportPathStr);
        } catch (NullPointerException e) {
            reportPath = new File(TestNG.DEFAULT_OUTPUTDIR);
        }

        if (!reportPath.exists()) {
            if (!reportPath.mkdirs()) {
                throw new RuntimeException("Failed to create output run directory");
            }
        }

        File reportFile = new File(reportPath, "test-report.html");

        htmlReporter = new ExtentHtmlReporter(reportFile);
        extent = new ExtentReports();

        List statusHierarchy = Arrays.asList(
                Status.FATAL,
                Status.FAIL,
                Status.ERROR,
                Status.WARNING,
                Status.SKIP,
                Status.PASS,
                Status.DEBUG,
                Status.INFO
        );

        extent.setAnalysisStrategy(AnalysisStrategy.SUITE);
        extent.config().statusConfigurator().setStatusHierarchy(statusHierarchy);

        extent.attachReporter(htmlReporter);

        htmlReporter.config().setTheme(Theme.DARK);
    }

    public static TNGReportListener getReportInstance() {
        return instance;
    }

    private static void setReportInstance(TNGReportListener reportListener) {
        instance = reportListener;
    }

    public void onStart(ISuite iSuite) {
        ExtentTest suite = extent.createTest(iSuite.getName());

        iSuite.setAttribute(REPORTER_ATTRIBUTE, extent);
        iSuite.setAttribute(SUITE_ATTRIBUTE, suite);
    }

    public void onFinish(ISuite iSuite) {
    }

    public void onTestStart(ITestResult iTestResult) {
    }

    public void onTestSuccess(ITestResult iTestResult) {
    }

    public void onTestFailure(ITestResult iTestResult) {
    }

    public void onTestSkipped(ITestResult iTestResult) {
    }

    public void onTestFailedButWithinSuccessPercentage(ITestResult iTestResult) {

    }

    public void onStart(ITestContext iTestContext) {
        ISuite iSuite = iTestContext.getSuite();
        ExtentTest suite = (ExtentTest) iSuite.getAttribute(SUITE_ATTRIBUTE);
        ExtentTest testContext = suite.createNode(iTestContext.getName());
        iTestContext.setAttribute("testContext", testContext);
    }

    public void onFinish(ITestContext iTestContext) {
        ExtentTest testContext = (ExtentTest) iTestContext.getAttribute("testContext");
        if (iTestContext.getFailedTests().size() > 0) {
            testContext.fail("Failed");
        } else if (iTestContext.getSkippedTests().size() > 0) {
            testContext.skip("Skipped");
        } else {
            testContext.pass("Passed");
        }
    }

    public void beforeInvocation(IInvokedMethod iInvokedMethod, ITestResult iTestResult) {
        if (iInvokedMethod.isTestMethod()) {
            ITestContext iTestContext = iTestResult.getTestContext();
            ExtentTest testContext = (ExtentTest) iTestContext.getAttribute("testContext");
            ExtentTest test = testContext.createNode(iTestResult.getName(), iTestResult.getMethod().getDescription());
            iTestResult.setAttribute("test", test);
        }
    }

    public void afterInvocation(IInvokedMethod iInvokedMethod, ITestResult iTestResult) {
        if (iInvokedMethod.isTestMethod()) {
            ExtentTest test = (ExtentTest) iTestResult.getAttribute("test");
            List<String> logs = Reporter.getOutput(iTestResult);
            for (String log : logs) {
                test.info(log);
            }

            int status = iTestResult.getStatus();
            if (ITestResult.SUCCESS == status) {
                test.pass("Passed");
            } else if (ITestResult.FAILURE == status) {
                test.fail(iTestResult.getThrowable());
            } else {
                test.skip("Skipped");
            }

            for (String group : iInvokedMethod.getTestMethod().getGroups()) {
                test.assignCategory(group);
            }
        }
    }

    public void generateReport(List<XmlSuite> list, List<ISuite> list1, String s) {
        extent.setTestRunnerOutput(testRunnerOutput);
        extent.flush();
    }

    private static String getCurrentDateAndTime() {
        log.info("Attempting to get current date and time stamp");
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMddHHmm");
        Date now = new Date();
        String strDate = sdfDate.format(now);
        log.info("Retrieved string: " + strDate);
        return strDate.toLowerCase();
    }

    public void addLogToTest(Status status, String sLogMessage) {
        getExtentTest().log(status, sLogMessage);
    }

    public static ExtentTest getExtentTest() {
        ITestResult iTestResult = Reporter.getCurrentTestResult();
        Preconditions.checkState(iTestResult != null);
        ExtentTest test = (ExtentTest) iTestResult.getAttribute("test");
        log.debug(test);
        return test;
    }


}
