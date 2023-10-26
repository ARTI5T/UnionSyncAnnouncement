package work.art1st.unionsync.packet;

import com.google.gson.JsonObject;
import lombok.SneakyThrows;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class PayloadRaw extends Payload<JsonObject> {
    @SneakyThrows
    public Payload<?> loads() throws UnknownPacketException {
        if (subProtocolRegistry.containsKey(this.subprotocol.getTypeIdentifier())) {
            Class<? extends Payload<?>> clazz = subProtocolRegistry.get(this.subprotocol.getTypeIdentifier());
            Payload<?> payload = clazz.getDeclaredConstructor().newInstance();
            Type type = ((ParameterizedType) clazz.getGenericSuperclass()).getActualTypeArguments()[0];
            payload.body = gson.fromJson(body, type);
            return payload;
        }
        throw new UnknownPacketException();
    }

    @Override
    protected SubProtocol subProtocol() {
        // Should never be called
        throw new RuntimeException();
    }
}
