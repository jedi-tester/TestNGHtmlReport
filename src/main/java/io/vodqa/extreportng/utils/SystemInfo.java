package io.vodqa.extreportng.utils;

import java.util.Map;

/**
 * Created by SergioLeone on 11/05/2017.
 */

/**
 * This interface is for the user who should implement this interface and generate their own system information
 * and return back the map
 */
public interface SystemInfo {

    /**
     * Add a map of system information and return as a map
     * @return The map of system information
     */
//    Map<String, String> getSystemInfoMap();
    Map<String, String> getSystemInfoMap(String filePath);
}
