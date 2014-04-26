package cpup.cbot.events.user

import cpup.cbot.{Context, CBot}
import cpup.cbot.events.Event
import cpup.cbot.users.User

case class TakeEvent(bot: CBot, override val context: Context, user: User, permission: Symbol) extends UserUpdateEvent