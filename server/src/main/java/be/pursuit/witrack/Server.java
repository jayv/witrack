package be.pursuit.witrack;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jo Voordeckers - jo.voordeckers@pursuit.be
 */
public class Server {

    private static final Logger LOG = LoggerFactory.getLogger(Server.class);

    public Server() {

        final Weld weld = new Weld();

        WeldContainer container = weld.initialize();

        container.instance().select(WiTrackApplicationBean.class).get();

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                LOG.info("Process shutdown received initiating shutdown");
                weld.shutdown();
            }
        }));

    }

    public static void main(String[] args) {
           new Server();
    }
}
