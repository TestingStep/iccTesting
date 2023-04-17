package com.vodafone.v2x.roadusersimulator.roaduser;

import ch.hsr.geohash.GeoHash;
import com.vodafone.v2x.sdk.android.facade.LatLng;
import com.vodafone.v2x.sdk.android.facade.enums.StationType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.UUID;

abstract class RoadUser implements Runnable, RoadUserInterface {

    private static Logger logger = LogManager.getLogger(RoadUser.class);

    private boolean isActive = false;
    private Thread thread;
    private boolean isAnimated=false;
    private Object lock = new Object();
    private long xmitPeriodInMs = 1000L;
    private static int counter=0;
    protected int stationID;
    protected String uuid = UUID.randomUUID().toString();
    protected StationType stationType;


    public RoadUser(StationType stationType) {
        this.stationType=stationType;
        counter++;
        stationID= counter;
        logger.debug("RoadUser "+uuid+" stationType="+stationType.name()+" stationID="+stationID+" created");
    }

    public void activate() {
        isActive=true;
        logger.debug("RoadUser "+uuid+" activated");
    }

    public void suspend() {
        if (isActive) {
            isActive = false;
            setAnimation(false);
            logger.debug("RoadUser "+uuid+" suspended");
        }
    }

    public synchronized void setAnimation(boolean request) {
        if (this.isAnimated != request) {
            if (request==true) {
                thread = new Thread(this);
                this.isAnimated=true;
                thread.start();
            }else {
                this.isAnimated=false;
                synchronized(lock) {
                    lock.notifyAll();
                }
            }
        }
    }

    abstract protected void computeNewLocation() ;

    abstract protected void publishNewLocation() ;

    protected LatLng predict(LatLng from , float headingInDegree , float speedInKmH , long delayInMs ) {
        float deltaDistanceInCm = computeTraveledDistanceInCm(speedInKmH,delayInMs);
        float deltaXInCm = deltaDistanceInCm * (float)Math.sin(Math.toRadians((double) headingInDegree));
        float deltaYInCm = deltaDistanceInCm * (float)Math.cos(Math.toRadians((double) headingInDegree));
        return from.shiftThisPoint((long) deltaXInCm,(long) deltaYInCm);
    }

    private float computeTraveledDistanceInCm(float speedInKmH , long durationInMs) {
        float deltaDistanceInCm = ((speedInKmH*100000f/3600f) * (((float)durationInMs)/1000f));
        return deltaDistanceInCm;
    }

    protected String computeTopic(String msgName, double lat,double lon) {
        StringBuffer topic = new StringBuffer("v2x/").append(msgName).append("/public/g8");
        String g8 = GeoHash.withCharacterPrecision(lat,lon,8).toBase32();
        for (int i=0;i<g8.length();i++) {
            topic.append("/").append(g8.charAt(i));
        }
        return topic.toString();
    }


    @Override
    public void run() {
        logger.debug("RoadUser "+uuid+" thread started");
        while(isAnimated) {
            computeNewLocation();
            publishNewLocation();
            synchronized (lock) {
                try {
                    lock.wait(xmitPeriodInMs);
                }catch(InterruptedException e) {
                   logger.error("E69",e);
                }
            }
        }
        logger.debug("RoadUser "+uuid+" thread stopped");
    }


}
