package work.art1st.unionsync;

import com.electronwill.nightconfig.core.conversion.ObjectConverter;
import com.electronwill.nightconfig.core.file.FileConfig;
import com.electronwill.nightconfig.core.file.FileConfigBuilder;
import com.google.inject.Inject;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.SneakyThrows;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;
import work.art1st.unionsync.announcement.AnnouncementCommand;
import work.art1st.unionsync.announcement.Settings;
import work.art1st.unionsync.packet.ClientAuth;
import work.art1st.unionsync.packet.announcement.ListPacket;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.util.Objects;

@Plugin(
        id = "union-sync",
        name = "UnionSync",
        authors = {"__ART1st__"},
        version = BuildConstants.VERSION
)
public class UnionSync {
    private final ProxyServer proxy;
    private final Logger logger;
    private final Path dataDirectory;

    @Inject
    public UnionSync(ProxyServer proxy, Logger logger, @DataDirectory Path dataDirectory) {
        this.proxy = proxy;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @SneakyThrows
    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        copyResourceFile("config.toml");
        FileConfigBuilder builder = (FileConfigBuilder) FileConfig
                .builder(this.dataDirectory.resolve("config.toml"))
                .defaultResource(Objects.requireNonNull(this.getClass().getResource("/config.toml")).getPath())
                .sync();
        FileConfig fileConfig = builder.build();
        fileConfig.load();

        URI serverUri = URI.create(fileConfig.get("server"));

        String type = fileConfig.get("auth.type");
        String id = fileConfig.get("auth.id");
        String token = fileConfig.get("auth.token");
        Settings settings = new ObjectConverter().toObject(fileConfig, Settings::new);

        fileConfig.close();

        ClientAuth auth;
        if (type.equalsIgnoreCase("union")) {
            auth = ClientAuth.unionAuthPacket(id, token);
        } else {
            auth = ClientAuth.defaultAuthPacket(id, token);
        }

        UnionSyncWebSocketClient.initialize(serverUri, auth, this.logger, settings, content -> {
            Component msg = ((ListPacket) content).newAnnouncementAsComponent(settings);
            if (msg != null) {
                this.proxy.getAllPlayers().forEach(player -> {
                    player.sendMessage(msg);
                });
            }
        });

        proxy.getCommandManager().register("announcement", new AnnouncementCommand(builder), "ua");
    }

    @Subscribe
    public void onPlayerChooseInitialServer(PlayerChooseInitialServerEvent event) {
        if (UnionSyncWebSocketClient.getInstance().isOpen()) {
            UnionSyncWebSocketClient.getInstance().queryAnnouncementList(event.getPlayer());
        }
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        this.logger.info("Plugin disabled.");
        UnionSyncWebSocketClient.kill();
    }

    private void copyResourceFile(String filename) throws IOException {
        File file = this.dataDirectory.resolve(filename).toFile();
        if (file.exists()) {
            return;
        }
        file.delete();
        file.getParentFile().mkdirs();
        file.createNewFile();
        InputStream is = this.getClass().getResourceAsStream("/" + filename);
        FileOutputStream fos = new FileOutputStream(this.dataDirectory.resolve(filename).toString());
        byte[] b = new byte[1024];
        int length;
        while ((length = is.read(b)) > 0) {
            fos.write(b, 0, length);
        }
        is.close();
        fos.close();
    }
}
