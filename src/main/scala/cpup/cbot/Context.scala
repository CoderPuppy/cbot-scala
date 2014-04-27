package cpup.cbot

import cpup.cbot.plugin.{PluginType, Plugin}
import cpup.cbot.users.User
import cpup.cbot.events.user.{TakeEvent, GrantEvent}
import com.google.common.eventbus.EventBus
import cpup.cbot.events.plugin.{DisablePluginEvent, EnablePluginEvent}
import cpup.cbot.events.Replyable
import scala.collection.mutable

trait Context {
	def bot: CBot

	val bus = new EventBus

	protected var _plugins = new mutable.HashMap[String, Plugin]
	def plugins = _plugins.values.toSet

	def enablePlugin(pluginTypes: Map[String, PluginType[Plugin]], name: String): Context = {
		enablePlugin(pluginTypes(name).create(this, pluginTypes))
	}

	def disablePlugin(pluginTypes: Map[String, PluginType[Plugin]], name: String): Context = {
		disablePlugin(pluginTypes(name).create(this, pluginTypes))
	}

	def enablePlugin(plugin: Plugin) = {
		if(_plugins.getOrElse(plugin.toString, null) != plugin) {
			_plugins(plugin.toString) = plugin
			plugin.enable(this)
			bot.bus.post(new EnablePluginEvent(bot, this, plugin))
		}
		this
	}

	def disablePlugin(plugin: Plugin) = {
		if(_plugins.getOrElse(plugin.toString, null) == plugin) {
			_plugins.remove(plugin.toString)
			plugin.disable(this)
			bot.bus.post(new DisablePluginEvent(bot, this, plugin))
		}
		this
	}

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

	def output: Option[MessageOutput[_]]
}