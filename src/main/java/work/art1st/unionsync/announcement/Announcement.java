package work.art1st.unionsync.announcement;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import org.apache.commons.text.StringEscapeUtils;

import java.text.SimpleDateFormat;
import java.util.List;

@Getter
public class Announcement {

    protected String title;
    protected JsonElement content;
    @SerializedName("author_id")
    protected String authorId;
    protected String channel;
    protected List<String> tags;
    @SerializedName("time_created")
    protected long timeCreated;
    @SerializedName("time_expires")
    protected long timeExpires;
    protected JsonElement meta;
    protected JsonElement targets;

    protected static final SimpleDateFormat formatter = new SimpleDateFormat("MM/dd");

    protected String contentAsText() {
        StringBuilder stringBuilder = new StringBuilder();
        if (content.isJsonArray()) {
            content.getAsJsonArray().forEach(jsonElement -> {
                JsonArray array = jsonElement.getAsJsonArray();
                if (array.get(0).getAsString().equals("text")) {
                    stringBuilder.append(array.get(1).getAsString()).append('\n');
                }
            });
        } else {
            stringBuilder.append(content.getAsString());
        }
        if (stringBuilder.charAt(stringBuilder.length() - 1) == '\n') {
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        }
        return stringBuilder.toString();
    }

    public Component toComponent(final String jsonFormatPattern) {
        String replaced;
        replaced = jsonFormatPattern.replace("%start-date%", formatter.format(this.timeCreated * 1000L));
        replaced = replaced.replace("%end-date%", formatter.format(this.timeExpires * 1000L));
        replaced = replaced.replace("%title%", StringEscapeUtils.escapeJson(this.title));
        replaced = replaced.replace("%content%", StringEscapeUtils.escapeJson(this.contentAsText()));
        replaced = replaced.replace("%author%", this.authorId);
        replaced = replaced.replace("%channel%", this.channel);
        return JSONComponentSerializer.json().deserialize(replaced);
    }

    public boolean isNew() {
        if (this.meta.isJsonObject()) {
            if (((JsonObject) this.meta).has("is_new")) {
                return ((JsonObject) this.meta).get("is_new").getAsBoolean();
            }
        }
        return false;
    }

    public boolean shouldShow() {
        if ((this.targets == null) || (this.targets.isJsonNull())) {
            return true;
        }
        if (this.targets.isJsonArray()) {
            return this.targets.getAsJsonArray().size() == 0;
        }
        return false;
    }
}
