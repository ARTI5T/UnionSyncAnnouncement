package work.art1st.unionsync.packet.announcement;

import work.art1st.unionsync.packet.Payload;

public class QueryPacket extends Payload<QueryPacket.QueryBody> {
    private static final SubProtocol subProtocol = new SubProtocol(
            AnnouncementSubprotocol.name,
            AnnouncementSubprotocol.version,
            "QUERY"

    );
    @Override
    protected SubProtocol subProtocol() {
        return subProtocol;
    }

    public static class QueryBody {}

}
