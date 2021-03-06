package cpup.cbot.events.user

import cpup.cbot.CBot
import cpup.cbot.users.IRCUser
import org.pircbotx.PircBotX
import org.pircbotx.hooks.events
import cpup.cbot.events.Event

case class NickChangeEvent(bot: CBot, ircUser: IRCUser, oldNick: String, newNick: String) extends Event with IRCUserEvent {
	override def context = bot

	def this(bot: CBot, e: events.NickChangeEvent[PircBotX]) {
		this(
			bot,
			bot.users.fromNick(e.getOldNick),
			e.getOldNick,
			e.getNewNick
		)
	}
}