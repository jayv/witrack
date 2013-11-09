package be.pursuit.witrack.json.collector;

import java.util.Date;

/**
 * @author Jo Voordeckers - jo.voordeckers@pursuit.be
 */
public class Scan {

    private String mac;
    private Date firstSeen;
    private Date lastSeen;
    private int power;
    private int packets;

    public String getMac() {
        return mac;
    }

    public void setMac(final String mac) {
        this.mac = mac;
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
}
