package cpup.cbot.events

import cpup.cbot.CBot
import cpup.cbot.users.IRCUser
import org.pircbotx.hooks.events
import org.pircbotx.PircBotX

case class PrivateMessageEvent(bot: CBot, user: IRCUser, msg: String) extends Event with UserEvent with MessageEvent with Replyable {
	def this(bot: CBot, e: events.PrivateMessageEvent[PircBotX]) {
		this(
			bot,
			bot.users.fromNick(e.getUser.getNick),
			e.getMessage
		)
	}

	override def context = user.user

	override def reply(msg: String) = user.send.msg(msg)
	override def genericReply(msg: String) = user.send.msg(msg)
	override def privateReply(msg: String) = user.send.msg(msg)
}