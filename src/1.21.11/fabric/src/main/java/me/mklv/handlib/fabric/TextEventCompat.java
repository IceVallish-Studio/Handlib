package me.mklv.handlib.fabric;

import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;

public final class TextEventCompat {

    private TextEventCompat() {
    }

    public static ClickEvent runCommand(String command) {
        return new ClickEvent.RunCommand(command);
    }

    public static ClickEvent suggestCommand(String command) {
        return new ClickEvent.SuggestCommand(command);
    }

    public static HoverEvent showText(String text) {
        return new HoverEvent.ShowText(Text.literal(text));
    }
}
