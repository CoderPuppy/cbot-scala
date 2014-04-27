package cpup.cbot

trait MessageOutput[THIS <: MessageOutput[THIS]] {
	def msg(msg: String): THIS
}