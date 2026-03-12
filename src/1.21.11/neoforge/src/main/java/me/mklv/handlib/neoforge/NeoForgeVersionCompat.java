package me.mklv.handlib.neoforge;

import java.util.function.Predicate;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import static net.minecraft.commands.Commands.LEVEL_OWNERS;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public final class NeoForgeVersionCompat {
    private NeoForgeVersionCompat() {
    }

    public static Predicate<CommandSourceStack> ownerPermission() {
        return Commands.hasPermission(LEVEL_OWNERS);
    }

    public static ClickEvent runCommand(String command) {
        return new ClickEvent.RunCommand(command);
    }

    public static ClickEvent suggestCommand(String command) {
        return new ClickEvent.SuggestCommand(command);
    }

    public static HoverEvent showText(Component component) {
        return new HoverEvent.ShowText(component);
    }

    public static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> payloadType(String namespace, String path) {
        return new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(namespace, path));
    }
}