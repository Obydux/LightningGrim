package ac.grim.grimac.utils.team;

import ac.grim.grimac.api.packet.types.server.play.ServerTeamsPacket;


public final class EntityPredicates {

//    public static Predicate<GrimPlayer> canBePushedBy(GrimPlayer player, PacketEntity entity, TeamHandler teamHandler) {
//        if (player.gamemode == GameMode.SPECTATOR) return p -> false;
//        final EntityTeam entityTeam = teamHandler.getEntityTeam(entity).orElse(null);
//        WrapperPlayServerTeams.CollisionRule collisionRule = entityTeam == null ? WrapperPlayServerTeams.CollisionRule.ALWAYS : entityTeam.getCollisionRule();
//        if (collisionRule == WrapperPlayServerTeams.CollisionRule.NEVER) return p -> false;
//
//        return p -> {
//            final EntityTeam playersTeam = teamHandler.getPlayersTeam().orElse(null);
//            WrapperPlayServerTeams.CollisionRule collisionRule2 = playersTeam == null ? WrapperPlayServerTeams.CollisionRule.ALWAYS : playersTeam.getCollisionRule();
//            if (collisionRule2 == WrapperPlayServerTeams.CollisionRule.NEVER) {
//                return false;
//            } else {
//                boolean bl = entityTeam != null && entityTeam.equals(playersTeam);
//                if ((collisionRule == WrapperPlayServerTeams.CollisionRule.PUSH_OWN_TEAM || collisionRule2 == WrapperPlayServerTeams.CollisionRule.PUSH_OWN_TEAM) && bl) {
//                    return false;
//                } else {
//                    return collisionRule != WrapperPlayServerTeams.CollisionRule.PUSH_OTHER_TEAMS && collisionRule2 != WrapperPlayServerTeams.CollisionRule.PUSH_OTHER_TEAMS || bl;
//                }
//            }
//        };
//    }

    public static boolean canBePushedBy(EntityTeam entityTeam, EntityTeam playersTeam) {
        ServerTeamsPacket.CollisionRule collisionRule = entityTeam == null ? ServerTeamsPacket.CollisionRule.ALWAYS : entityTeam.getCollisionRule();
        if (collisionRule == ServerTeamsPacket.CollisionRule.NEVER) return false;
        ServerTeamsPacket.CollisionRule collisionRule2 = playersTeam == null ? ServerTeamsPacket.CollisionRule.ALWAYS : playersTeam.getCollisionRule();
        if (collisionRule2 == ServerTeamsPacket.CollisionRule.NEVER) {
            return false;
        } else {
            final boolean bl = entityTeam != null && entityTeam.equals(playersTeam);
            if ((collisionRule == ServerTeamsPacket.CollisionRule.PUSH_OWN_TEAM || collisionRule2 == ServerTeamsPacket.CollisionRule.PUSH_OWN_TEAM) && bl) {
                return false;
            } else {
                return collisionRule != ServerTeamsPacket.CollisionRule.PUSH_OTHER_TEAMS && collisionRule2 != ServerTeamsPacket.CollisionRule.PUSH_OTHER_TEAMS || bl;
            }
        }
    }
}
