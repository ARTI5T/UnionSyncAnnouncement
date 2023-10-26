package work.art1st.unionsync.announcement;

import com.electronwill.nightconfig.core.conversion.ObjectConverter;
import com.electronwill.nightconfig.core.file.FileConfig;
import com.electronwill.nightconfig.core.file.FileConfigBuilder;
import com.velocitypowered.api.command.SimpleCommand;
import net.kyori.adventure.text.Component;
import work.art1st.unionsync.UnionSyncWebSocketClient;

public class AnnouncementCommand implements SimpleCommand {
    FileConfigBuilder fileConfigBuilder;
    public AnnouncementCommand(FileConfigBuilder fileConfigBuilder) {
        this.fileConfigBuilder = fileConfigBuilder;
    }

    @Override
    public void execute(Invocation invocation) {
        String[] args = invocation.arguments();
        if (args.length == 1 && args[0].equals("reload")) {
            FileConfig fileConfig = this.fileConfigBuilder.build();
            fileConfig.load();
            Settings settings = new ObjectConverter().toObject(fileConfig, Settings::new);
            fileConfig.close();
            UnionSyncWebSocketClient.getInstance().setSettings(settings);
            invocation.source().sendMessage(Component.text("Reloading complete."));
        } else {
            UnionSyncWebSocketClient.getInstance().queryAnnouncementList(invocation.source());
        }
    }

    @Override
    public boolean hasPermission(final SimpleCommand.Invocation invocation) {
        String[] args = invocation.arguments();
        if (args.length == 1 && args[0].equals("reload")) {
            return invocation.source().hasPermission("unionsync.command.reload");
        } else {
            return true;
        }
    }
}
