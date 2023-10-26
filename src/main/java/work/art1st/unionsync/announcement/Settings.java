package work.art1st.unionsync.announcement;

import com.electronwill.nightconfig.core.conversion.Path;
import lombok.Getter;

@Getter
public class Settings {
    @Path("announcement.max-line-per-chanel")
    private int maxL;
    @Path("announcement.header")
    private String formatHeader;
    @Path("announcement.channel")
    private String formatChannel;
    @Path("announcement.text")
    private String formatText;
    @Path("announcement.text-new")
    private String formatTextNew;
    @Path("announcement.footer")
    private String formatFooter;
    @Path("announcement.empty")
    private String empty;
    @Path("announcement.broadcast-text-new")
    private String formatBroadcastNew;
}
