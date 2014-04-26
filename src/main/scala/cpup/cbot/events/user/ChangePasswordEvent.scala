package cpup.cbot.events.user

import cpup.cbot.CBot
import cpup.cbot.users.User

case class ChangePasswordEvent(bot: CBot, user: User, oldPassword: String, newPassword: String) extends UserUpdateEvent {
	override def context = bot
}