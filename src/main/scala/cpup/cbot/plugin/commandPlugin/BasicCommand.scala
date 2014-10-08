package cpup.cbot.plugin.commandPlugin

import scala.collection.mutable
import cpup.cbot.plugin.commandPlugin.CommandPlugin.TCommandEvent


trait BasicCommand extends TCommand[BasicArguments] {
	def args: List[ArgDef[_]]

	override def usages = List(args.mkString(" "))

	override def parse(e: TCommandEvent) = {
		val argsRes = new mutable.HashMap[ArgDef[_], Any]()
		val numRequired = args.view.count(_.default.isEmpty)
		if(e.args.positional.size < numRequired) {
			throw new InvalidUsageException(s"Not enough arguments, got: ${e.args.positional.size}, required: $numRequired")
		}
		var optionalsLeft = e.args.positional.length - numRequired
		var i = 0
		for(argDef <- args) {
			argsRes(argDef) = argDef.default match {
				case None =>
					i += 1
					argDef.parse(e.bot, e.context, e.ircUser, e.args.positional(i - 1)) match {
						case ArgError(msg: String) =>
							e.reply("Invalid value: " + msg)
							throw new InvalidUsageException("Invalid value: " + msg)

						case Argument(res) => res
					}
				case Some(default) =>
					(if(optionalsLeft > 0) {
						optionalsLeft -= 1
						i += 1
						argDef.parse(e.bot, e.context, e.ircUser, e.args.positional(i - 1))
					} else {
						ArgError("optional")
					}).flatOr(default(e.bot, e.context, e.ircUser)) match {
						case ArgError(msg: String) =>
							throw new RuntimeException("default failed: " + msg)
						case Argument(res) => res
					}
			}
		}
		BasicArguments(e.args, argsRes.toMap)
	}
}