package cpup.cbot.plugin.commandPlugin

import cpup.cbot.plugin.commandPlugin.CommandPlugin.TCommandEvent

trait TCommand[A] {
	def name: String
	def usages: Seq[String]
	def parse(e: TCommandEvent): A
	def handle(e: TCommandEvent, args: A)
}