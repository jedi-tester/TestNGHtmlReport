package io.vodqa.extreportng.utils;

/**
 * Created by SergioLeone on 12/05/2017.
 */

/**
 * A helper class to set and get node names.
 */
public class TestNodeName {
    private static final ThreadLocal<String> nodeName = new ThreadLocal<>();

    /**
     * Gets the name of the node.
     * @return node name string
     */
    public static String getNodeName() {
        return nodeName.get();
    }

    /**
     * Sets the name for the node
     * @param name name to be set for the node
     */
    public static void setNodeName(String name) {
        nodeName.set(name);
    }
}
