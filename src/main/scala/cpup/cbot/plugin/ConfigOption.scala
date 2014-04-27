package cpup.cbot.plugin

import cpup.cbot.events.Replyable

trait ConfigOption {
	def name: String
	def usage: String

	def get: String
	def set(reply: Replyable, newVal: String)
}