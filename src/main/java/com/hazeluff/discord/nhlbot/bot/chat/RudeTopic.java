package com.hazeluff.discord.nhlbot.bot.chat;

import java.util.Arrays;
import java.util.regex.Pattern;

import com.hazeluff.discord.nhlbot.Config;
import com.hazeluff.discord.nhlbot.bot.NHLBot;
import com.hazeluff.discord.nhlbot.utils.Utils;

import sx.blah.discord.handle.obj.IMessage;

public class RudeTopic extends Topic {

	public RudeTopic(NHLBot nhlBot) {
		super(nhlBot);
	}

	@Override
	public void replyTo(IMessage message) {
		String reply = Utils.getRandom(Arrays.asList(
				"Nah, you should fuck off.", "Go kill yourself.", "You can suck my dick.",
				"Go take it, and shove it up your butt.", "Please, eat shit and die.", "Get fucked.",
				"You are cordially invited to get fucked.", "Bleep Bloop. I am just a robot.", "Ok. Twat.",
				"Why you gotta be so ruuuddee :musical_note:\nhttps://goo.gl/aMwOxY",
				"You're probably getting coal this Christmas.", "I'm just doing my job. :cry:", "That's not nice.",
				String.format("%s worked really hard on me.", Config.HAZELUFF_MENTION)));
		nhlBot.getDiscordManager().sendMessage(message.getChannel(), reply);
	}

	@Override
	public boolean isReplyTo(IMessage message) {
		return isStringMatch(
				Pattern.compile("\\b((fuck\\s*off)|(shut\\s*(up|it))|(fuck\\s*(you|u)))\\b"),
				message.getContent().toLowerCase());
	}

}
