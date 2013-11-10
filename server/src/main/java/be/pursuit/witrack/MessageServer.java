package be.pursuit.witrack;

import be.pursuit.witrack.json.collector.ScanResult;
import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.DataListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.net.InetSocketAddress;

/**
 * @author Jo Voordeckers - jo.voordeckers@pursuit.be
 */
@Singleton
public class MessageServer {

    private static final Logger LOG = LoggerFactory.getLogger(MessageServer.class);

    private Configuration config;

    private SocketIOServer server;

    @Inject
    private Ingester ingester;

    public void start() {

        config = new Configuration();
        // config.setHostname("localhost");
        config.setPort(3000);

        server = new SocketIOServer(config);

        server.addEventListener("scan", ScanResult.class, new DataListener<ScanResult>() {

            public void onData(final SocketIOClient socketIOClient, final ScanResult scanResult, final AckRequest ackRequest) {

                String ipAddress = ((InetSocketAddress) socketIOClient.getRemoteAddress()).getAddress().getHostAddress();
                ingester.ingest(scanResult, ipAddress);

            }
        });

        server.start();
    }

    public void stop() {

        server.stop();
    }

}
