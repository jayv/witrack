package be.pursuit.witrack;

import be.pursuit.witrack.mongo.Database;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Jo Voordeckers - jo.voordeckers@pursuit.be
 */
@Singleton
public class WiTrackApplication {

    private static final Logger LOG = LoggerFactory.getLogger(WiTrackApplication.class);

    @Inject
    private MessageServer messageServer;

    @Inject
    private Timer timer;

    @Inject
    private Database db;

    @PostConstruct
    public void start() {
        LOG.info("Launching WiTrack scanner...");
        db.init();
        messageServer.start();
        timer.startTimers();
        LOG.info("WiTrack scanner listening...");
    }

    @PreDestroy
    public void stop() {
        LOG.info("WiTrack scanner stopping...");
        timer.stopTimers();
        messageServer.stop();
        db.stop();
        LOG.info("WiTrack scanner stopped.");
    }

}
