package ac.grim.grimac.utils.anticheat;

import ac.grim.grimac.api.packet.protocol.version.server.ServerVersions;

public class Version {
    private static final boolean IS_FLAT = ServerVersions.getServerVersion().isNewerThanOrEquals(ServerVersions.V_1_13);

    public static boolean isFlat() {
        return IS_FLAT;
    }
}
