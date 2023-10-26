package work.art1st.unionsync.packet;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import work.art1st.unionsync.packet.announcement.ListPacket;
import work.art1st.unionsync.packet.announcement.QueryPacket;

import java.util.HashMap;
import java.util.Map;

@Getter
public abstract class Payload<T> extends Packet {
    @Getter
    public static class SubProtocol {
        private final String name;
        private final int version;
        @SerializedName("packet_type")
        private final String packetType;

        public SubProtocol(String name, int version, String packetType) {
            this.name = name;
            this.version = version;
            this.packetType = packetType;
        }

        public String getTypeIdentifier() {
            return name + ":" + packetType;
        }
    }

    protected SubProtocol subprotocol;
    protected T body;

    protected static Map<String, Class<? extends Payload<?>>> subProtocolRegistry = new HashMap<>();
    protected abstract SubProtocol subProtocol();

    void setSubProtocol() {
        subprotocol = subProtocol();
    }

    static {
        Payload.subProtocolRegistry.put("ANNOUNCEMENT:LIST", ListPacket.class);
        Payload.subProtocolRegistry.put("ANNOUNCEMENT:QUERY", QueryPacket.class);
    }
}
