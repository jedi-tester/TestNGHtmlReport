package io.vodqa.extreportng.extras;

import org.openqa.selenium.WebDriver;

/**
 * Created by SergioLeone on 15/05/2017.
 */
public interface SeleDriver {

    static WebDriver getSeleDriver(WebDriver driver) {
        return driver;
    }

}
