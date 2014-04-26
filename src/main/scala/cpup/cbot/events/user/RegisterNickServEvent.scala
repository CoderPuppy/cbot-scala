package cpup.cbot.events.user

import cpup.cbot.CBot
import cpup.cbot.users.User

case class RegisterNickServEvent(bot: CBot, user: User, nickserv: String) extends UpdateUserEvent {

}