package red.felnull.katyouvotifier.event;

import com.vexsoftware.votifier.model.Vote;
import net.minecraftforge.eventbus.api.Event;

public class VotifierEvent extends Event {
    private Vote vote;

    public VotifierEvent(Vote vote) {
        this.vote = vote;
    }

    public Vote getVote() {
        return vote;
    }
}
