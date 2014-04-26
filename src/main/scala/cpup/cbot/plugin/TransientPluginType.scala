package cpup.cbot.plugin

import cpup.cbot.CBot
import java.io.File

trait TransientPluginType extends PluginType[Plugin] {
	override def writes(bot: CBot, pluginTypes: Map[String, PluginType[Plugin]]) = None
	override def reads(bot: CBot, pluginTypes: Map[String, PluginType[Plugin]], file: File) = None
}