package com.vodafone.v2x.roadusersimulator.app;

import com.vodafone.v2x.roadusersimulator.app.usecases.StadiumUsecase;
import com.vodafone.v2x.roadusersimulator.app.usecases.TemplateUsecase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Application {

    private static Logger logger = LogManager.getLogger(Application.class);


    public static void main(String[] args) {
        String appName = AppProperties.get(AppProperties.PROPERTY_APPLICATION_NAME);
        String appVersion = AppProperties.get(AppProperties.PROPERTY_APPLICATION_VERSION);
        logger.info("AppName: " + appName + " , Version:" + appVersion);

        // select your use case here (uncomment ONLY 1 usecase)
        //TemplateUsecase.start();
        StadiumUsecase.start();
    }
}
