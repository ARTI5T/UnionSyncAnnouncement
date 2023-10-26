package work.art1st.unionsync.packet;

import com.google.gson.JsonObject;
import lombok.Getter;

@Getter
public class ClientAuth extends Packet {

    private final String type;
    private final JsonObject field;
    private ClientAuth(String type, JsonObject field) {
        this.type = type;
        this.field = field;
    }

    public static ClientAuth defaultAuthPacket(String id, String token) {
        JsonObject field = new JsonObject();
        field.addProperty("id", id);
        field.addProperty("token", token);
        return new ClientAuth("DEFAULT", field);
    }

    public static ClientAuth unionAuthPacket(String id, String token) {
        JsonObject field = new JsonObject();
        field.addProperty("id", id);
        field.addProperty("token", token);
        return new ClientAuth("UNION", field);
    }
}
