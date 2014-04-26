package cpup.cbot.events

import cpup.cbot.CBot
import cpup.cbot.users.User

case class RegisterEvent(bot: CBot, user: User) extends Event with UserEvent {
	def context = bot
}