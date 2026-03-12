package me.mklv.handlib.fabric;

import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;

public final class TextEventCompat {

    private TextEventCompat() {
    }

    public static ClickEvent runCommand(String command) {
        return new ClickEvent(ClickEvent.Action.RUN_COMMAND, command);
    }

    public static ClickEvent suggestCommand(String command) {
        return new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command);
    }

    public static HoverEvent showText(String text) {
        return new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(text));
    }
}
