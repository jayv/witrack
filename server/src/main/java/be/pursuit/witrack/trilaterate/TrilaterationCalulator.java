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

            int radius = measurement.radius();
            int oneOverRadius = 1 / radius;
            numeratorX.addValue(measurement.x() * oneOverRadius);
            numeratorY.addValue(measurement.y() * oneOverRadius);
            denominator.addValue(radius);
        }

        double sumRadii = denominator.getSum();

        location.setX((int) (numeratorX.getSum() / sumRadii));
        location.setY((int) (numeratorY.getSum() / sumRadii));

        Position position = new Position();
        position.setDeviceId(bucket.getDeviceId());
        position.setTime(bucket.getTime());
        position.setLocation(location);

        LOG.trace("Trilaterated device: {} x: {}  y: {} based on {} measurements", new Object[] { position.getDeviceId(), position.getLocation().getX(), location.getY(), bucket.getMeasurements().size() });

        return position;
    }

}
