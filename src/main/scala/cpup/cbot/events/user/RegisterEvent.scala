package cpup.cbot.events.user

import cpup.cbot.CBot
import cpup.cbot.users.User
import cpup.cbot.events.Event

case class RegisterEvent(bot: CBot, user: User) extends Event with UserEvent {
	override def context = bot
}