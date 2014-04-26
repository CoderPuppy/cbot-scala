package cpup.cbot.events.user

import cpup.cbot.CBot
import cpup.cbot.users.User
import cpup.cbot.events.Event

case class UnregisterNickServEvent(bot: CBot, user: User, nickserv: String) extends Event with UserEvent {
	override def context = bot
}