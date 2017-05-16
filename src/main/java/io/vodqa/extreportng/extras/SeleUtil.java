package io.vodqa.extreportng.extras;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import sun.misc.Launcher;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Created by SergioLeone on 12/05/2017.
 */
public class SeleUtil implements SeleDriver {

    private static final Logger log = LogManager.getLogger(SeleUtil.class.getName());

    private static WebDriver driver;

    private SeleUtil.Highlight hiUtil = new Highlight();
    private SeleUtil.JSHelper jsHelper = new JSHelper();

    public SeleUtil() {
//        this.driver = driver;
    }

    public static void setReportDriver(WebDriver driver) {
        SeleUtil.driver = SeleDriver.getSeleDriver(driver);
    }

    protected static WebDriver getDriver() {
        log.debug("Returning driver: " + driver);
        return driver;
    }

    public class Highlight {
        private WebElement lastElem = null;
        private String lastBorder = null;

        void highlightElement(WebElement elem) throws IOException {
            log.debug("Highlight element: " + elem);
            final String jsScript = jsHelper.getScriptStringFromFile("getElementBorder.js");
            unhighlightLast();

            // remember the new element
            log.debug("Getting last element: " + lastElem);
            lastElem = elem;

            lastBorder = (String)jsHelper.executeScript(jsScript, elem);
            log.debug("Highlighted: " + elem + "\n New border: " + lastBorder);
        }

        public void unhighlightElement(WebElement elem) throws IOException {
            log.debug("Unhighlight element: " + elem);
            final String jsScript = jsHelper.getScriptStringFromFile("removeElementBorder.js");

            lastBorder = (String) jsHelper.executeScript(jsScript, elem);
            log.debug("Unhighlighted: " + elem + "\n New border: " + lastBorder);

            lastElem = null;
        }

        private void unhighlightLast() throws IOException{
            final String jsScript = jsHelper.getScriptStringFromFile("unhighlightLastElement.js");

            if (lastElem != null) {
                log.debug("Unhighlight last element: " + lastElem);
                try {
                    // if there already is a highlighted element, unhighlight it
                    jsHelper.executeScript(jsScript, lastElem, lastBorder);
                    log.debug("Unhighlighted last element: " + lastElem + "\n With border: " + lastBorder);
                } catch (StaleElementReferenceException ignored) {
                    // the page got reloaded, the element isn't there
                } finally {
                    // element either restored or wasn't valid, nullify in both cases
                    lastElem = null;
                }
            }
        }
    }

    class JSHelper {

//        private JavascriptExecutor js = (JavascriptExecutor)getDriver();

        final String getScriptStringFromFile(String sScriptFileName) throws IOException {
            log.debug("Converting file to string for: " + sScriptFileName);

            String jsScriptString;

            try {
                jsScriptString = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("scripts/" + sScriptFileName), Charset.defaultCharset());
            } catch (IOException e) {
                log.error("Exception found in method: " + getCurrentMethodName());
                log.error(e.getMessage());
                log.error(getExceptionMessage(e));
                throw e;
            }

            log.debug("Script string to be returned: " + jsScriptString);
            return jsScriptString;
        }

        Object executeScript(String jsScript, Object... var) {
            log.debug("Executing JS script");
            try {
                return ((JavascriptExecutor)driver).executeScript(jsScript, var);
            } catch (Exception e) {
                log.error("Exception found in method: " + getCurrentMethodName());
                log.error(e.getMessage());
                log.error(getExceptionMessage(e));
                throw e;
            }
        }
    }

    protected String captureScreenshot(WebDriver driver, String sScreenshotName) throws IOException {
        try {
            log.debug("Attempting to capture screenshot");
            TakesScreenshot ts=(TakesScreenshot)driver;
            log.debug("Getting screenshot: " + ts + " with Driver: " + driver);
            File source=ts.getScreenshotAs(OutputType.FILE);
            log.debug("Source file: " + source);
            final String sScreenshotFilePath = sPath(sScreenshotName);
            FileUtils.copyFile(source, new File(sScreenshotFilePath));
            log.debug("Copied screenshot to: " + sScreenshotFilePath);
            hiUtil.unhighlightLast();
            return sScreenshotFilePath;
        } catch (IOException e) {
            log.error("Exception found in method: " + getCurrentMethodName());
            log.error(e.getMessage());
            log.error(getExceptionMessage(e));
            throw e;
        }
    }

    protected String captureScreenshot(WebDriver driver, String sScreenshotName, WebElement element, boolean highlight) throws Exception {
        log.debug("Attempting to capture screenshot of element: " + element);
        log.debug("Highlight parameter: " + highlight);

        try {
            if (highlight && isElementDisplayed(element)) {
                log.debug("Element is displayed and highlight is: " + highlight + ". Attempting scroll to element and highlight");
                scrollElementIntoMiddle(element, highlight);
            } else if (isElementDisplayed(element)) {
                log.debug("Element is displayed and highlight is: " + highlight + ". Attempting scroll to element");
                scrollElementIntoMiddle(element);
            } else {
                log.debug("Element is not displayed.");
                try {
                    throw new NoSuchElementException("Error capturing screenshot with element, element not displayed. Locator: " + element);
                } catch (Exception e) {
                    log.error("Exception found in method: " + getCurrentMethodName());
                    log.error(e.getMessage());
                    log.error(getExceptionMessage(e));
                    throw e;
                }
            }
        } catch (Exception e) {
            log.error("Exception found in method: " + getCurrentMethodName());
            log.error(e.getMessage());
            log.error(getExceptionMessage(e));
        }

        return captureScreenshot(driver, sScreenshotName);
    }

    private void scrollElementIntoMiddle(WebElement element) throws IOException {
        log.info("Attempting to move(scroll) to element with JavaScriptExecutor call");

        String jsScript = jsHelper.getScriptStringFromFile("scrollElementIntoMiddle.js");

        if (isElementDisplayed(element)) {
            jsHelper.executeScript(jsScript, element);
        } else {
            try {
                throw new NoSuchElementException("Error scrolling element into middle, element not displayed. Locator: " + element);
            } catch (Exception e) {
                log.error("Exception found in method: " + getCurrentMethodName());
                log.error(e.getMessage());
                log.error(getExceptionMessage(e));
                throw e;
            }
        }
    }

    private void scrollElementIntoMiddle(WebElement element, boolean highlight) throws Exception {
        log.info("Attempting to move(scroll) to element with JavaScriptExecutor call");

        int counter = 0;
        int maxTry = 5;

        breakWhile:while (true) {
            if (counter < maxTry) {
                try {
                    if (highlight && isElementDisplayed(element)) {
                        log.debug("Element is displayed and highlight is: " + highlight);
                        scrollElementIntoMiddle(element);
                        log.debug("Waiting for element to be visible in viewport");
                        waitUntil
                                (new CustomConditions().
                                        isElementInViewport(element), 3, 500, TimeUnit.MILLISECONDS);
                        log.debug("Element is visible. Attempting to highlight");
                        hiUtil.highlightElement(element);
                        break breakWhile;
                    } else if (isElementDisplayed(element)) {
                        log.debug("Element is displayed and highlight is: " + highlight);
                        scrollElementIntoMiddle(element);
                        log.debug("Waiting for element to be visible in viewport");
                        waitUntil
                                (new CustomConditions().
                                        isElementInViewport(element), 3, 500, TimeUnit.MILLISECONDS);
                        log.debug("Element is visible");
                        break breakWhile;
                    } else {
                        log.debug("Element is not displayed.");
                        try {
                            throw new NoSuchElementException("Error scrolling element into middle. Element not displayed. Locator: " + element);
                        } catch (Exception e) {
                            log.error("Exception found in method: " + getCurrentMethodName());
                            log.error(e.getMessage());
                            log.error(getExceptionMessage(e));
                            throw e;
                        }
                    }
                } catch (Exception e) {
                    log.debug("Element is not in viewport. Attempting once more to scroll");
                    scrollElementIntoMiddle(element);
                    counter++;
                    log.debug("Attempts counter: " + counter);
                }
            } else {
                log.debug("Max scroll attempts reached.");
                try {
                    throw new Exception("Unable to scroll element into viewport");
                } catch (Exception e) {
                    log.error("Exception found in method: " + getCurrentMethodName());
                    log.error(e.getMessage());
                    log.error(getExceptionMessage(e));
                    throw e;
                }
            }
        }
    }

    private static String sPath(String sScreenshotName) {
        return System.getProperty("reportPath") + "\\Screenshots\\" + getCurrentTime() + "_" + sScreenshotName + ".png";
    }

    private static String getCurrentTime() {
        log.debug("Attempting to get current time stamp");
        SimpleDateFormat sdfDate = new SimpleDateFormat("HHmmssSSS");
        Date now = new Date();
        String strDate = sdfDate.format(now);
        log.debug("Retrieved string: " + strDate);
        return strDate.toLowerCase();
    }

    private static String getExceptionMessage(Exception e) {
        String s = "";
        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        e.printStackTrace(printWriter);
        s = writer.toString();
//        System.err.println("ERROR: Exception Found Check the Log File");
        return s;
    }

    private static boolean isElementDisplayed(WebElement element) {
        boolean displayed = false;

        try {
            log.debug("Attempting to verify if element is displayed. Locator: " + element);
            if (element.isDisplayed()) {
                log.debug("Element is displayed. Returning 'true'");
                displayed = true;
            }
        } catch (Exception e) {
            log.debug("Element is not displayed. Returning 'false'");
            displayed = false;
        }

        return displayed;
    }

    private static Object waitUntil(Function function, int timeOutInSeconds) {
        FluentWait<WebDriver> wait = new FluentWait<>(driver)
                .withTimeout(timeOutInSeconds, SECONDS);

        return wait.until(function);
    }

    private static Object waitUntil(Function function, int timeOutInSeconds, int poolingInterval) {
        Wait<WebDriver> wait = new FluentWait<>(driver)
                .withTimeout(timeOutInSeconds, SECONDS)
                .pollingEvery(poolingInterval, SECONDS);

        return wait.until(function);
    }

    private static Object waitUntil(Function function, int timeOutInSeconds, int poolingInterval, TimeUnit poolingIntervalUnit) {
        Wait<WebDriver> wait = new FluentWait<>(driver)
                .withTimeout(timeOutInSeconds, SECONDS)
                .pollingEvery(poolingInterval, poolingIntervalUnit);

        return wait.until(function);
    }

    private static Object waitUntil(Function function, int timeOutInSeconds, int poolingInterval, TimeUnit poolingIntervalUnit, Exception ignoredException) {
        Wait<WebDriver> wait = new FluentWait<>(driver)
                .withTimeout(timeOutInSeconds, SECONDS)
                .pollingEvery(poolingInterval, poolingIntervalUnit)
                .ignoring(ignoredException.getClass());

        return wait.until(function);
    }

    private static Object waitUntil(Function function, int timeOutInSeconds, int poolingInterval, TimeUnit poolingIntervalUnit, Exception ignoredException, String sMessage) {
        Wait<WebDriver> wait = new FluentWait<>(driver)
                .withTimeout(timeOutInSeconds, SECONDS)
                .pollingEvery(poolingInterval, poolingIntervalUnit)
                .ignoring(ignoredException.getClass())
                .withMessage(sMessage);

        return wait.until(function);
    }

    static String getCurrentMethodName() {
        return Thread.currentThread().getStackTrace()[2].getMethodName();
    }
}

