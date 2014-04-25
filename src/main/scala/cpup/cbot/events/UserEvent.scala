package cpup.cbot.events

import cpup.cbot.users.IRCUser

trait UserEvent extends Event {
	def user: IRCUser

	def checkPermission(permission: Symbol) = context.checkPermission(user.user, permission)
}