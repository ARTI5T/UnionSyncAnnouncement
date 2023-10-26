package work.art1st.unionsync.packet.announcement;

import lombok.SneakyThrows;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import work.art1st.unionsync.announcement.Announcement;
import work.art1st.unionsync.announcement.Settings;
import work.art1st.unionsync.packet.Payload;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ListPacket extends Payload<Map<String, List<Announcement>>> {
    private static final SubProtocol subProtocol = new SubProtocol(
            AnnouncementSubprotocol.name,
            AnnouncementSubprotocol.version,
            "LIST"

    );
    @Override
    protected SubProtocol subProtocol() {
        return subProtocol;
    }
    @SneakyThrows
    static void ifNotNull(String format, Consumer<String> f) {
        if (format != null && format.length() > 0) {
            f.accept(format);
        }
    }
    public Component asComponent(final Settings settings) {
        TextComponent.Builder builder = Component.text();
        ifNotNull(settings.getFormatHeader(), f -> {
            builder.append(JSONComponentSerializer.json().deserialize(f))
                    .append(Component.newline());
        });
        body.forEach((channel, list) -> {
            ifNotNull(settings.getFormatChannel(), f -> {
                builder.append(JSONComponentSerializer.json().deserialize(
                        f.replace("%channel%", channel)))
                        .append(Component.newline());
            });
            int j = 0;
            for (int i = 0; i < list.size(); ++i) {
                if (j >= settings.getMaxL()) {
                    break;
                }
                Announcement a = list.get(i);
                if (!a.shouldShow()) {
                    continue;
                }
                String format;
                if (a.isNew()) {
                    format = settings.getFormatTextNew();
                } else {
                    format = settings.getFormatText();
                }
                builder.append(list.get(i).toComponent(format))
                        .append(Component.newline());
                ++j;
            }
            if (j == 0) {
                builder.append(JSONComponentSerializer.json().deserialize(settings.getEmpty()))
                        .append(Component.newline());
            }
        });
        ifNotNull(settings.getFormatFooter(), f -> {
            builder.append(JSONComponentSerializer.json().deserialize(f));
        });
        return builder.build();
    }

    public Component newAnnouncementAsComponent(final Settings settings) {
        TextComponent.Builder builder = Component.text();
        final boolean[] cont = {true};
        body.forEach((channel, list) -> {
            if (cont[0]) {
                if (list.size() > 0) {
                    Announcement a = list.get(0);
                    if (!a.shouldShow()) {
                        return;
                    }
                    if (a.isNew()) {
                        builder.append(a.toComponent(settings.getFormatBroadcastNew()));
                        // Found a new announcement
                        cont[0] = false;
                    }
                }
            }
        });
        // We should not display this
        if (cont[0]) {
            return null;
        }
        return builder.build();
    }
}
