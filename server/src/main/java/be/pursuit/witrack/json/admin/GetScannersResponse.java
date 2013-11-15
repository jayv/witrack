package be.pursuit.witrack.json.admin;

import java.util.List;
import be.pursuit.witrack.mongo.Scanner;

/**
 * @author Jo Voordeckers - jo.voordeckers@pursuit.be
 */
public class GetScannersResponse extends AdminResponse {

    private List<Scanner> scanners;

    public GetScannersResponse(final List<Scanner> scanners) {
        this.scanners = scanners;
    }

    public List<Scanner> getScanners() {
        return scanners;
    }

    public void setScanners(final List<Scanner> scanners) {
        this.scanners = scanners;
    }
}
