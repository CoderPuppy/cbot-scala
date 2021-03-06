package cpup.cbot.events.user

import cpup.cbot.CBot
import cpup.cbot.users.IRCUser
import org.pircbotx.hooks.events
import org.pircbotx.PircBotX
import cpup.cbot.events.{Replyable, MessageEvent, Event}

case class PrivateMessageEvent(bot: CBot, ircUser: IRCUser, msg: String) extends Event with IRCUserEvent with MessageEvent with Replyable {
	def this(bot: CBot, e: events.PrivateMessageEvent[PircBotX]) {
		this(
			bot,
			bot.users.fromNick(e.getUser.getNick),
			e.getMessage
		)
	}

	override def context = user

	override def reply(msg: String) = ircUser.send.msg(msg)
	override def genericReply(msg: String) = ircUser.send.msg(msg)
	override def privateReply(msg: String) = ircUser.send.msg(msg)
}