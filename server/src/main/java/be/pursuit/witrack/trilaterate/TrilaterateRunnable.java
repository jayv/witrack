package be.pursuit.witrack.trilaterate;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Jo Voordeckers - jo.voordeckers@pursuit.be
 */
@Singleton
public class TrilaterateRunnable implements Runnable {

    @Inject
    private TrilaterationService service;

    @Override
    public void run() {
        service.trilaterate();
    }

}
