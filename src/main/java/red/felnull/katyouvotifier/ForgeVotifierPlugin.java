package red.felnull.katyouvotifier;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.net.VotifierServerBootstrap;
import com.vexsoftware.votifier.net.VotifierSession;
import com.vexsoftware.votifier.net.protocol.v1crypto.RSAIO;
import com.vexsoftware.votifier.net.protocol.v1crypto.RSAKeygen;
import com.vexsoftware.votifier.platform.LoggingAdapter;
import com.vexsoftware.votifier.platform.VotifierPlugin;
import com.vexsoftware.votifier.platform.scheduler.VotifierScheduler;
import com.vexsoftware.votifier.util.KeyCreator;
import com.vexsoftware.votifier.util.TokenUtil;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.Logger;
import red.felnull.katyouvotifier.event.VotifierEvent;
import red.felnull.katyouvotifier.util.ServerUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.Key;
import java.security.KeyPair;
import java.util.HashMap;
import java.util.Map;

public class ForgeVotifierPlugin implements VotifierPlugin {
    private static final Gson GSON = new Gson();
    private final CustomLoggingAdapter loggingAdapter;
    private final Map<String, Key> tokens = new HashMap<>();
    private VotifierScheduler scheduler;
    private KeyPair keyPair;
    private VotifierServerBootstrap bootstrap;

    public ForgeVotifierPlugin(Logger LOGGER) {
        loggingAdapter = new CustomLoggingAdapter(LOGGER);
    }

    public boolean loadAndBind() {
        scheduler = new ForgeScheduler();
        File fol = FMLPaths.CONFIGDIR.get().resolve("Votifier").toFile();
        File config = fol.toPath().resolve("config.json").toFile();

        if (!fol.exists())
            fol.mkdirs();

        String host;
        int port;
        boolean disablev1;

        if (!config.exists()) {
            String hostAddr = ServerUtils.getMinecraftServer() != null ? ServerUtils.getMinecraftServer().getLocalIp() : null;
            if (hostAddr == null || hostAddr.isEmpty())
                hostAddr = "0.0.0.0";
            String defaltToken = "";
            try {
                defaltToken = TokenUtil.newToken();
            } catch (Exception ex) {
                loggingAdapter.error("Error generating Votifier token", ex);
                return false;
            }
            JsonObject jo = new JsonObject();
            jo.addProperty("host", hostAddr);
            jo.addProperty("port", 8192);
            jo.addProperty("disable-v1-protocol", false);
            JsonObject tokenJo = new JsonObject();
            tokenJo.addProperty("default", defaltToken);
            jo.add("tokens", tokenJo);
            try {
                Files.write(config.toPath(), jsonBuilder(jo).getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                e.printStackTrace();
            }
            tokens.put("default", KeyCreator.createKeyFrom(defaltToken));
            loggingAdapter.info("------------------------------------------------------------------------------");
            loggingAdapter.info("No tokens were found in your configuration, so we've generated one for you.");
            loggingAdapter.info("Your default Votifier token is " + defaltToken + ".");
            loggingAdapter.info("You will need to provide this token when you submit your server to a voting");
            loggingAdapter.info("list.");
            loggingAdapter.info("------------------------------------------------------------------------------");
            host = hostAddr;
            port = 8192;
            disablev1 = false;
        } else {
            try {
                JsonObject jo = GSON.fromJson(new FileReader(config), JsonObject.class);
                JsonObject tokenJo = jo.getAsJsonObject("tokens");
                for (Map.Entry<String, JsonElement> entry : tokenJo.entrySet()) {
                    tokens.put(entry.getKey(), KeyCreator.createKeyFrom(entry.getValue().getAsString()));
                    loggingAdapter.info("Loaded token for website: " + entry.getKey());
                }
                host = jo.get("host").getAsString();
                port = jo.get("port").getAsInt();
                disablev1 = jo.get("disable-v1-protocol").getAsBoolean();
            } catch (Exception ex) {
                loggingAdapter.error("Error creating configuration file", ex);
                return false;
            }
        }

        File rsa = fol.toPath().resolve("rsa").toFile();

        try {
            if (!rsa.exists()) {
                rsa.mkdirs();
                keyPair = RSAKeygen.generate(2048);
                RSAIO.save(rsa, keyPair);
            } else {
                keyPair = RSAIO.load(rsa);
            }
        } catch (Exception ex) {
            loggingAdapter.error("Error reading configuration file or RSA tokens", ex);
            return false;
        }
        if (port >= 0) {
            if (disablev1) {
                loggingAdapter.info("------------------------------------------------------------------------------");
                loggingAdapter.info("Votifier protocol v1 parsing has been disabled. Most voting websites do not");
                loggingAdapter.info("currently support the modern Votifier protocol in NuVotifier.");
                loggingAdapter.info("------------------------------------------------------------------------------");
            }
            bootstrap = new VotifierServerBootstrap(host, port, this, disablev1);
            bootstrap.start(error -> {
            });

        } else {
            loggingAdapter.info("------------------------------------------------------------------------------");
            loggingAdapter.info("Your Votifier port is less than 0, so we assume you do NOT want to start the");
            loggingAdapter.info("votifier port server! Votifier will not listen for votes over any port, and");
            loggingAdapter.info("will only listen for pluginMessaging forwarded votes!");
            loggingAdapter.info("------------------------------------------------------------------------------");
        }
        return true;
    }

    @Override
    public void onVoteReceived(Vote vote, VotifierSession.ProtocolVersion protocolVersion, String remoteAddress) {
        //  loggingAdapter.info("Got a " + protocolVersion.humanReadable + " vote record from " + remoteAddress + " -> " + vote);
        MinecraftForge.EVENT_BUS.post(new VotifierEvent(vote));
    }

    @Override
    public Map<String, Key> getTokens() {
        return tokens;
    }

    @Override
    public KeyPair getProtocolV1Key() {
        return keyPair;
    }

    @Override
    public LoggingAdapter getPluginLogger() {
        return loggingAdapter;
    }

    @Override
    public VotifierScheduler getScheduler() {
        return scheduler;
    }

    public static String jsonBuilder(JsonObject inJson) {
        return new GsonBuilder().serializeNulls().setPrettyPrinting().create().toJson(inJson);
    }

    public void halt() {
        if (bootstrap != null) {
            bootstrap.shutdown();
            bootstrap = null;
        }
    }

    public boolean reload() {
        try {
            halt();
        } catch (Exception ex) {
            loggingAdapter.warn("On halt, an exception was thrown. This may be fine!", ex);
        }

        if (loadAndBind()) {
            loggingAdapter.info("Reload was successful.");
            return true;
        } else {
            try {
                halt();
                loggingAdapter.error("On reload, there was a problem with the configuration. Votifier currently does nothing!");
            } catch (Exception ex) {
                loggingAdapter.error("On reload, there was a problem loading, and we could not re-halt the server. Votifier is in an unstable state!", ex);
            }
            return false;
        }
    }
}
