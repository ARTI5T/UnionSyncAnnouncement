package work.art1st.unionsync;
import java.net.Socket;
import java.net.URI;
import java.util.concurrent.Semaphore;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.audience.Audience;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import work.art1st.unionsync.announcement.Settings;
import work.art1st.unionsync.packet.*;
import work.art1st.unionsync.packet.announcement.ListPacket;
import work.art1st.unionsync.packet.announcement.QueryPacket;

import javax.net.ssl.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

public class UnionSyncWebSocketClient extends WebSocketClient {
    @Getter
    @Setter
    private static UnionSyncWebSocketClient instance;
    private static boolean alive = true;
    public interface Callback {
        void call(Packet response);
    }

    enum State {
        Auth,
        Payload
    }
    private final URI serverUri;
    private final ClientAuth clientAuth;
    private State state;
    private final Semaphore synchronizeLock = new Semaphore(1);
    private Callback pendingCallback;
    private final Callback newAnnouncementCallback;
    private final Logger logger;
    @Setter
    private Settings settings;

    public static void initialize(URI serverUri, ClientAuth clientAuth, Logger logger, Settings settings, Callback newAnnouncementCallback) {
        instance = new UnionSyncWebSocketClient(serverUri, clientAuth, logger, settings, newAnnouncementCallback);
        instance.connect();
    }

    private UnionSyncWebSocketClient(URI serverUri, ClientAuth clientAuth, Logger logger, Settings settings, Callback newAnnouncementCallback) {
        super(serverUri);
        trustAllHosts();
        this.serverUri = serverUri;
        this.clientAuth = clientAuth;
        this.state = State.Auth;
        this.settings = settings;
        this.logger = logger;
        this.newAnnouncementCallback = newAnnouncementCallback;
    }

    public void sendPacket(Packet content, Callback callback) throws InterruptedException, UnknownPacketException {
        if (this.isOpen()) {
            if (this.synchronizeLock.tryAcquire(15, TimeUnit.SECONDS)) {
                this.pendingCallback = callback;
                this.send(Packet.dumps(content));
            } else {
                this.logger.warn("Failed to acquire lock!");
            }
        }
    }

    public void sendAsyncPacket(Packet content) throws InterruptedException, UnknownPacketException {
        if (this.isOpen()) {
            if (this.synchronizeLock.tryAcquire(15, TimeUnit.SECONDS)) {
                this.pendingCallback = null;
                this.send(Packet.dumps(content));
                this.synchronizeLock.release();
            } else {
                this.logger.warn("Failed to acquire lock!");
            }
        }
    }

    public void queryAnnouncementList(Audience audience) {
        try {
            this.sendPacket(new QueryPacket(), response -> {
                if (response instanceof PayloadRaw) {
                    try {
                        Payload<?> payload = ((PayloadRaw) response).loads();
                        if (payload instanceof ListPacket) {
                            audience.sendMessage(((ListPacket) payload).asComponent(this.settings));
                        } else {
                            throw new UnknownPacketException();
                        }
                    } catch (UnknownPacketException e) {
                        logger.error("Received unknown packet while fetching announcement list.");
                    }
                }
            });
        } catch (InterruptedException | UnknownPacketException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onOpen(ServerHandshake handshakeData) {
        try {
            this.sendPacket(this.clientAuth, response -> {
                if (response instanceof AuthSuccess) {
                    this.state = State.Payload;
                } else {
                    logger.error("Login failed.");
                }
            });
        } catch (InterruptedException | UnknownPacketException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onMessage(String message) {
        try {
            Packet content = Packet.loads(message);
            if (this.pendingCallback != null) {
                this.pendingCallback.call(content);
            } else {
                Payload<?> payload = ((PayloadRaw) content).loads();
                if (payload instanceof ListPacket) {
                    // New announcement arrives
                    this.newAnnouncementCallback.call(payload);
                }
            }
        } catch (UnknownPacketException e) {
            // Unknown packet, ignore
        } finally {
            if (this.pendingCallback != null) {
                this.pendingCallback = null;
                this.synchronizeLock.release();
            }
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        if (this.synchronizeLock.availablePermits() == 0) {
            this.synchronizeLock.release();
        }
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            // Ignore
        }
        logger.error("Connection closed: " + code);
        if (alive) {
            instance = new UnionSyncWebSocketClient(serverUri, clientAuth, logger, settings, newAnnouncementCallback);
            instance.connect();
        }
    }

    @Override
    public void onError(Exception ex) {
        System.err.println("Error occurred: " + ex);
    }

    public static void kill() {
        alive = false;
        getInstance().close();
    }

    // TODO: Use public key
    void trustAllHosts() {
        TrustManager[] trustAllCerts = new TrustManager[]{new X509ExtendedTrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s, Socket socket) throws CertificateException {

            }

            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s, Socket socket) throws CertificateException {

            }

            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s, SSLEngine sslEngine) throws CertificateException {

            }

            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s, SSLEngine sslEngine) throws CertificateException {

            }

            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
//                return new java.security.cert.X509Certificate[]{};
//                System.out.println("getAcceptedIssuers");
                return null;
            }

            @Override
            public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                System.out.println("checkClientTrusted");
            }

            @Override
            public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                System.out.println("checkServerTrusted");
            }
        }};

        try {

            SSLContext ssl = SSLContext.getInstance("SSL");
            ssl.init(null, trustAllCerts, new java.security.SecureRandom());

            SSLSocketFactory socketFactory = ssl.getSocketFactory();
            this.setSocketFactory(socketFactory);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
