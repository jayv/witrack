package be.pursuit.witrack.mongo;

import org.bson.types.ObjectId;

import java.util.Date;

/**
 * @author Jo Voordeckers - jo.voordeckers@pursuit.be
 */
public class Scanner {

    private ObjectId _id;

    private String scannerId;

    private String ipAddress;

    private Location location;

    private Date lastUpdate;

    public static class Location {

        private int x;

        private int y;

        private double correction;

        public double getCorrection() {
            return correction;
        }

        public void setCorrection(final double correction) {
            this.correction = correction;
        }

        public int getX() {
            return x;
        }

        public void setX(final int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(final int y) {
            this.y = y;
        }
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(final Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public ObjectId get_id() {
        return _id;
    }

    public void set_id(final ObjectId _id) {
        this._id = _id;
    }

    public String getScannerId() {
        return scannerId;
    }

    public void setScannerId(final String scannerId) {
        this.scannerId = scannerId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(final String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(final Location location) {
        this.location = location;
    }
}
