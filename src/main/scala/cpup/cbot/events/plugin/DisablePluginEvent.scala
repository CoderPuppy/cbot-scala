package cpup.cbot.events.plugin

import cpup.cbot.{Context, CBot}
import cpup.cbot.plugin.{Plugin, PluginManager}
import cpup.cbot.events.Event

case class DisablePluginEvent(bot: CBot, manager: PluginManager, plugin: Plugin) extends Event {
	def context = manager match {
		case context: Context => context
		case _ => bot
	}
}