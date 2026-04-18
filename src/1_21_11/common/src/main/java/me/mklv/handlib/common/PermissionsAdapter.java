package me.mklv.handlib.common;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.resources.Identifier;
import java.lang.reflect.Method;
import java.lang.reflect.Field;

public class PermissionsAdapter {
    private static final Method PERMISSIONS_CHECK;
    private static final boolean HAS_FABRIC_PERMISSIONS;
    private static final Permission BYPASS_PERMISSION =
            Permission.Atom.create(Identifier.fromNamespaceAndPath("handshaker", "bypass"));

    static {
        Method checkMethod = null;
        boolean hasFabricPermissions = false;

        try {
            // Try to load the fabric-permissions-api
            Class<?> permissionsClass = Class.forName("me.lucko.fabric.api.permissions.v0.Permissions");

            checkMethod = findCheckMethod(permissionsClass, SharedSuggestionProvider.class, String.class, int.class);
            if (checkMethod == null) {
                checkMethod = findCheckMethod(permissionsClass, CommandSourceStack.class, String.class, int.class);
            }
            hasFabricPermissions = checkMethod != null;
        } catch (ClassNotFoundException e) {
            // fabric-permissions-api not available, will use defaults
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
            return source.permissions().hasPermission(BYPASS_PERMISSION);
        }

        if (HAS_FABRIC_PERMISSIONS) {
            try {
                return (boolean) PERMISSIONS_CHECK.invoke(null, source, permission, minimumLevel);
            } catch (Exception e) {
                return fallbackHasPermission(source, minimumLevel);
            }
        }

        return fallbackHasPermission(source, minimumLevel);
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
            return player.permissions().hasPermission(BYPASS_PERMISSION);
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

        return fallbackHasPermission(player, 4);
    }

    public static boolean hasFabricPermissions() {
        return HAS_FABRIC_PERMISSIONS;
    }

    private static Method findCheckMethod(Class<?> permissionsClass, Class<?> subjectType, Class<?>... trailingTypes) {
        try {
            Class<?>[] parameterTypes = new Class<?>[trailingTypes.length + 1];
            parameterTypes[0] = subjectType;
            System.arraycopy(trailingTypes, 0, parameterTypes, 1, trailingTypes.length);
            return permissionsClass.getMethod("check", parameterTypes);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private static boolean fallbackHasPermission(CommandSourceStack source, int minimumLevel) {
        try {
            if (source.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.byId(minimumLevel)))) {
                return true;
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

        return false;
    }

    private static boolean fallbackHasPermission(ServerPlayer player, int minimumLevel) {
        try {
            if (player.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.byId(minimumLevel)))) {
                return true;
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
