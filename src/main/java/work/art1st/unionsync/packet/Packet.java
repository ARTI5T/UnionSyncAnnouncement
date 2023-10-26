package work.art1st.unionsync.packet;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

public class Packet {
    public static final Gson gson = new Gson();
    private static final Map<String, Class<? extends Packet>> registry = new HashMap<>();
    private static final Map<Class<? extends Packet>, String> revRegistry = new HashMap<>();

    static {
        registry.put("CLIENT_AUTH", ClientAuth.class);
        revRegistry.put(ClientAuth.class, "CLIENT_AUTH");
        registry.put("AUTH_SUCCESS", AuthSuccess.class);
        revRegistry.put(AuthSuccess.class, "AUTH_SUCCESS");
        registry.put("ERROR", Error.class);
        revRegistry.put(Error.class, "ERROR");
        registry.put("PAYLOAD", PayloadRaw.class);
        revRegistry.put(Payload.class, "PAYLOAD");
    }

    public static Packet loads(String rawPacket) throws UnknownPacketException {
        JsonObject packet = new Gson().fromJson(rawPacket, JsonObject.class);
        String type = packet.get("type").getAsString();
        if (registry.containsKey(type)) {
            return gson.fromJson(packet.get("content"), registry.get(type));
        }
        throw new UnknownPacketException();
    }

    public static String dumps(Packet src) throws UnknownPacketException {
        Class<?> clazz = src.getClass();
        while (!revRegistry.containsKey(clazz)) {
            clazz = clazz.getSuperclass();
            if (clazz.equals(Packet.class)) {
                throw new UnknownPacketException();
            }
        }
        if (src instanceof Payload) {
            ((Payload<?>) src).setSubProtocol();
        }
        String type = revRegistry.get(clazz);
        JsonElement content = gson.toJsonTree(src);
        JsonObject json = new JsonObject();
        json.addProperty("type", type);
        json.add("content", content);
        return gson.toJson(json);
    }

}
