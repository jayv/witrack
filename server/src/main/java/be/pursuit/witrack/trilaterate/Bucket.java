package be.pursuit.witrack.trilaterate;

import be.pursuit.witrack.mongo.Scanner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Jo Voordeckers - jo.voordeckers@pursuit.be
 */
public class Bucket {

    private Date time;
    private String deviceId;
    private List<Measurement> measurements = new ArrayList<>();

    public static class Measurement {

        private Scanner scanner;
        private int power;

        public Scanner getScanner() {
            return scanner;
        }

        public void setScanner(final Scanner scanner) {
            this.scanner = scanner;
        }

        public int getPower() {
            return power;
        }

        public void setPower(final int power) {
            this.power = power;
        }

        public int x() {
            return getScanner().getLocation().getX();
        }

        public int y() {
            return getScanner().getLocation().getY();
        }

        public int radius() {
            return (int) (getPower() * getScanner().getLocation().getCorrection());
        }

    }

    public Date getTime() {
        return time;
    }

    public void setTime(final Date time) {
        this.time = time;
    }

    public List<Measurement> getMeasurements() {
        return measurements;
    }

    public void setMeasurements(final List<Measurement> measurements) {
        this.measurements = measurements;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(final String deviceId) {
        this.deviceId = deviceId;
    }
}
