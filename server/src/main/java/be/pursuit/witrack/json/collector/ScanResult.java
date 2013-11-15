package be.pursuit.witrack.json.collector;

import java.util.Arrays;
import java.util.Date;

/**
 * @author Jo Voordeckers - jo.voordeckers@pursuit.be
 */
public class ScanResult {

    private Date stamp;
    private String scannerId;
    private Scan[] scans;

    @Override
    public String toString() {
        return "ScanResult{" +
                "stamp=" + stamp +
                ", scannerId='" + scannerId + '\'' +
                ", scans=" + Arrays.toString(scans) +
                "} ";
    }

    public Date getStamp() {
        return stamp;
    }

    public void setStamp(final Date stamp) {
        this.stamp = stamp;
    }

    public String getScannerId() {
        return scannerId;
    }

    public void setScannerId(final String scannerId) {
        this.scannerId = scannerId;
    }

    public Scan[] getScans() {
        return scans;
    }

    public void setScans(final Scan[] scans) {
        this.scans = scans;
    }
}
