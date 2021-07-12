package red.felnull.katyouvotifier;

import com.vexsoftware.votifier.platform.scheduler.ScheduledVotifierTask;
import com.vexsoftware.votifier.platform.scheduler.VotifierScheduler;
import red.felnull.katyouvotifier.handler.ServerHandler;
import red.felnull.katyouvotifier.util.ServerUtils;

import java.util.concurrent.TimeUnit;

public class ForgeScheduler implements VotifierScheduler {
    @Override
    public ScheduledVotifierTask sync(Runnable runnable) {
        return new ForgeTaskWrapper(addExecute(runnable));
    }

    @Override
    public ScheduledVotifierTask onPool(Runnable runnable) {
        return new ForgeTaskWrapper(addSubmit(runnable));
    }

    @Override
    public ScheduledVotifierTask delayedSync(Runnable runnable, int delay, TimeUnit unit) {
        return new ForgeTaskWrapper(addExecute(runnable, delay, unit));
    }

    @Override
    public ScheduledVotifierTask delayedOnPool(Runnable runnable, int delay, TimeUnit unit) {
        return new ForgeTaskWrapper(addSubmit(runnable, delay, unit));
    }

    @Override
    public ScheduledVotifierTask repeatOnPool(Runnable runnable, int delay, int repeat, TimeUnit unit) {
        return null;
    }

    private static class ForgeTaskWrapper implements ScheduledVotifierTask {
        private final ForgeTask task;

        private ForgeTaskWrapper(ForgeTask task) {
            this.task = task;
        }

        @Override
        public void cancel() {
            task.cancel();
        }
    }

    private ForgeTask addExecute(Runnable runnable) {
        ForgeTask forgeTask = new ForgeTask(runnable);
        ServerUtils.getMinecraftServer().execute(forgeTask::run);
        return forgeTask;
    }

    private ForgeTask addSubmit(Runnable runnable) {
        ForgeTask forgeTask = new ForgeTask(runnable);
        ServerUtils.getMinecraftServer().submit(forgeTask::run);
        return forgeTask;
    }

    private ForgeTask addExecute(Runnable runnable, int time, TimeUnit unit) {
        ForgeTask forgeTask = new ForgeTask(runnable);
        ServerHandler.addServerDelayTask(() -> ServerUtils.getMinecraftServer().execute(forgeTask::run), toTicks(time, unit));
        return forgeTask;
    }

    private ForgeTask addSubmit(Runnable runnable, int time, TimeUnit unit) {
        ForgeTask forgeTask = new ForgeTask(runnable);
        ServerHandler.addServerDelayTask(() -> ServerUtils.getMinecraftServer().submit(forgeTask::run), toTicks(time, unit));
        return forgeTask;
    }

    private ForgeTask addSubmit(Runnable runnable, int time, TimeUnit unit, int repeat) {
        ForgeTask forgeTask = new ForgeTask(runnable);
        ServerHandler.addServerRepeatTask(() -> ServerUtils.getMinecraftServer().submit(forgeTask::run), toTicks(time, unit), repeat, unit);
        return forgeTask;
    }

    private int toTicks(int time, TimeUnit unit) {
        return (int) (unit.toMillis(time) / 50);
    }
}
