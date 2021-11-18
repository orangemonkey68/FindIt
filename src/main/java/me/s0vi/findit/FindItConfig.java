package me.s0vi.findit;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = "findit")
public class FindItConfig implements ConfigData {
    String faceColor = "#FFFFFF";
    String edgeColor = "#FFFFFF";

    public int getFaceColor() {
        String str = faceColor.replace("#", "");
        return Integer.decode(str);
    }

    public int getEdgeColor() {
        String str = faceColor.replace("#", "");
        return Integer.decode(str);
    }
}
