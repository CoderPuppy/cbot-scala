package cpup.cbot.events.user

import cpup.cbot.CBot
import cpup.cbot.users.User
import cpup.cbot.events.Event

case class ChangePasswordEvent(bot: CBot, user: User, oldPassword: String, newPassword: String) extends Event with UserEvent {
	override def context = bot
}