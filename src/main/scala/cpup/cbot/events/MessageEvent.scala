package cpup.cbot.events

trait MessageEvent extends Event {
	def message: String
}