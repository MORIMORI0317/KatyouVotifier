package red.felnull.katyouvotifier;

public class ForgeTask {
    private final Runnable runnable;
    public boolean cancel;

    public ForgeTask(Runnable runnable) {
        this.runnable = runnable;
    }

    public void cancel() {
        cancel = true;
    }

    public void run() {
        if (!cancel) {
            runnable.run();
        }
    }
}
