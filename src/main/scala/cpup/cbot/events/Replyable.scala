package cpup.cbot.events

trait Replyable {
	def reply(msg: String)
	def genericReply(msg: String)
	def privateReply(msg: String)
}