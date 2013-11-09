package be.pursuit.witrack;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

/**
 * @author Jo Voordeckers - jo.voordeckers@pursuit.be
 */
public class Server {

    public Server() {

        final Weld weld = new Weld();

        WeldContainer container = weld.initialize();

        container.instance().select(WiTrackApplication.class).get();

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                weld.shutdown();
            }
        }));
    }

    public static void main(String[] args) {
        new Server();
    }
}
