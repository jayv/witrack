package be.pursuit.witrack;

import be.pursuit.witrack.json.collector.Scan;
import be.pursuit.witrack.json.collector.ScanResult;
import be.pursuit.witrack.mongo.Database;
import be.pursuit.witrack.mongo.Scanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Date;

/**
 * @author Jo Voordeckers - jo.voordeckers@pursuit.be
 */
@Singleton
public class Ingester {

    private static final Logger LOG = LoggerFactory.getLogger(Ingester.class);

    @Inject
    private Database db;

    public void ingest(final ScanResult scanResult, final String ipAddress) {

        Date timeOfEvent = new Date();

        long offset = calculateOffset(timeOfEvent, scanResult);

        applyOffset(offset, scanResult);

        storeResult(scanResult, ipAddress);

    }

    private void storeResult(final ScanResult scanResult, final String ipAddress) {

        storeOrUpdateScanner(scanResult, ipAddress);
        storeScan(scanResult);

    }

    private void storeScan(final ScanResult scanResult) {

        LOG.trace("Storing scan from {}", scanResult.getScannerId());
        for (Scan scan : scanResult.getScans()) {
            db.scans().insert(new be.pursuit.witrack.mongo.Scan(scanResult.getScannerId(), scan));
        }
    }

    private void storeOrUpdateScanner(final ScanResult scanResult, final String ipAddress) {

        Scanner scanner = db.scanners().findOne("{ scannerId : # }", scanResult.getScannerId()).as(Scanner.class);

        if (scanner == null) {
            scanner = new Scanner();
            scanner.setScannerId(scanResult.getScannerId());
            scanner.setIpAddress(ipAddress);
            scanner.setLastUpdate(scanResult.getStamp());

            Scanner.Location location = new Scanner.Location();
            location.setX(1);
            location.setY(1);
            location.setCorrection(1);
            scanner.setLocation(location);

            LOG.info("Creating new Scanner {}, needs a position!", scanner.getScannerId());
            db.scanners().insert(scanner);

        } else {
            db.scanners().update("{ scannerId: # }", scanResult.getScannerId())
                    .with("{ $set: { ipAddress: #, lastUpdate: # } }", ipAddress, scanResult.getStamp());
        }
    }

    private void applyOffset(final long offset, final ScanResult scanResult) {

        scanResult.setStamp(new Date(scanResult.getStamp().getTime() + offset));

        for (Scan scan : scanResult.getScans()) {
            scan.setFirstSeen(new Date(scan.getFirstSeen().getTime() + offset));
            scan.setLastSeen(new Date(scan.getLastSeen().getTime() + offset));
        }

    }

    private long calculateOffset(final Date timeOfEvent, final ScanResult scanResult) {

        return timeOfEvent.getTime() - scanResult.getStamp().getTime();
    }
}
