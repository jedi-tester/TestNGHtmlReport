package io.vodqa.extreportng.listener;

import com.aventstack.extentreports.*;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.Markup;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import io.vodqa.extreportng.extras.SeleUtil;
import io.vodqa.extreportng.utils.SystemInfo;
import io.vodqa.extreportng.utils.TestNodeName;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebElement;
import org.testng.*;
import org.testng.xml.XmlSuite;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by SergioLeone on 11/05/2017.
 */

/**
 * This class is an implementation of TestNG Listeners and consists of setup  methods
 * for generating an html report using ExtentReports library created by Anshoo Arora,
 * as well as methods to use inside your tests for adding additional information to reports,
 * passing, failing tests, adding nodes, screenshots, and other implementations of ExtentReports library.
 */
public class TNGReportListener extends SeleUtil implements ISuiteListener, ITestListener, IInvokedMethodListener, IReporter {

    private static final Logger log = LogManager.getLogger(TNGReportListener.class.getName());

    private static final String REPORTER_ATTRIBUTE = "extentTestNgReporter";
    private static final String SUITE_ATTRIBUTE = "extentTestNgSuite";
    private ExtentReports extent;
    private List<String> testRunnerOutput;
    private Map<String, String> systemInfo;
    private ExtentHtmlReporter htmlReporter;
    private static TNGReportListener instance;
    private final String reportFolderName = getCurrentDateAndTime();

    public TNGReportListener() {
        setReportInstance(this);
        testRunnerOutput = new ArrayList<>();
        System.setProperty("reportPath", System.getProperty("user.dir") + "\\test-output\\" + reportFolderName);
        String reportPathStr = System.getProperty("reportPath");
        File reportPath;

        try {
            reportPath = new File(reportPathStr);
        } catch (NullPointerException e) {
            reportPath = new File(TestNG.DEFAULT_OUTPUTDIR + reportFolderName);
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

    /**
     * Gets the instance of the report listener {@link TNGReportListener}
     *
     * @return The instance of the {@code TNGReportListener}
     */
    public static TNGReportListener getReportInstance() {
        return instance;
    }

    /**
     * Sets the instance of the report listener {@link TNGReportListener}
     */
    private static void setReportInstance(TNGReportListener reportListener) {
        instance = reportListener;
    }

    /**
     * Gets the system information map
     *
     * @return The {@code Map} system information map object
     */
    public Map<String, String> getSystemInfoMap() {
        return systemInfo;
    }

    /**
     * Sets the system information
     *
     * @param systemInfo The generated system information {@link Map} object
     */
    public void setSystemInfo(Map<String, String> systemInfo) {
        this.systemInfo = systemInfo;
    }

    /**
     * This method will be automatically called on start of every TestNG Suite
     *
     * @param iSuite TestNG {@link ISuite} object
     */
    public void onStart(ISuite iSuite) {
        ExtentTest suite = extent.createTest(iSuite.getName());

        String configFile = iSuite.getParameter("report.config");

        if (!Strings.isNullOrEmpty(configFile)) {
            htmlReporter.loadXMLConfig(configFile);
        }

        String systemInfoImplClassName = iSuite.getParameter("system.info");
        if (!Strings.isNullOrEmpty(systemInfoImplClassName)) {
            generateSystemInfo(systemInfoImplClassName, iSuite);
        }

        iSuite.setAttribute(REPORTER_ATTRIBUTE, extent);
        iSuite.setAttribute(SUITE_ATTRIBUTE, suite);
    }

    /**
     * Generates System Information that will be included in the report
     * from a properties file.
     *
     * Properties file path should be included in testng.xml as a suite-level parameter
     * with name="sysinfo.properties"
     *
     * @param systemInfoImplClassName   name of the class that houses implementation for
     * @param iSuite                    TestNG {@link ISuite} object
     *
     * @throws IllegalArgumentException If the specified class does not implement {@link SystemInfo}
     * @throws IllegalStateException    If the specified class is not found
     */
    private void generateSystemInfo(String systemInfoImplClassName, ISuite iSuite) {
        try {
            Class<?> systemInfoClass = Class.forName(systemInfoImplClassName);
            if (!SystemInfo.class.isAssignableFrom(systemInfoClass)) {
                throw new IllegalArgumentException("The given system.info class name <" + systemInfoImplClassName +
                        "> should implement the interface <" + SystemInfo.class.getName() + ">");
            }

            SystemInfo t = (SystemInfo) systemInfoClass.newInstance();
            setSystemInfo(t.getSystemInfoMap(iSuite.getParameter("sysinfo.properties")));
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * This method will be automatically called on finish of every TestNG Suite
     *
     * @param iSuite TestNG {@link ISuite} object
     */
    public void onFinish(ISuite iSuite) {
    }

    /**
     * This method will be automatically called on start of every TestNG Test Method
     *
     * @param iTestResult TestNG {@link ITestResult} object
     */
    public void onTestStart(ITestResult iTestResult) {
    }

    /**
     * This method will be automatically called on success of every TestNG Test Method
     *
     * @param iTestResult TestNG {@link ITestResult} object
     */
    public void onTestSuccess(ITestResult iTestResult) {
//        try {
//            addScreenCapture(iTestResult);
//        } catch (IOException e) {
//            e.printStackTrace();
//            log.debug(e.getMessage());
//            log.debug(getExceptionMessage(e));
//        }
    }

    /**
     * This method will be automatically called on fail of every TestNG Test Method
     *
     * @param iTestResult TestNG {@link ITestResult} object
     */
    public void onTestFailure(ITestResult iTestResult) {
//        try {
//            addScreenCapture(iTestResult);
//        } catch (IOException e) {
//            e.printStackTrace();
//            log.debug(e.getMessage());
//            log.debug(getExceptionMessage(e));
//        }
    }

    /**
     * This method will be automatically called on skip of every TestNG Test Method
     *
     * @param iTestResult TestNG {@link ITestResult} object
     */
    public void onTestSkipped(ITestResult iTestResult) {
//        try {
//            addScreenCapture(iTestResult);
//        } catch (IOException e) {
//            e.printStackTrace();
//            log.debug(e.getMessage());
//            log.debug(getExceptionMessage(e));
//        }
    }

    /**
     * This method will be automatically called on fail with success percentage of every TestNG Test Method
     *
     * @param iTestResult TestNG {@link ITestResult} object
     */
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

    /**
     * Will be automatically called before invocation of every TestNG Test Method
     * and makes use of {@link ExtentReports} to add test method name and description to report.
     *
     * @param iInvokedMethod    TestNG {@link IInvokedMethod} object
     * @param iTestResult       TestNG {@link ITestResult} object
     */
    public void beforeInvocation(IInvokedMethod iInvokedMethod, ITestResult iTestResult) {
        if (iInvokedMethod.isTestMethod()) {
            ITestContext iTestContext = iTestResult.getTestContext();
            ExtentTest testContext = (ExtentTest) iTestContext.getAttribute("testContext");
            ExtentTest test = testContext.createNode(iTestResult.getName(), iTestResult.getMethod().getDescription());
            iTestResult.setAttribute("test", test);
        }
    }

    /**
     * Will be automatically called after invocation of every TestNG Test Method
     * and makes use of {@link ExtentReports} to mark completed test as passed, failed, or skipped,
     * and add the result to report.
     *
     * @param iInvokedMethod    TestNG {@link IInvokedMethod} object
     * @param iTestResult       TestNG {@link ITestResult} object
     */
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

    /**
     * Sets the test runner output
     *
     * @param message The message to be logged
     */
    public void setTestRunnerOutput(String message) {
        testRunnerOutput.add(message);
    }

    public void generateReport(List<XmlSuite> list, List<ISuite> list1, String s) {
        if (getSystemInfoMap() != null) {
            for (Map.Entry<String, String> entry : getSystemInfoMap().entrySet()) {
                extent.setSystemInfo(entry.getKey(), entry.getValue());
            }
        }
        extent.setTestRunnerOutput(testRunnerOutput);
        extent.flush();
    }

    /**
     * Adds new node to the test.
     *
     * Before invoking the method set the node name using {@link TestNodeName}
     */
    public void addNewNodeToTest() {
        addNewNodeToTest(TestNodeName.getNodeName());
    }

    /**
     * Adds new node to the test with a given name.
     *
     * @param nodeName The name of the node to be created
     */
    public void addNewNodeToTest(String nodeName) {
        addNewNode("test", nodeName);
    }

    /**
     * Adds new node to the suite.
     *
     * Before invoking the method set the node name using {@link TestNodeName}
     */
    public void addNewNodeToSuite() {
        addNewNodeToSuite(TestNodeName.getNodeName());
    }

    /**
     * Adds new node to the suite with the given name
     *
     * @param nodeName The name of the node to be created
     */
    public void addNewNodeToSuite(String nodeName) {
        addNewNode(SUITE_ATTRIBUTE, nodeName);
    }

    /**
     * Adds new node
     *
     * @param parent    Parent node name
     * @param nodeName  The name of the node to be added
     */
    private void addNewNode(String parent, String nodeName) {
        ITestResult result = Reporter.getCurrentTestResult();
        Preconditions.checkState(result != null);
        ExtentTest parentNode = (ExtentTest) result.getAttribute(parent);
        ExtentTest childNode = parentNode.createNode(nodeName);
        result.setAttribute(nodeName, childNode);
    }

    /**
     * Adds a screenshot image file to the report.
     * This method should only be used in the configuration method
     * (i.e. in methods annotated with {@link org.testng.annotations.AfterMethod})
     * and the {@link ITestResult} is the mandatory parameter
     *
     * Example:
     * @code @AfterMethod doAfterMethodInvocation(ItestResult itestResult) {}
     *
     * @param iTestResult           The {@link ITestResult} object
     * @param sScreenshotName       The image file name
     * @throws IOException
     */
    public void addScreenCapture(ITestResult iTestResult, String sScreenshotName) throws IOException {
        ExtentTest test = (ExtentTest) iTestResult.getAttribute("test");
        test.addScreenCaptureFromPath(captureScreenshot(getDriver(), sScreenshotName));
    }

    /**
     * Adds a screenshot image file to the report.
     * This method should only be used in the configuration method
     * (i.e. in methods annotated with {@link org.testng.annotations.AfterMethod})
     * and the {@link ITestResult} is the mandatory parameter
     * (@code @AfterMethod doAfterMethodInvocation(ItestResult itestResult) {})
     *
     * Additionally prior to taking a screenshot it will try to scroll element into view port
     * and highlight it with a border based on boolean parameter
     *
     * @param iTestResult           The {@link ITestResult} object
     * @param sScreenshotName       The image file name
     * @param element               {@link WebElement} to be scrolled to and displayed in viewport
     * @param highlight             boolean to highlight the element before taking screenshot
     * @throws IOException
     */
    public void addScreenCapture(ITestResult iTestResult, String sScreenshotName, WebElement element, boolean highlight) throws Exception {
        ExtentTest test = (ExtentTest) iTestResult.getAttribute("test");
        test.addScreenCaptureFromPath(captureScreenshot(getDriver(), sScreenshotName, element, highlight));
    }

    /**
     * Adds a screenshot image file to the report.
     * This method should only be used in the configuration method
     * (i.e. in methods annotated with {@link org.testng.annotations.AfterMethod})
     * and the {@link ITestResult} is the mandatory parameter
     * (@code @AfterMethod doAfterMethodInvocation(ItestResult itestResult) {})
     *
     * Screenshot name will be taken from invoked Test Method name and execution status (pass, fail, skip)
     *
     * @param iTestResult           The {@link ITestResult} object
     * @throws IOException
     */
    private void addScreenCapture(ITestResult iTestResult) throws IOException {
        ExtentTest test = (ExtentTest) iTestResult.getAttribute("test");
        test.addScreenCaptureFromPath
                (captureScreenshot(getDriver(),
                        getMethodName(iTestResult) + "_" + getExtentTestStatus(iTestResult)));
    }

    /**
     * Adds a screenshot image file to the report.
     * This method should only be used in the configuration method
     * (i.e. in methods annotated with {@link org.testng.annotations.AfterMethod})
     * and the {@link ITestResult} is the mandatory parameter
     * (@code @AfterMethod doAfterMethodInvocation(ItestResult itestResult) {})
     *
     * Screenshot name will be taken from invoked Test Method name and execution status (pass, fail, skip)
     *
     * Additionally prior to taking a screenshot it will try to scroll element into view port
     * and highlight it with a border based on boolean parameter
     *
     * @param iTestResult           The {@link ITestResult} object
     * @param element               {@link WebElement} to be scrolled to and displayed in viewport
     * @param highlight             boolean to highlight the element before taking screenshot
     * @throws IOException
     */
    public void addScreenCapture(ITestResult iTestResult, WebElement element, boolean highlight) throws Exception {
        ExtentTest test = (ExtentTest) iTestResult.getAttribute("test");
        test.addScreenCaptureFromPath
                (captureScreenshot(getDriver(),
                        getMethodName(iTestResult) + "_" + getExtentTestStatus(iTestResult),
                        element, highlight));
    }

    /**
     * Adds a screen shot image file to the report.
     * This method should only be used in the {@link org.testng.annotations.Test} annotated method
     *
     * @param sScreenshotName   The image file name
     * @throws IOException
     */
    public void addScreenCapture(String sScreenshotName) throws IOException {
        ITestResult iTestResult = Reporter.getCurrentTestResult();
        Preconditions.checkState(iTestResult != null);
        ExtentTest test = (ExtentTest) iTestResult.getAttribute("test");
        test.addScreenCaptureFromPath(captureScreenshot(getDriver(), sScreenshotName));
    }

    /**
     * Adds a screen shot image file to the report.
     * This method should only be used in the {@link org.testng.annotations.Test} annotated method
     *
     * Additionally prior to taking a screenshot it will try to scroll element into view port
     * and highlight it with a border based on boolean parameter
     *
     * @param sScreenshotName   The image file name
     * @param element               {@link WebElement} to be scrolled to and displayed in viewport
     * @param highlight             boolean to highlight the element before taking screenshot
     * @throws IOException
     */
    public void addScreenCapture(String sScreenshotName, WebElement element, boolean highlight) throws Exception {
        ITestResult iTestResult = Reporter.getCurrentTestResult();
        Preconditions.checkState(iTestResult != null);
        ExtentTest test = (ExtentTest) iTestResult.getAttribute("test");
        test.addScreenCaptureFromPath(captureScreenshot(getDriver(), sScreenshotName, element, highlight));
    }

    /**
     * Media model provider method for attaching screenshot to logs with a given name.
     *
     * @param sScreenshotName   Custom screenshot name
     * @return                  {@link MediaEntityModelProvider} object
     * @throws                  IOException
     */
    public MediaEntityModelProvider addMediaProvider(String sScreenshotName) throws IOException {
        return MediaEntityBuilder.createScreenCaptureFromPath
                (captureScreenshot(getDriver(), sScreenshotName)).build();
    }

    /**
     * Media model provider method for attaching screenshot to logs with a given name.
     *
     * Additionally prior to taking a screenshot it will try to scroll element into view port
     * and highlight it with a border based on boolean parameter
     *
     * @param sScreenshotName   Custom screenshot name
     * @param element           {@link WebElement} to be scrolled to and displayed in viewport
     * @param highlight         boolean to highlight the element before taking screenshot
     * @return                  {@link MediaEntityModelProvider} object
     * @throws                  IOException
     */
    public MediaEntityModelProvider addMediaProvider(String sScreenshotName, WebElement element, boolean highlight) throws Exception {
        return MediaEntityBuilder.createScreenCaptureFromPath
                (captureScreenshot(getDriver(), sScreenshotName, element, highlight)).build();
    }

    /**
     * Adds info log message to the node.
     *
     * Before invoking the method set the node name using {@link TestNodeName}
     *
     * @param logMessage The log message string
     */
    public void addInfoLogToNode(String logMessage) {
        addInfoLogToNode(logMessage, TestNodeName.getNodeName());
    }

    /**
     * Adds info log message to the node with a given name
     *
     * @param logMessage    The log message string
     * @param nodeName      The name of the node
     */
    public void addInfoLogToNode(String logMessage, String nodeName) {
        ITestResult result = Reporter.getCurrentTestResult();
        Preconditions.checkState(result != null);
        ExtentTest test = (ExtentTest) result.getAttribute(nodeName);
        test.info(logMessage);
    }

    /**
     * Adds info log message to the node and attaches media to it.
     *
     * Before invoking the method set the node name using {@link TestNodeName}
     *
     * @param logMessage    The log message string
     * @param provider      {@link MediaEntityModelProvider} object for attaching media file to node
     */
    public void addInfoLogToNode(String logMessage, MediaEntityModelProvider provider) {
        addInfoLogToNode(logMessage, TestNodeName.getNodeName(), provider);
    }

    /**
     * Adds info log message to the node with a given name and attaches media to it.
     *
     * @param logMessage    The log message string
     * @param nodeName      The name of the node
     * @param provider      {@link MediaEntityModelProvider} object for attaching media file to node
     */
    public void addInfoLogToNode(String logMessage, String nodeName, MediaEntityModelProvider provider) {
        ITestResult result = Reporter.getCurrentTestResult();
        Preconditions.checkState(result != null);
        ExtentTest test = (ExtentTest) result.getAttribute(nodeName);
        test.info(logMessage, provider);
    }

    /**
     * Marks the node as failed and adds {@link Throwable} object details.
     *
     * Before invoking the method set the node name using {@link TestNodeName}
     *
     * @param t The {@link Throwable} object
     */
    public void failTheNode(Throwable t) {
        failTheNode(TestNodeName.getNodeName(), t);
    }

    /**
     * Marks the node with a give name as failed and adds {@link Throwable} object details.
     *
     * @param nodeName The name of the node
     * @param t        The {@link Throwable} object
     */
    public void failTheNode(String nodeName, Throwable t) {
        ITestResult result = Reporter.getCurrentTestResult();
        Preconditions.checkState(result != null);
        ExtentTest test = (ExtentTest) result.getAttribute(nodeName);
        test.fail(t);
    }

    /**
     * Marks the node as failed and adds {@link Throwable} object details
     * and media file from {@link MediaEntityModelProvider} object.
     *
     * Before invoking the method set the node name using {@link TestNodeName}
     *
     * @param t The         {@link Throwable} object
     * @param provider      {@link MediaEntityModelProvider} object for attaching media file to node
     */
    public void failTheNode(Throwable t, MediaEntityModelProvider provider) {
        failTheNode(TestNodeName.getNodeName(), t, provider);
    }

    /**
     * Marks node with a given name as failed and adds {@link Throwable} object details
     * and media file from {@link MediaEntityModelProvider} object.
     *
     * @param nodeName      The name of the node
     * @param t             The {@link Throwable} object
     * @param provider      {@link MediaEntityModelProvider} object for attaching media file to node
     */
    public void failTheNode(String nodeName, Throwable t, MediaEntityModelProvider provider) {
        ITestResult result = Reporter.getCurrentTestResult();
        Preconditions.checkState(result != null);
        ExtentTest test = (ExtentTest) result.getAttribute(nodeName);
        test.fail(t, provider);
    }

    /**
     * Marks the node as failed. The node name should have been set already using {@link TestNodeName}
     *
     * @param logMessage The message to be logged
     */
    public void failTheNode(String logMessage) {
        failTheNode(TestNodeName.getNodeName(), logMessage);
    }

    /**
     * Marks the given node as failed
     *
     * @param nodeName   The name of the node
     * @param logMessage The message to be logged
     */
    public void failTheNode(String nodeName, String logMessage) {
        ITestResult result = Reporter.getCurrentTestResult();
        Preconditions.checkState(result != null);
        ExtentTest test = (ExtentTest) result.getAttribute(nodeName);
        test.fail(logMessage);
    }

    /**
     * Marks the node as failed and attaches media file to it
     * using {@link MediaEntityModelProvider} object
     *
     * Before invoking the method set the node name using {@link TestNodeName}
     *
     * @param logMessage    The message to be logged
     * @param provider      {@link MediaEntityModelProvider} object for attaching media file to node
     */
    public void failTheNode(String logMessage, MediaEntityModelProvider provider) {
        failTheNode(TestNodeName.getNodeName(), provider, logMessage);
    }

    /**
     * Marks node with a given name as failed and adds log message and media files to it
     * using {@link MediaEntityModelProvider} object
     *
     * @param nodeName      The name of the node
     * @param logMessage    The message to be logged
     * @param provider      {@link MediaEntityModelProvider} object for attaching media file to node
     */
    public void failTheNode(String nodeName, MediaEntityModelProvider provider, String logMessage) {
        ITestResult result = Reporter.getCurrentTestResult();
        Preconditions.checkState(result != null);
        ExtentTest test = (ExtentTest) result.getAttribute(nodeName);
        test.fail(logMessage, provider);
    }

    /**
     * Marks the test as failed
     * and attaches a media file to it
     * using {@link MediaEntityModelProvider} object
     *
     * This method should only be used inside {@link org.testng.annotations.Test} annotated methods
     *
     * @param details String message to log into report
     * @param provider      {@link MediaEntityModelProvider} object for attaching media file to node
     */
    public void failTheTest(String details, MediaEntityModelProvider provider) {
        ExtentTest test = getExtentTest();
        test.fail(details, provider);
    }

    /**
     * Marks the test as failed
     *
     * This method should only be used inside {@link org.testng.annotations.Test} annotated methods
     *
     * @param details String message to log into report
     */
    public void failTheTest(String details) {
        ExtentTest test = getExtentTest();
        test.fail(details);
    }

    /**
     * Marks the test as failed
     * and attaches a {@link Throwable} object details,
     * and a media file to it using {@link MediaEntityModelProvider} object
     *
     * This method should only be used inside {@link org.testng.annotations.Test} annotated methods
     *
     * @param t The         {@link Throwable} object
     * @param provider      {@link MediaEntityModelProvider} object for attaching media file to node
     */
    public void failTheTest(Throwable t, MediaEntityModelProvider provider) {
        ExtentTest test = getExtentTest();
        test.fail(t, provider);
    }

    /**
     * Marks the test as failed
     * and attaches a {@link Throwable} object details.
     *
     * This method should only be used inside {@link org.testng.annotations.Test} annotated methods
     *
     * @param t The         {@link Throwable} object
     */
    public void failTheTest(Throwable t) {
        ExtentTest test = getExtentTest();
        test.fail(t);
    }

    /**
     * Marks the test as failed
     * and attaches {@link Markup} object to it.
     *
     * This method should only be used inside {@link org.testng.annotations.Test} annotated methods
     *
     * @param markup Markup object
     */
    public void failTheTest(Markup markup) {
        ExtentTest test = getExtentTest();
        test.fail(markup);
    }

    private static String getCurrentDateAndTime() {
        log.info("Attempting to get current date and time stamp");
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMddHHmm");
        Date now = new Date();
        String strDate = sdfDate.format(now);
        log.info("Retrieved string: " + strDate);
        return strDate.toLowerCase();
    }

    /**
     * Adds a log to the test node. This method should be used only in the
     * {@link org.testng.annotations.Test} annotated method
     *
     * @param status        The log status
     * @param sLogMessage   The log message
     */
    public void addLogToTest(Status status, String sLogMessage) {
        getExtentTest().log(status, sLogMessage);
    }

    /**
     * Adds a log to the test node and attaches
     * a media file to it using {@link MediaEntityModelProvider} object
     *
     * This method should only be used inside {@link org.testng.annotations.Test} annotated methods
     *
     * @param status        The log status
     * @param sLogMessage   The log message
     * @param provider      {@link MediaEntityModelProvider} object for attaching media file to node
     */
    public void addLogToTest(Status status, String sLogMessage, MediaEntityModelProvider provider) {
        getExtentTest().log(status, sLogMessage, provider);
    }

    /**
     * Adds a log to the test node and attaches
     * a screenshot with a given name
     *
     * This method should only be used inside {@link org.testng.annotations.Test} annotated methods
     *
     * @param status            The log status
     * @param sLogMessage       The log message
     * @param sScreenshotName   The screenshot name to be attached to the log
     */
    public void addLogToTest(Status status, String sLogMessage, String sScreenshotName) throws IOException{
        getExtentTest().log(status, sLogMessage, addMediaProvider(sScreenshotName));
    }

    /**
     * Adds a log to the test node and attaches a screenshot with a given name
     *
     * Additionally prior to taking a screenshot it will try to scroll element into view port
     * and highlight it with a border based on boolean parameter
     *
     * This method should only be used inside {@link org.testng.annotations.Test} annotated methods
     *
     * @param status            The log status
     * @param sLogMessage       The log message
     * @param sScreenshotName   The screenshot name to be attached to the log
     * @param element           {@link WebElement} to be scrolled to and displayed in viewport
     * @param highlight         boolean to highlight the element before taking screenshot
     */
    public void addLogToTest(Status status, String sLogMessage, String sScreenshotName,
                             WebElement element, boolean highlight) throws Exception{
        getExtentTest().log(status, sLogMessage, addMediaProvider(sScreenshotName, element, highlight));
    }

    /**
     * Adds a log to the test node with a {@link Throwable} object details.
     *
     * This method should only be used inside {@link org.testng.annotations.Test} annotated methods
     *
     * @param status    The log status
     * @param t         {@link Throwable} object
     */
    public void addLogToTest(Status status, Throwable t) {
        getExtentTest().log(status, t);
    }

    /**
     * Adds a log to the test node with a {@link Throwable} object details,
     * and a media file using {@link MediaEntityModelProvider} object
     *
     * This method should only be used inside {@link org.testng.annotations.Test} annotated methods
     *
     * @param status    The log status
     * @param t         {@link Throwable} object
     * @param provider      {@link MediaEntityModelProvider} object for attaching media file to node
     */
    public void addLogToTest(Status status, Throwable t, MediaEntityModelProvider provider) {
        getExtentTest().log(status, t, provider);
    }

    /**
     * Adds a log to the test node with a {@link Throwable} object details,
     * and attaches a screenshot with a given name
     *
     * This method should only be used inside {@link org.testng.annotations.Test} annotated methods
     *
     * @param status            The log status
     * @param t                 {@link Throwable} object
     * @param sScreenshotName   Screenshot name to be attached to log
     */
    public void addLogToTest(Status status, Throwable t, String sScreenshotName) throws IOException {
        getExtentTest().log(status, t, addMediaProvider(sScreenshotName));
    }

    /**
     * Adds a log to the test node with a {@link Throwable} object details,
     * and attaches a screenshot with a given name
     *
     * Additionally prior to taking a screenshot it will try to scroll element into view port
     * and highlight it with a border based on boolean parameter
     *
     * This method should only be used inside {@link org.testng.annotations.Test} annotated methods
     *
     * @param status            The log status
     * @param t                 {@link Throwable} object
     * @param sScreenshotName   Screenshot name to be attached to log
     * @param element           {@link WebElement} to be scrolled to and displayed in viewport
     * @param highlight         boolean to highlight the element before taking screenshot
     */
    public void addLogToTest(Status status, Throwable t, String sScreenshotName,
                             WebElement element, boolean highlight) throws Exception {
        getExtentTest().log(status, t, addMediaProvider(sScreenshotName, element, highlight));
    }

    /**
     * Adds a log to the test node with a {@link Markup} object details.
     *
     * @param status    The log status
     * @param markup    {@link Markup} object
     */
     public void addLogToTest(Status status, Markup markup) {
        getExtentTest().log(status, markup);
    }

    /**
     * Get instance of extent test node.
     *
     * This method should be used only in the
     * {@link org.testng.annotations.Test} annotated method
     *
     * @return {@link ExtentTest} test object
     */
    public static ExtentTest getExtentTest() {
        ITestResult iTestResult = Reporter.getCurrentTestResult();
        Preconditions.checkState(iTestResult != null);
        ExtentTest test = (ExtentTest) iTestResult.getAttribute("test");
        log.debug(test);
        return test;
    }

    /**
     * Get status of test node.
     *
     * This method should be used only in the
     * {@link org.testng.annotations.Test} annotated method
     *
     * @return execution status of test
     */
    public static Status getExtentTestStatus() {
        ITestResult iTestResult = Reporter.getCurrentTestResult();
        Preconditions.checkState(iTestResult != null);
        ExtentTest test = (ExtentTest) iTestResult.getAttribute("test");
        log.debug(test.getStatus());
        return test.getStatus();
    }

    /**
     * Get status of test node. This method should be used only in the configuration method
     * and the {@link ITestResult} is the mandatory parameter
     *
     * @param iTestResult The {@link ITestResult} object
     */
    public static Status getExtentTestStatus(ITestResult iTestResult) {
        ExtentTest test = (ExtentTest) iTestResult.getAttribute("test");
        log.debug(test.getStatus());
        return test.getStatus();
    }

    /**
     * Get name of test node method.
     *
     * This method should only be used in the configuration method
     * and the {@link ITestResult} is the mandatory parameter
     *
     * @param iTestResult The {@link ITestResult} object
     *
     * @return result of TestNG invoked test method
     */
    public static String getMethodName(ITestResult iTestResult) {
        return iTestResult.getMethod().getMethodName();
    }

    /**
     * Get name of test node method.
     *
     * This method should only be used in the
     * {@link org.testng.annotations.Test} annotated method
     *
     * @return name of invoked TestNG test method
     */
    public static String getMethodName() {
        ITestResult iTestResult = Reporter.getCurrentTestResult();
        Preconditions.checkState(iTestResult != null);
        return iTestResult.getMethod().getMethodName();
    }

    public static Markup addCodeBlockMarkup(String code) {
        return MarkupHelper.createCodeBlock(code);
    }

    public static Markup addLabelMarkup(String sText, ExtentColor extentColor){
        return MarkupHelper.createLabel(sText, extentColor);
    }

    public static Markup addTableMarkup(String[][] data) {
        return MarkupHelper.createTable(data);
    }

    //    public void logStepIntoExtentReport(String elementDescription, String action,String typeString) {
//        ExtentTestManager.getTest().log(Status.INFO,
//                elementDescription + "; " + withBoldHTML("Text") + ": " + typeString);
//    }

//    public String withBoldHTML(String string) {
//        if (!string.trim().isEmpty()) {
//            return "<b>" + string + "</b>";
//        } else {
//            return "";
//        }
//    }


    // ~ Inner Classes --------------------------------------------------------
    /** Arranges methods by classname and method name */
    private class TestSorter implements Comparator<IInvokedMethod> {
        // ~ Methods
        // -------------------------------------------------------------

        /** Arranges methods by classname and method name */
        @Override
        public int compare(IInvokedMethod obj1, IInvokedMethod obj2) {
            int r = obj1.getTestMethod().getTestClass().getName().compareTo(obj2.getTestMethod().getTestClass().getName());
            return r;
        }
    }

    private class TestMethodSorter implements Comparator<ITestNGMethod> {
        @Override
        public int compare(ITestNGMethod obj1, ITestNGMethod obj2) {
            int r = obj1.getTestClass().getName().compareTo(obj2.getTestClass().getName());
            if (r == 0) {
                r = obj1.getMethodName().compareTo(obj2.getMethodName());
            }
            return r;
        }
    }

    private class TestResultsSorter implements Comparator<ITestResult> {
        @Override
        public int compare(ITestResult obj1, ITestResult obj2) {
            int result = obj1.getTestClass().getName().compareTo(obj2.getTestClass().getName());
            if (result == 0) {
                result = obj1.getMethod().getMethodName().compareTo(obj2.getMethod().getMethodName());
            }
            return result;
        }
    }
}
