package cpup.cbot.events

import cpup.cbot.users.User

trait UserEvent extends Event {
	def user: User

	def checkPermission(permission: Symbol) = context.checkPermission(user, permission)
}