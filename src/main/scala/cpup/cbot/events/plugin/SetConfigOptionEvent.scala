package cpup.cbot.events.plugin

import cpup.cbot.CBot
import cpup.cbot.plugin.Plugin
import cpup.cbot.events.Replyable

case class SetConfigOptionEvent(bot: CBot, plugin: Plugin, reply: Replyable, key: String, value: String) extends UpdatePluginEvent with Replyable {
	override def context = bot

	override def privateReply(msg: String) { reply.privateReply(msg) }
	override def genericReply(msg: String) { reply.genericReply(msg) }
	override def reply(msg: String) { reply.reply(msg) }
}