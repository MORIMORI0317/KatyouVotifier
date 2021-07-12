package red.felnull.katyouvotifier;

import com.vexsoftware.votifier.platform.LoggingAdapter;
import org.apache.logging.log4j.Logger;

public class CustomLoggingAdapter implements LoggingAdapter {
    private final Logger LOGGER;

    public CustomLoggingAdapter(Logger logger) {
        this.LOGGER = logger;
    }

    @Override
    public void error(String s) {
        LOGGER.error(s);
    }

    @Override
    public void error(String s, Object... o) {
        LOGGER.error(s, o);
    }

    @Override
    public void error(String s, Throwable e, Object... o) {
        LOGGER.error(s, e, o);
    }

    @Override
    public void warn(String s) {
        LOGGER.warn(s);
    }

    @Override
    public void warn(String s, Object... o) {
        LOGGER.warn(s, o);
    }

    @Override
    public void info(String s) {
        LOGGER.info(s);
    }

    @Override
    public void info(String s, Object... o) {
        LOGGER.info(s, o);
    }
}
