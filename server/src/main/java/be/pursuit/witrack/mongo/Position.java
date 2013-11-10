package be.pursuit.witrack.mongo;

import org.bson.types.ObjectId;

import java.util.Date;

/**
 * @author Jo Voordeckers - jo.voordeckers@pursuit.be
 */
public class Position {

    private ObjectId _id;
    private String deviceId;
    private Date time;
    private Location location;

    public static class Location {

        private int x;

        private int y;

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

    public ObjectId get_id() {
        return _id;
    }

    public void set_id(final ObjectId _id) {
        this._id = _id;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(final String deviceId) {
        this.deviceId = deviceId;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(final Location location) {
        this.location = location;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(final Date time) {
        this.time = time;
    }
}
