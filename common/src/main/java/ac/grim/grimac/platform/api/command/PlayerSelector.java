package ac.grim.grimac.platform.api.command;

import ac.grim.grimac.platform.api.sender.Sender;


public interface PlayerSelector {
    Sender getSinglePlayer(); // Throws an exception if not a single selection

    String inputString();
}
