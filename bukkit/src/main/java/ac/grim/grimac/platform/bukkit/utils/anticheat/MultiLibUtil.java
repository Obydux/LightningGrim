package ac.grim.grimac.platform.bukkit.utils.anticheat;

import ac.grim.grimac.api.packet.protocol.version.server.ServerVersions;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;

public class MultiLibUtil {

    public final static Method externalPlayerMethod = getMethod(Player.class, "isExternalPlayer");
    private static final boolean IS_PRE_1_18 = ServerVersions.getServerVersion().isOlderThan(ServerVersions.V_1_18);

    public static Method getMethod(Class<?> clazz, String methodName) {
        try {
            return clazz.getMethod(methodName);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    // TODO: cache external players for better performance, but this only matters for people using multi-lib
    public static boolean isExternalPlayer(Player player) {
        if (externalPlayerMethod == null || IS_PRE_1_18) return false;
        try {
            return (boolean) externalPlayerMethod.invoke(player);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


}
