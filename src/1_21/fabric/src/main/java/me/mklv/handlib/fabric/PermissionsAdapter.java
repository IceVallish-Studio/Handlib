package me.mklv.handlib.fabric;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import java.lang.reflect.Method;

public class PermissionsAdapter {
    private static final Method PERMISSIONS_CHECK;
    private static final boolean HAS_FABRIC_PERMISSIONS;

    static {
        Method checkMethod = null;
        boolean hasFabricPermissions = false;

        try {
            // Try to load the fabric-permissions-api
            Class<?> permissionsClass = Class.forName("me.lucko.fabric.api.permissions.v0.Permissions");
            checkMethod = permissionsClass.getMethod("check", CommandSourceStack.class, String.class, int.class);
            hasFabricPermissions = true;
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            // fabric-permissions-api not available, will use vanilla fallback
        }

        PERMISSIONS_CHECK = checkMethod;
        HAS_FABRIC_PERMISSIONS = hasFabricPermissions;
    }

    public static boolean checkPermission(CommandSourceStack source, String permission, int minimumLevel) {
        if ("handshaker.bypass".equals(permission)) {
            if (HAS_FABRIC_PERMISSIONS) {
                try {
                    return (boolean) PERMISSIONS_CHECK.invoke(null, source, permission, minimumLevel);
                } catch (Exception e) {
                    return false;
                }
            }
            return false;
        }

        if (HAS_FABRIC_PERMISSIONS) {
            try {
                return (boolean) PERMISSIONS_CHECK.invoke(null, source, permission, minimumLevel);
            } catch (Exception e) {
                return source.hasPermission(minimumLevel);
            }
        }

        return source.hasPermission(minimumLevel);
    }

    public static boolean checkPermission(ServerPlayer player, String permission) {
        if ("handshaker.bypass".equals(permission)) {
            if (HAS_FABRIC_PERMISSIONS) {
                try {
                    CommandSourceStack source = player.createCommandSourceStack();
                    if (source != null) {
                        return (boolean) PERMISSIONS_CHECK.invoke(null, source, permission, Integer.MAX_VALUE);
                    }
                } catch (Exception e) {
                    return false;
                }
            }

            return false;
        }

        if (HAS_FABRIC_PERMISSIONS) {
            try {
                CommandSourceStack source = player.createCommandSourceStack();
                if (source != null) {
                    return checkPermission(source, permission, 4);
                }
            } catch (Exception e) {
                return false;
            }
        }

        return false;
    }

    public static boolean hasFabricPermissions() {
        return HAS_FABRIC_PERMISSIONS;
    }
}