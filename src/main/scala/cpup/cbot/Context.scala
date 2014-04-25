package cpup.cbot

import cpup.cbot.plugin.PluginManager
import cpup.cbot.users.User

trait Context extends PluginManager {
	def getPermissions(user: User): Set[Symbol]

	def checkPermission(user: User, permission: Symbol) = getPermissions(user).contains(permission)
	def grantPermission(user: User, permission: Symbol): Context
}