package cpup.cbot.events.plugin

import cpup.cbot.{Context, CBot}
import cpup.cbot.plugin.Plugin
import cpup.cbot.events.Event

case class DisablePluginEvent(bot: CBot, context: Context, plugin: Plugin) extends Event