package com.hazeluff.discord.nhlbot.bot.chat;

import java.util.Arrays;
import java.util.regex.Pattern;

import com.hazeluff.discord.nhlbot.bot.NHLBot;
import com.hazeluff.discord.nhlbot.utils.Utils;

import sx.blah.discord.handle.obj.IMessage;

public class WhatsUpTopic extends Topic {

	public WhatsUpTopic(NHLBot nhlBot) {
		super(nhlBot);
	}

	@Override
	public void replyTo(IMessage message) {
		String reply = Utils.getRandom(Arrays.asList(
				"Nothing Much. You?",
				"Bot stuff. You?",
				"Chillin. Want to join?",
				"Listening to some music.\nhttps://www.youtube.com/watch?v=cU8HrO7XuiE",
				"nm, u?"));
		nhlBot.getDiscordManager().sendMessage(message.getChannel(), reply);
	}

	@Override
	public boolean isReplyTo(IMessage message) {
		return isStringMatch(
				Pattern.compile("\\b((what(')?s\\s*up)|whaddup|wassup|sup)\\b"),
				message.getContent().toLowerCase());
	}

}
