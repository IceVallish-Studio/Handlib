package me.mklv.handlib.fabric;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import java.lang.reflect.Method;

public class PermissionsAdapter {
    private static final Method PERMISSIONS_CHECK_COMMAND_SOURCE;
    private static final Method PERMISSIONS_CHECK_PLAYER;
    private static final boolean HAS_FABRIC_PERMISSIONS;

    static {
        Method checkCommandSourceMethod = null;
        Method checkPlayerMethod = null;
        boolean hasFabricPermissions = false;

        try {
            // Try to load the fabric-permissions-api
            Class<?> permissionsClass = Class.forName("me.lucko.fabric.api.permissions.v0.Permissions");

            // Try to get the CommandSourceStack version (takes permission string and int level)
            try {
                checkCommandSourceMethod = permissionsClass.getMethod("check", CommandSourceStack.class, String.class, int.class);
            } catch (NoSuchMethodException e1) {
                // Fallback: try without the int parameter (some versions don't require it)
                try {
                    checkCommandSourceMethod = permissionsClass.getMethod("check", CommandSourceStack.class, String.class);
                } catch (NoSuchMethodException e2) {
                    // Method not found
                }
            }

            // Try to get the ServerPlayer version (1.21.11+, takes permission string only)
            try {
                checkPlayerMethod = permissionsClass.getMethod("check", ServerPlayer.class, String.class);
            } catch (NoSuchMethodException e1) {
                // Try with int parameter as fallback
                try {
                    checkPlayerMethod = permissionsClass.getMethod("check", ServerPlayer.class, String.class, int.class);
                } catch (NoSuchMethodException e2) {
                    // Method not found
                }
            }

            hasFabricPermissions = checkCommandSourceMethod != null || checkPlayerMethod != null;
        } catch (ClassNotFoundException e) {
            // fabric-permissions-api not available, will use defaults
        }

        PERMISSIONS_CHECK_COMMAND_SOURCE = checkCommandSourceMethod;
        PERMISSIONS_CHECK_PLAYER = checkPlayerMethod;
        HAS_FABRIC_PERMISSIONS = hasFabricPermissions;
    }

    public static boolean checkPermission(CommandSourceStack source, String permission, int minimumLevel) {
        if (HAS_FABRIC_PERMISSIONS && PERMISSIONS_CHECK_COMMAND_SOURCE != null) {
            try {
                try {
                    return (boolean) PERMISSIONS_CHECK_COMMAND_SOURCE.invoke(null, source, permission, minimumLevel);
                } catch (IllegalArgumentException e) {
                    return (boolean) PERMISSIONS_CHECK_COMMAND_SOURCE.invoke(null, source, permission);
                }
            } catch (Exception e) {
                return fallbackHasPermission(source, minimumLevel);
            }
        }

        return fallbackHasPermission(source, minimumLevel);
    }

    public static boolean checkPermission(ServerPlayer player, String permission) {
        if ("handshaker.bypass".equals(permission)) {
            if (!HAS_FABRIC_PERMISSIONS || PERMISSIONS_CHECK_PLAYER == null) {
                return false;
            }

            try {
                return (boolean) PERMISSIONS_CHECK_PLAYER.invoke(null, player, permission);
            } catch (Exception e) {
                return false;
            }
        }

        if (HAS_FABRIC_PERMISSIONS && PERMISSIONS_CHECK_PLAYER != null) {
            try {
                return (boolean) PERMISSIONS_CHECK_PLAYER.invoke(null, player, permission);
            } catch (Exception e) {
                return false;
            }
        }

        return false;
    }

    public static boolean hasFabricPermissions() {
        return HAS_FABRIC_PERMISSIONS;
    }

    private static boolean fallbackHasPermission(CommandSourceStack source, int minimumLevel) {
        try {
            Method hasPermission = source.getClass().getMethod("hasPermission", int.class);
            Object result = hasPermission.invoke(source, minimumLevel);
            if (result instanceof Boolean allowed) {
                return allowed;
            }
        } catch (Exception ignored) {
        }

        try {
            Method getPermissionLevel = source.getClass().getMethod("getPermissionLevel");
            Object result = getPermissionLevel.invoke(source);
            if (result instanceof Number level) {
                return level.intValue() >= minimumLevel;
            }
        } catch (Exception ignored) {
        }

        try {
            Object entity = source.getEntity();
            if (entity instanceof ServerPlayer player) {
                return fallbackHasPermission(player, minimumLevel);
            }
        } catch (Exception ignored) {
        }

        return resolveServer(source) != null;
    }

    private static boolean fallbackHasPermission(ServerPlayer player, int minimumLevel) {
        try {
            Method hasPermission = player.getClass().getMethod("hasPermission", int.class);
            Object result = hasPermission.invoke(player, minimumLevel);
            if (result instanceof Boolean allowed) {
                return allowed;
            }
        } catch (Exception ignored) {
        }

        try {
            Method hasPermissions = player.getClass().getMethod("hasPermissions", int.class);
            Object result = hasPermissions.invoke(player, minimumLevel);
            if (result instanceof Boolean allowed) {
                return allowed;
            }
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
                Method getPermissionLevel = permissions.getClass().getMethod("getPermissionLevel");
                Object level = getPermissionLevel.invoke(permissions);
                if (level instanceof Number number) {
                    return number.intValue() >= minimumLevel;
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