package com.hazeluff.discord.nhlbot.bot.command;

import java.util.List;

import com.hazeluff.discord.nhlbot.bot.GameDayChannel;
import com.hazeluff.discord.nhlbot.bot.NHLBot;
import com.hazeluff.discord.nhlbot.nhl.Game;
import com.hazeluff.discord.nhlbot.nhl.GameStatus;
import com.hazeluff.discord.nhlbot.nhl.Team;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;

/**
 * Displays the score of a game in a Game Day Channel.
 */
public class ScoreCommand extends Command {

	public ScoreCommand(NHLBot nhlBot) {
		super(nhlBot);
	}

	@Override
	public void replyTo(IMessage message, List<String> arguments) {
		IChannel channel = message.getChannel();
		IGuild guild = message.getGuild();
		List<Team> preferredTeam = nhlBot.getPreferencesManager().getTeams(guild.getLongID());
		if (preferredTeam.isEmpty()) {
			sendSubscribeFirstMessage(channel);
		} else {
			Game game = nhlBot.getGameScheduler().getGameByChannelName(channel.getName());
			if (game == null) {
				nhlBot.getDiscordManager().sendMessage(channel, getRunInGameDayChannelsMessage(guild, preferredTeam));
			} else if (game.getStatus() == GameStatus.PREVIEW) {
				nhlBot.getDiscordManager().sendMessage(channel, GAME_NOT_STARTED_MESSAGE);
			} else {
				nhlBot.getDiscordManager().sendMessage(channel, GameDayChannel.getScoreMessage(game));
			}
		}
	}

	@Override
	public boolean isAccept(IMessage message, List<String> arguments) {
		return arguments.get(0).equalsIgnoreCase("score");
	}

}
