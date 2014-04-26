package cpup.cbot.events.user

import cpup.cbot.CBot
import cpup.cbot.users.{User, IRCUser}

case class RegisterNickServEvent(bot: CBot, user: User, nickserv: String) extends UserUpdateEvent {

}