package me.mklv.handlib.neoforge;

import java.util.function.Predicate;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public final class NeoForgeVersionCompat {
    private NeoForgeVersionCompat() {
    }

    public static Predicate<CommandSourceStack> ownerPermission() {
        return source -> source.hasPermission(4);
    }

    @SuppressWarnings("null")
    public static ClickEvent runCommand(String command) {
        return new ClickEvent(ClickEvent.Action.RUN_COMMAND, command);
    }

    @SuppressWarnings("null")
    public static ClickEvent suggestCommand(String command) {
        return new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command);
    }

    @SuppressWarnings("null")
    public static HoverEvent showText(Component component) {
        return new HoverEvent(HoverEvent.Action.SHOW_TEXT, component);
    }

    @SuppressWarnings("null")
    public static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> payloadType(String namespace, String path) {
        return new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(namespace, path));
    }
}