package io.vodqa.extreportng.utils;

import java.util.Map;

/**
 * Created by SergioLeone on 11/05/2017.
 */

/**
 * Implement this interface to generate custom system information
 * and return back the {@link Map<String, String>} object.
 */
public interface SystemInfo {

    /**
     *
     * @return The {@link Map<String, String>} object of system information
     */
    Map<String, String> getSystemInfoMap(String filePath);
}
