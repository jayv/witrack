package be.pursuit.witrack.trilaterate;

import be.pursuit.witrack.mongo.Database;
import be.pursuit.witrack.mongo.Position;
import be.pursuit.witrack.mongo.Scan;
import be.pursuit.witrack.mongo.Scanner;
import com.google.common.base.Function;
import com.google.common.collect.*;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.ForkJoinPool;

/**
 * @author Jo Voordeckers - jo.voordeckers@pursuit.be
 */
@Singleton
public class TrilaterationService {

    private static final Logger LOG = LoggerFactory.getLogger(TrilaterationService.class);

    private static final int ONE_MINUTES = 1 * 1000 * 60;

    private static final int BUCKET_WINDOW = 60 * 1000; // 15s window

    private static final int CPU_CORES = Runtime.getRuntime().availableProcessors();

    private static final Comparator<Scan> SCAN_COMP_BY_DEV_AND_TIME = new Comparator<Scan>() {
        @Override
        public int compare(final Scan s1, final Scan s2) {

            int devComp = s1.getDeviceId().compareTo(s2.getDeviceId());

            if (devComp == 0) {
                return s1.getLastSeen().compareTo(s2.getLastSeen());
            }

            return devComp;

        }
    };
    private static final int MIN_SCANS_PER_DEVICE = 3;

    @Inject
    private Database db;

    @Inject
    private TrilaterationCalulator calulator;

    private ForkJoinPool pool;

    @PostConstruct
    public void init() {
        pool = new ForkJoinPool(CPU_CORES);
    }

    public void trilaterate() {

        try {

            Date oneMinutesAgo = new Date(System.currentTimeMillis() - ONE_MINUTES);

            Iterable<Scan> scanIterable = db.scans().find().as(Scan.class);

            List<Scan> scans = Lists.newArrayList(scanIterable);

            processScans(scans);

            db.scans().remove("{ lastSeen: { $lt: #} }", oneMinutesAgo);

        } catch (Exception e) {
            LOG.error("Failed to trilaterate", e);
        }
    }

    private void processScans(final List<Scan> scans) {

        LOG.trace("Processing {} scans for trilateration", scans.size());

        Collections.sort(scans, SCAN_COMP_BY_DEV_AND_TIME);

        List<Bucket> buckets = bucketize(scans);

        for (Bucket bucket : buckets) {

            if (bucket.getMeasurements().size() >= MIN_SCANS_PER_DEVICE) {
                Position position = calulator.calculate(bucket);
                db.positions().insert(position);
            } else {


                StringBuffer sb = new StringBuffer();

                for (Bucket.Measurement measurement : bucket.getMeasurements()) {
                    sb.append(" scanner: ").append(measurement.getScanner().getScannerId()).append(" power: ").append(measurement.getPower());
                }

                LOG.debug("Skipped bucket for {} @{} : {}", bucket.getDeviceId(), bucket.getTime(), sb);
            }
        }

    }

    private List<Bucket> bucketize(final List<Scan> scans) {

        Map<String, Scanner> scanners = fetchScanners();

        List<Bucket> bucketList = Lists.newArrayList();

        Bucket bucket = new Bucket();

        for (Scan scan : scans) {

            if (bucket.getDeviceId() == null) {
                bucket.setTime(scan.getLastSeen());
                bucket.setDeviceId(scan.getDeviceId());

            } else if (!bucket.getDeviceId().equals(scan.getDeviceId())
                    || !inTimeRange(scan.getLastSeen(), bucket.getTime())) {

                averageMeasures(bucket);
                bucketList.add(bucket);
                bucket = new Bucket();
                bucket.setDeviceId(scan.getDeviceId());
                bucket.setTime(scan.getLastSeen());
            }

            Bucket.Measurement measurement = new Bucket.Measurement();
            measurement.setScanner(scanners.get(scan.getScannerId()));
            measurement.setPower(scan.getPower());
            bucket.getMeasurements().add(measurement);
        }

        return bucketList;
    }

    private void averageMeasures(final Bucket bucket) {

        Map<Scanner, List<Bucket.Measurement>> map = new HashMap<>();

        for (Bucket.Measurement measurement : bucket.getMeasurements()) {

            List<Bucket.Measurement> measurements = map.get(measurement.getScanner());

            if (measurements == null) {
                measurements = new ArrayList<>();
                map.put(measurement.getScanner(), measurements);
            }
            measurements.add(measurement);
        }
        bucket.setMeasurements(new ArrayList<Bucket.Measurement>());

        for (Map.Entry<Scanner, List<Bucket.Measurement>> entry : map.entrySet()) {

            DescriptiveStatistics scans = new DescriptiveStatistics();

            for (Bucket.Measurement measurement : entry.getValue()) {

                scans.addValue(measurement.getPower());
            }

            Bucket.Measurement measurement = new Bucket.Measurement();
            measurement.setPower((int) Math.round(scans.getMean()));
            measurement.setScanner(entry.getKey());

            bucket.getMeasurements().add(measurement);
        }

    }

    private boolean inTimeRange(Date scan, Date bucketTime) {
        return bucketTime.getTime() + BUCKET_WINDOW > scan.getTime();
    }

    private Map<String, Scanner> fetchScanners() {
        Iterable<Scanner> scannerIterable = db.scanners().find().as(Scanner.class);
        return Maps.uniqueIndex(scannerIterable, new Function<Scanner, String>() {
            @Override
            public String apply(final be.pursuit.witrack.mongo.Scanner scanner) {
                return scanner.getScannerId();
            }
        });
    }

    @PreDestroy
    public void destroy() {
        pool.shutdown();
    }

}
