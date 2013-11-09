package be.pursuit.witrack;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.DataListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Singleton;

/**
 * @author Jo Voordeckers - jo.voordeckers@pursuit.be
 */
@Singleton
public class WiTrackApplicationBean {

    private static final Logger LOG = LoggerFactory.getLogger(WiTrackApplicationBean.class);

    private Configuration config;

    private SocketIOServer server;

    public WiTrackApplicationBean() {
    }

    @PostConstruct
    public void start() {

        Configuration config = new Configuration();
//            config.setHostname("localhost");
        config.setPort(3000);

        server = new SocketIOServer(config);

        server.addEventListener("scan", ScanResult.class, new DataListener<ScanResult>() {
            public void onData(final SocketIOClient socketIOClient, final ScanResult scanResult, final AckRequest ackRequest) {

                LOG.debug("Received data: {}, from: {}", scanResult, socketIOClient.getRemoteAddress());

            }
        });

        server.start();
        LOG.info("WiTrack scanner listening...");
    }

    @PreDestroy
    public void stop() {

       LOG.info("WiTrack scanner stopping...");
       server.stop();
    }


}
