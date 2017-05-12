package io.vodqa.extreportng.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

/**
 * Created by L094540 on 25/04/2017.
 */

public class GetSystemInfo implements SystemInfo {
    @Override
    public Map<String, String> getSystemInfoMap(String filePath) {
        FileInputStream fis;
        Map<String, String> systemInfo = null;

        try {
            fis = new FileInputStream(filePath);
            ResourceBundle res = new PropertyResourceBundle(fis);
            systemInfo = new HashMap<String, String>();

            Enumeration<String> keys = res.getKeys();
            while (keys.hasMoreElements()) {
                String key = keys.nextElement();
                systemInfo.put(key, res.getString(key));
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return systemInfo;
    }
}
