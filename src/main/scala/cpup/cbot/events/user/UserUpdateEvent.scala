package cpup.cbot.events.user

import cpup.cbot.events.Event
import cpup.cbot.Context

trait UserUpdateEvent extends Event with UserEvent {
	override def context: Context = bot
}