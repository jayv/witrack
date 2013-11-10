package be.pursuit.witrack;

import be.pursuit.witrack.trilaterate.TrilaterateRunnable;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Jo Voordeckers - jo.voordeckers@pursuit.be
 */
@Singleton
public class Timer {

    private ScheduledExecutorService scheduledThreadPoolExecutor;

    @Inject
    private TrilaterateRunnable trilaterateRunnable;

    public void startTimers(){
        scheduledThreadPoolExecutor = Executors.newSingleThreadScheduledExecutor();
        scheduledThreadPoolExecutor.scheduleAtFixedRate(trilaterateRunnable, 0, 1, TimeUnit.MINUTES);
    }

    public void stopTimers() {
        scheduledThreadPoolExecutor.shutdown();
    }

}
