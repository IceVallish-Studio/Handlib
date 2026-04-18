package me.mklv.handlib.common;

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
        } catch (Exception e) {
            // fabric-permissions-api not available
        }

        PERMISSIONS_CHECK = checkMethod;
        HAS_FABRIC_PERMISSIONS = hasFabricPermissions;
    }

    public static boolean checkPermission(CommandSourceStack source, String permission, int minimumLevel) {
        if ("handshaker.bypass".equals(permission)) {
            if (HAS_FABRIC_PERMISSIONS && PERMISSIONS_CHECK != null) {
                try {
                    return (boolean) PERMISSIONS_CHECK.invoke(null, source, permission, Integer.MAX_VALUE);
                } catch (Exception e) {
                    // fall through
                }
            }
            return source.hasPermission(4);
        }

        if (HAS_FABRIC_PERMISSIONS && PERMISSIONS_CHECK != null) {
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
            if (HAS_FABRIC_PERMISSIONS && PERMISSIONS_CHECK != null) {
                try {
                    CommandSourceStack source = player.createCommandSourceStack();
                    if (source != null) {
                        return (boolean) PERMISSIONS_CHECK.invoke(null, source, permission, Integer.MAX_VALUE);
                    }
                } catch (Exception e) {
                    // fall through
                }
            }
            return fallbackHasPermission(player, 4);
        }

        if (HAS_FABRIC_PERMISSIONS && PERMISSIONS_CHECK != null) {
            try {
                CommandSourceStack source = player.createCommandSourceStack();
                if (source != null) {
                    return checkPermission(source, permission, 4);
                }
            } catch (Exception e) {
                return false;
            }
        }

        return fallbackHasPermission(player, 4);
    }

    public static boolean hasFabricPermissions() {
        return HAS_FABRIC_PERMISSIONS;
    }

    private static boolean fallbackHasPermission(ServerPlayer player, int minimumLevel) {
        try {
            // Use reflection for hasPermissions to avoid potential binary compatibility issues with getServer()
            Method hasPermissions = player.getClass().getMethod("hasPermissions", int.class);
            return (boolean) hasPermissions.invoke(player, minimumLevel);
        } catch (Exception ignored) {
        }

        try {
            Object server = resolveServer(player);
            if (server == null) {
                return false;
            }

            Object playerList = server.getClass().getMethod("getPlayerList").invoke(server);
            Object identity = resolveIdentity(player);
            if (playerList == null || identity == null) {
                return false;
            }

            Method getProfilePermissions = playerList.getClass().getMethod("getProfilePermissions", identity.getClass());
            Object permissions = getProfilePermissions.invoke(playerList, identity);
            if (permissions instanceof Number level) {
                return level.intValue() >= minimumLevel;
            }
            if (permissions != null) {
                try {
                    Method getPermissionLevel = permissions.getClass().getMethod("getPermissionLevel");
                    Object level = getPermissionLevel.invoke(permissions);
                    if (level instanceof Number number) {
                        return number.intValue() >= minimumLevel;
                    }
                } catch (Exception ignored) {
                }
            }
        } catch (Exception ignored) {
        }

        return false;
    }

    private static Object resolveServer(Object sourceOrPlayer) {
        if (sourceOrPlayer == null) {
            return null;
        }

        String[] methods = {"getServer", "server"};
        for (String methodName : methods) {
            try {
                Method method = sourceOrPlayer.getClass().getMethod(methodName);
                Object result = method.invoke(sourceOrPlayer);
                if (result != null) {
                    return result;
                }
            } catch (Exception ignored) {
            }
        }

        try {
            return sourceOrPlayer.getClass().getField("server").get(sourceOrPlayer);
        } catch (Exception ignored) {
        }

        return null;
    }

    private static Object resolveIdentity(ServerPlayer player) {
        String[] methods = {"nameAndId", "getNameAndId", "gameProfile", "getGameProfile"};
        for (String methodName : methods) {
            try {
                Method method = player.getClass().getMethod(methodName);
                Object result = method.invoke(player);
                if (result != null) {
                    return result;
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }
}
