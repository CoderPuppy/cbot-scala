package cpup.cbot.events.channel

import cpup.cbot.CBot
import cpup.cbot.users.IRCUser
import cpup.cbot.channels.Channel
import org.pircbotx.hooks.events
import org.pircbotx.PircBotX
import cpup.cbot.events.{Replyable, MessageEvent, UserEvent, Event}

case class ChannelMessageEvent(bot: CBot, channel: Channel, user: IRCUser, msg: String) extends Event with ChannelEvent with UserEvent with MessageEvent with Replyable {
	def this(bot: CBot, e: events.MessageEvent[PircBotX]) {
		this(bot, bot.channels(e.getChannel.getName), bot.users.fromNick(e.getUser.getNick), e.getMessage)
	}

	override def reply(msg: String) {
		channel.send.msg(s"${user.nick}: $msg")
	}

	override def genericReply(msg: String) {
		channel.send.msg(msg)
	}

	override def privateReply(msg: String) {
		user.send.msg(msg)
	}
}