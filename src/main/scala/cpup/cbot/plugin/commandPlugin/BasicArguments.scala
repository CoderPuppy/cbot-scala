package cpup.cbot.plugin.commandPlugin

case class BasicArguments(args: Arguments, argDefs: Map[ArgDef[_], Any]) {
	def apply(i: Int) = args(i)
	def apply(name: String) = args(name)
	def apply[T](argDef: ArgDef[T]) = argDefs(argDef).asInstanceOf[T]
}