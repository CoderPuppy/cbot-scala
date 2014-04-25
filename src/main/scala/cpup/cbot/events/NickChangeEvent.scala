package cpup.cbot.events

import cpup.cbot.CBot
import cpup.cbot.users.IRCUser
import org.pircbotx.PircBotX
import org.pircbotx.hooks.events

case class NickChangeEvent(bot: CBot, user: IRCUser, oldNick: String, newNick: String) extends Event with UserEvent {
	def context = bot

	def this(bot: CBot, e: events.NickChangeEvent[PircBotX]) {
		this(
			bot,
			bot.users.fromNick(e.getOldNick),
			e.getOldNick,
			e.getNewNick
		)
	}
}