package cpup.cbot.events.user

import cpup.cbot.events.Event
import cpup.cbot.CBot
import cpup.cbot.users.User

case class UnregisterEvent(bot: CBot, user: User) extends Event with UserEvent {
	override def context = bot
}