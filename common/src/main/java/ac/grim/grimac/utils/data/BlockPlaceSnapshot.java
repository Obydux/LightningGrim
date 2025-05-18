package ac.grim.grimac.utils.data;

import ac.grim.grimac.api.packet.types.RecievablePacket;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BlockPlaceSnapshot {
    RecievablePacket wrapper;
    boolean sneaking;
}
