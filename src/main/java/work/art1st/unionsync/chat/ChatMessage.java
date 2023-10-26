package work.art1st.unionsync.chat;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import lombok.Data;
import net.kyori.adventure.text.Component;

@Data
public class ChatMessage {
    private String server;
    private String source;
    private String channel;
    private transient Component content;
    @SerializedName("content")
    private JsonElement contentJson;
}
