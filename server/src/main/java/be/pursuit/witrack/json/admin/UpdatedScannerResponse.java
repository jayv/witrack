package be.pursuit.witrack.json.admin;

import be.pursuit.witrack.mongo.Scanner;

/**
 * @author Jo Voordeckers - jo.voordeckers@pursuit.be
 */
public class UpdatedScannerResponse extends AdminResponse {

    private Scanner updatedScanner;

    public UpdatedScannerResponse(final Scanner updatedScanner) {
        this.updatedScanner = updatedScanner;
    }

    public Scanner getUpdatedScanner() {
        return updatedScanner;
    }

    public void setUpdatedScanner(final Scanner updatedScanner) {
        this.updatedScanner = updatedScanner;
    }

}
