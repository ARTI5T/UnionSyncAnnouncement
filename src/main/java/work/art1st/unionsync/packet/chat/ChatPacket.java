package work.art1st.unionsync.packet.chat;

import work.art1st.unionsync.chat.ChatMessage;
import work.art1st.unionsync.packet.Payload;

public class ChatPacket extends Payload<ChatMessage> {
    private static final SubProtocol subProtocol = new SubProtocol(
            ChatSubprotocol.name,
            ChatSubprotocol.version,
            "MSG"

    );
    @Override
    protected SubProtocol subProtocol() {
        return subProtocol;
    }
}
