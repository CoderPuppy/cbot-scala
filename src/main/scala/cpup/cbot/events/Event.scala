package cpup.cbot.events

import cpup.cbot.{Context, CBot}

trait Event {
	def bot: CBot
	def context: Context
}