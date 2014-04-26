package cpup.cbot

import cpup.cbot.plugin.PluginManager
import cpup.cbot.users.User
import cpup.cbot.events.user.{TakeEvent, GrantEvent}

trait Context extends PluginManager {
	def bot: CBot

	def getPermissions(user: User): Set[Symbol]

	def checkPermission(user: User, permission: Symbol) = getPermissions(user).contains(permission)
	def grantPermission(user: User, permission: Symbol) = {
		bot.bus.post(new GrantEvent(bot, this, user, permission))
		_grantPermission(user, permission)
		this
	}
	def takePermission(user: User, permission: Symbol) = {
		bot.bus.post(new TakeEvent(bot, this, user, permission))
		_takePermission(user, permission)
		this
	}

	protected def _grantPermission(user: User, permission: Symbol)
	protected def _takePermission(user: User, permission: Symbol)
}