package io.vodqa.extreportng.extras;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;

import java.io.IOException;

/**
 * Created by SergioLeone on 12/05/2017.
 */
public class CustomConditions {

    private static final Logger log = LogManager.getLogger(CustomConditions.class);

    static ExpectedCondition<Boolean> isElementInViewport(WebElement element) throws IOException {
        log.debug("Executing custom condition: " + SeleUtil.getCurrentMethodName());

        final String jsScript = SeleUtil.JSHelper.getScriptStringFromFile("isElementInViewport.js");

        return new ExpectedCondition<Boolean>() {

            @Override
            public Boolean apply(WebDriver d) {
                log.debug("Waiting for element to be visible in viewport");
                log.debug(SeleUtil.JSHelper.executeScript(jsScript, element));
                return ((Boolean) SeleUtil.JSHelper.executeScript(jsScript, element));
            }

            @Override
            public String toString() {
                return "element to be completely visible in viewport";
            }
        };
    }
}
