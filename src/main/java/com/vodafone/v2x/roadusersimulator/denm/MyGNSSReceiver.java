package com.vodafone.v2x.roadusersimulator.denm;

import com.vodafone.v2x.sdk.android.facade.LocationProvider;
import com.vodafone.v2x.sdk.android.facade.V2XSDK;
import com.vodafone.v2x.sdk.android.facade.models.GpsLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MyGNSSReceiver extends LocationProvider implements Runnable {

    private static Logger logger = LogManager.getLogger(MyGNSSReceiver.class);
    private boolean isRunning=false;
    private GpsLocation fixedLocation ;
    private Thread thread ;
    private final Object lock = new Object();
    private static MyGNSSReceiver uniqueInstance = new MyGNSSReceiver();

    private MyGNSSReceiver() {
        super("MyGNSSReceiver");
    }

    static MyGNSSReceiver getInstance() {
        return uniqueInstance;
    }

    @Override
    protected boolean turnOn() {
        logger.debug("turnOn()");
        thread = new Thread(this);
        thread.start();
        return true;
    }

    @Override
    protected void turnOff() {
        logger.debug("turnOff()");
        synchronized (lock) {
            isRunning = false;
            lock.notifyAll();
        }
    }

    @Override
    public void run() {
        isRunning=true;
        while (isRunning) {

            fixedLocation = new GpsLocation(43.1234,7.1234,450d,90f,10f,null, V2XSDK.getInstance().getUTCTimeInMs());
            injectLocation(fixedLocation);
            try {
                synchronized(lock) {
                    Thread.sleep(1000L);
                }
            } catch (InterruptedException e) {
                logger.error("E40",e);
            }
        }
    }
}
