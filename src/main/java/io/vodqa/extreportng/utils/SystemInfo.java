package io.vodqa.extreportng.utils;

import java.util.Map;

/**
 * Created by SergioLeone on 11/05/2017.
 */

/**
 * Implement this interface to generate custom system information
 * and return back the {@link Map} object.
 */
public interface SystemInfo {

    Map<String, String> getSystemInfoMap(String filePath);
    
}
