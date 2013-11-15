package be.pursuit.witrack.trilaterate;

import be.pursuit.witrack.mongo.Position;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jo Voordeckers - jo.voordeckers@pursuit.be
 */
public class TrilaterationCalulator {

    private static final Logger LOG = LoggerFactory.getLogger(TrilaterationService.class);

    public Position calculate(Bucket bucket) {

        Position.Location location = new Position.Location();

        DescriptiveStatistics numeratorX = new DescriptiveStatistics();
        DescriptiveStatistics numeratorY = new DescriptiveStatistics();
        DescriptiveStatistics denominator = new DescriptiveStatistics();

        for (Bucket.Measurement measurement : bucket.getMeasurements()) {

            double weight = 1.0 / measurement.radius();

            System.out.println("Measurement:"+weight+" "+bucket.getDeviceId()+" "+measurement.getPower()+" "+measurement.getScanner().getScannerId());

            numeratorX.addValue(((double)measurement.x()) * weight);
            numeratorY.addValue(((double)measurement.y()) * weight);
            denominator.addValue(weight);
        }

        double sumRadii = denominator.getSum();

        double x = numeratorX.getSum() / sumRadii;
        double y = numeratorY.getSum() / sumRadii;

        location.setX((int) x);
        location.setY((int) y);

        Position position = new Position();
        position.setDeviceId(bucket.getDeviceId());
        position.setTime(bucket.getTime());
        position.setLocation(location);

        LOG.trace("Trilaterated device: {} x: {}  y: {} based on {} measurements @ {}", new Object[] { position.getDeviceId(), x, y, bucket.getMeasurements().size(), bucket.getTime() });

        return position;
    }

}
