package com.hazeluff.discord.nhlbot.bot.command;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.hazeluff.discord.nhlbot.bot.NHLBot;
import com.hazeluff.discord.nhlbot.nhl.Team;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;

/**
 * Subscribes guilds to a team.
 */
public class SubscribeCommand extends Command {

	static final String MUST_HAVE_PERMISSIONS_MESSAGE = "You must have _Admin_ or _Manage Channels_ roles"
			+ "to subscribe the guild to a team.";
	static final String SPECIFY_TEAM_MESSAGE = "You must specify a parameter for what team you want to subscribe to. "
			+ "`@NHLBot subscribe [team]`";

	public SubscribeCommand(NHLBot nhlBot) {
		super(nhlBot);
	}

	@Override
	public void replyTo(IMessage message, List<String> arguments) {
		IChannel channel = message.getChannel();
		if (hasSubscribePermissions(message)) {
			if (arguments.size() < 2) {
				nhlBot.getDiscordManager().sendMessage(channel, SPECIFY_TEAM_MESSAGE);
			} else if (arguments.get(1).equalsIgnoreCase("help")) {
				StringBuilder response = new StringBuilder(
						"Subscribed to any of the following teams by typing `@NHLBot subscribe [team]`, "
								+ "where [team] is the one of the three letter codes for your team below: ")
										.append("```");
				List<Team> teams = Team.getSortedLValues();
				for (Team team : teams) {
					response.append("\n").append(team.getCode()).append(" - ").append(team.getFullName());
				}
				response.append("```\n");
				response.append("You can unsbscribe using:\n");
				response.append("`@NHLBot unsubscribe`");
				nhlBot.getDiscordManager().sendMessage(channel, response.toString());
			} else if (Team.isValid(arguments.get(1))) {
				Team team = Team.parse(arguments.get(1));
				IGuild guild = message.getGuild();
				// Subscribe guild
				nhlBot.getGameDayChannelsManager().deleteInactiveGuildChannels(guild);
				nhlBot.getPreferencesManager().subscribeGuild(guild.getLongID(), team);
				nhlBot.getGameDayChannelsManager().initChannels(guild);
				List<Team> subscribedTeams = nhlBot.getPreferencesManager().getTeams(guild.getLongID());
				if (subscribedTeams.size() > 1) {
					String teamsStr = StringUtils.join(
							subscribedTeams.stream().map(subbedTeam -> subbedTeam.getFullName()).sorted()
									.collect(Collectors.toList()),
							"\n");
					nhlBot.getDiscordManager().sendMessage(channel,
							"This server is now subscribed to:\n```" + teamsStr + "```");

				} else {
					nhlBot.getDiscordManager().sendMessage(channel,
							"This server is now subscribed to games of the **" + team.getFullName() + "**!");
				}
			} else {
				nhlBot.getDiscordManager().sendMessage(channel, "[" + arguments.get(1) + "] is not a valid team code. "
						+ "Use `@NHLBot subscribe help` to get a full list of team");
			}
		} else {
			nhlBot.getDiscordManager().sendMessage(channel, MUST_HAVE_PERMISSIONS_MESSAGE);
		}
	}

	@Override
	public boolean isAccept(IMessage message, List<String> arguments) {
		return arguments.get(0).equalsIgnoreCase("subscribe");
	}

}
