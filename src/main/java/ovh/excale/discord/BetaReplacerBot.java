package ovh.excale.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ovh.excale.discord.listeners.MemberJoinListener;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;

public class BetaReplacerBot {

	// [Invite link]
	// https://discord.com/api/oauth2/authorize?client_id=827329778213584916&permissions=134217728&scope=bot

	public static final Logger logger;
	public static final String VERSION;
	public static final String OWNER;
	public static final String[] CO_OWNERS;

	private static final Class<BetaReplacerBot> selfClass = BetaReplacerBot.class;
	private static final transient String TOKEN;
	private static JDA jda;

	public static JDA jda() {
		return jda;
	}

	static {

		// GET LOGGER
		logger = LoggerFactory.getLogger(selfClass);

		// VERSION META
		String version;
		InputStream in = selfClass.getClassLoader()
				.getResourceAsStream("VERSION");
		if(in != null)
			try(Scanner scanner = new Scanner(in)) {
				version = scanner.nextLine();
			} catch(Exception e) {
				logger.warn("Coudln't retrieve VERSION meta", e);
				version = "unknown";
			}
		else
			version = "unknown";

		VERSION = version;

		// MANDATORY ARGUMENTS
		for(String arg : new String[] { "DS_TOKEN", "DS_OWNER" })
			try {
				Objects.requireNonNull(System.getenv(arg));
			} catch(NullPointerException e) {
				logger.error("Missing argument {}, shutting down.", arg);
				System.exit(-1);
			}

		// GET ENVs
		TOKEN = System.getenv("DS_TOKEN");
		OWNER = System.getenv("DS_OWNER");
		CO_OWNERS = Arrays.stream(Optional.ofNullable(System.getenv("DS_COOWNERS"))
				.orElse("")
				.trim()
				.split(" *, *"))
				.filter(s -> s.length() != 0)
				.toArray(String[]::new);
	}

	public static void main(String[] args) {

		try {

			jda = JDABuilder.create(TOKEN, GatewayIntent.GUILD_MEMBERS)
					.disableCache(CacheFlag.ACTIVITY,
							CacheFlag.VOICE_STATE,
							CacheFlag.EMOTE,
							CacheFlag.CLIENT_STATUS)
					.setActivity(Activity.listening("members | beta:help"))
					.addEventListeners(new MemberJoinListener())
					.build()
					.awaitReady();

		} catch(Exception e) {
			logger.error(e.getMessage(), e);
			System.exit(-1);
		}

		logger.info("Bot running on version {}", VERSION);

	}

}
