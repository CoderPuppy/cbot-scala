package cpup.cbot.events.user

import cpup.cbot.users.User
import cpup.cbot.Context
import cpup.cbot.events.Event

trait UserEvent extends Event {
	def user: User
	override def context: Context = user

	def checkPermission(permission: Symbol) = context.checkPermission(user, permission)
}