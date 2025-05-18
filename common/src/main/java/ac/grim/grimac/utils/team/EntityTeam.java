package ac.grim.grimac.utils.team;

import ac.grim.grimac.api.packet.types.server.play.ServerTeamsPacket;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.api.packet.player.PacketUserProfile;
import lombok.Getter;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public final class EntityTeam {

    public final String name;
    public final Set<String> entries = new HashSet<>();
    private final GrimPlayer player;
    @Getter
    private ServerTeamsPacket.CollisionRule collisionRule;

    public EntityTeam(GrimPlayer player, String name) {
        this.player = player;
        this.name = name;
    }

    public void update(ServerTeamsPacket teams) {
        teams.getTeamInfo().ifPresent(info -> this.collisionRule = info.getCollisionRule());

        final TeamHandler teamHandler = player.checkManager.getPacketCheck(TeamHandler.class);
        final ServerTeamsPacket.TeamMode mode = teams.getTeamMode();
        if (mode == ServerTeamsPacket.TeamMode.ADD_ENTITIES || mode == ServerTeamsPacket.TeamMode.CREATE) {
            label:
            for (String teamPlayer : teams.getPlayers()) {
                if (teamPlayer.equals(player.user.getName())) {
                    teamHandler.setPlayerTeam(this);
                    continue;
                }

                for (PacketUserProfile profile : player.compensatedEntities.profiles.values()) {
                    if (profile.getName() != null && profile.getName().equals(teamPlayer)) {
                        teamHandler.addEntityToTeam(profile.getUUID().toString(), this);
                        continue label;
                    }
                }

                teamHandler.addEntityToTeam(teamPlayer, this);
            }
        } else if (mode == ServerTeamsPacket.TeamMode.REMOVE_ENTITIES) {
            label:
            for (String teamPlayer : teams.getPlayers()) {
                if (teamPlayer.equals(player.user.getName())) {
                    // Player was removed from their team.
                    teamHandler.setPlayerTeam(null);
                    continue;
                }

                for (PacketUserProfile profile : player.compensatedEntities.profiles.values()) {
                    if (profile.getName() != null && profile.getName().equals(teamPlayer)) {
                        String uuid = profile.getUUID().toString();
                        entries.remove(uuid);
                        teamHandler.removeEntityFromTeam(uuid);
                        continue label;
                    }
                }

                // Entity was removed from their team.
                teamHandler.removeEntityFromTeam(teamPlayer);
                entries.remove(teamPlayer);
            }
        } else if (mode == ServerTeamsPacket.TeamMode.REMOVE) {

            EntityTeam playersTeam = teamHandler.getPlayerTeam();
            // The player's team was deleted, so we must unset the player's team
            if (playersTeam != null && playersTeam.name.equals(name)) {
                teamHandler.setPlayerTeam(null);
            }

            // Also remove the team set on entities
            for (String entry : entries) {
                teamHandler.removeEntityFromTeam(entry);
            }
            entries.clear();
        }
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof EntityTeam t && Objects.equals(name, t.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
