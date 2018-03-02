package com.hazeluff.discord.nhlbot.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.nhlbot.Config;
import com.hazeluff.discord.nhlbot.bot.command.AboutCommand;
import com.hazeluff.discord.nhlbot.bot.discord.DiscordManager;
import com.hazeluff.discord.nhlbot.bot.preferences.PreferencesManager;
import com.hazeluff.discord.nhlbot.nhl.GameScheduler;
import com.hazeluff.discord.nhlbot.utils.Utils;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.util.DiscordException;


public class NHLBot {
	private static final Logger LOGGER = LoggerFactory.getLogger(NHLBot.class);

	private final IDiscordClient discordClient;
	private final MongoDatabase mongoDatabase;
	private final PreferencesManager preferencesManager;
	private final DiscordManager discordManager;
	private final GameScheduler gameScheduler;
	private final GameDayChannelsManager gameDayChannelsManager;
	private final String id;

	public NHLBot(String botToken, GameScheduler gameScheduler) {
		LOGGER.info("Running NHLBot v" + Config.VERSION);
		Thread.currentThread().setName("NHLBot");

		this.gameScheduler = gameScheduler;

		// Init DiscordClient
		discordClient = getClient(botToken);

		try {
			id = discordClient.getApplicationClientID();
			LOGGER.info("NHLBot. id [" + id + "]");
		} catch (DiscordException e) {
			LOGGER.error("Failed to get Application Client ID", e);
			throw new NHLBotException(e);
		}

		// Init MongoClient/GuildPreferences
		mongoDatabase = getMongoDatabaseInstance();
		preferencesManager = PreferencesManager.getInstance(discordClient, mongoDatabase);

		// Init other classes
		discordManager = new DiscordManager(discordClient);

		// Start the Game Day Channels Manager
		gameDayChannelsManager = new GameDayChannelsManager(this);
		gameDayChannelsManager.start();


		// Register listeners
		EventDispatcher dispatcher = discordClient.getDispatcher();
		dispatcher.registerListener(new CommandListener(this));
		
		LOGGER.info("Posting update to Discord channel.");
		discordClient.getGuilds().stream()
			.filter(guild -> {
				long id = guild.getLongID();
				return id == 268247727400419329l || id == 276953120964083713l;
			})
			.forEach(guild -> guild.getChannelsByName("welcome")
						.forEach(channel -> {
							channel.getFullMessageHistory().stream()
									.filter(message -> discordManager.isAuthorOfMessage(message))
									.forEach(message -> discordManager.deleteMessage(message));
							discordManager.sendMessage(channel, "I was just deployed.");
							new AboutCommand(this).sendFile(channel);
						}
					)
			);
	}

	static IDiscordClient getClient(String token) {
		ClientBuilder clientBuilder = new ClientBuilder();
		clientBuilder.withToken(token);
		IDiscordClient client;
		try {
			client = clientBuilder.login();
		} catch (DiscordException e) {
			LOGGER.error("Could not log in.", e);
			throw new NHLBotException(e);
		}
		while (!client.isReady()) {
			LOGGER.info("Waiting for client to be ready.");
			Utils.sleep(5000);
		}
		LOGGER.info("Client is ready.");
		client.changePlayingText(Config.STATUS_MESSAGE);

		return client;
	}

	@SuppressWarnings("resource")
	static MongoDatabase getMongoDatabaseInstance() {
		// No need to close the connection.
		return new MongoClient(Config.MONGO_HOST, Config.MONGO_PORT).getDatabase(Config.MONGO_DATABASE_NAME);
	}

	public IDiscordClient getDiscordClient() {
		return discordClient;
	}

	public MongoDatabase getMongoDatabase() {
		return mongoDatabase;
	}

	public PreferencesManager getPreferencesManager() {
		return preferencesManager;
	}

	public DiscordManager getDiscordManager() {
		return discordManager;
	}

	public GameScheduler getGameScheduler() {
		return gameScheduler;
	}

	public GameDayChannelsManager getGameDayChannelsManager() {
		return gameDayChannelsManager;
	}

	public String getId() {
		return id;
	}

	/**
	 * Gets the id of the bot, in the format displayed in a message.
	 * 
	 * @return
	 */
	public String getMentionId() {
		return "<@" + id + ">";
	}

	/**
	 * Gets the id of the bot, in the format displayed in a message, when the bot is mentioned by Nickname.
	 * 
	 * @return
	 */
	public String getNicknameMentionId() {
		return "<@!" + id + ">";
	}
}
