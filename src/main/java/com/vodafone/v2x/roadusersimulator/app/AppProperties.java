package com.vodafone.v2x.roadusersimulator.app;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.util.Properties;


public class AppProperties {

    // ------ Properties NAME-----------------------------------------------
    public static final String PROPERTY_APPLICATION_NAME = "appName";
    public static final String PROPERTY_APPLICATION_VERSION = "appVersion";

    //------------------------------------------------------------------------
    private static Logger logger = LogManager.getLogger(AppProperties.class);
    private static String PROPFILENAME = "config.properties";


    // Private constructor
    private AppProperties() {
    }


    public static synchronized String get(String property) {
        String reply = null;
        // Read properties
        try {
            Properties prop = new Properties();
            InputStream inputStream = AppProperties.class.getClassLoader().getResourceAsStream(PROPFILENAME);
            if (inputStream != null) {
                prop.load(inputStream);
                logger.debug("Reading the property: " + property + " from the file " + PROPFILENAME);
                reply = prop.getProperty(property);
            } else {
                logger.error("property file '" + PROPFILENAME + "' not found in the classpath");
            }
        } catch (Exception e) {
            logger.error("E31 Exception while Reading propertie", e);
        }
        logger.debug("Property(" + property + ") = " + reply);
        return reply;
    }

}
