package be.pursuit.witrack.mongo;


import org.bson.types.ObjectId;

import java.util.Date;

/**
 * @author Jo Voordeckers - jo.voordeckers@pursuit.be
 */
public class Scan {

    private ObjectId _id;
    private String deviceId;
    private String scannerId;
    private Date firstSeen;
    private Date lastSeen;
    private int power;
    private int packets;

    public Scan() {
    }

    public Scan(final String scannerId, final be.pursuit.witrack.json.collector.Scan scan) {
        this.scannerId = scannerId;
        deviceId = scan.getMac();
        firstSeen = scan.getFirstSeen();
        lastSeen = scan.getLastSeen();
        power = scan.getPower();
        packets = scan.getPackets();
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

    public Date getFirstSeen() {
        return firstSeen;
    }

    public void setFirstSeen(final Date firstSeen) {
        this.firstSeen = firstSeen;
    }

    public Date getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(final Date lastSeen) {
        this.lastSeen = lastSeen;
    }

    public int getPower() {
        return power;
    }

    public void setPower(final int power) {
        this.power = power;
    }

    public int getPackets() {
        return packets;
    }

    public void setPackets(final int packets) {
        this.packets = packets;
    }

    public String getScannerId() {
        return scannerId;
    }

    public void setScannerId(final String scannerId) {
        this.scannerId = scannerId;
    }
}
