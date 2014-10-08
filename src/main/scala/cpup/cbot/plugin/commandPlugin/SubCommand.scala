package cpup.cbot.plugin.commandPlugin

import cpup.cbot.plugin.commandPlugin.CommandPlugin.TCommandEvent

case class SubCommand(name: String, subCommands: TCommand[_]*) extends TCommand[(TCommand[_], Any)] {
	val subCommandsMap = subCommands.map((cmd) => (cmd.name, cmd)).toMap[String, TCommand[_]]

	override def usages = subCommands.flatMap((subCommand) => {
		subCommand.usages.map(subCommand.name + " " + _)
	})

	override def handle(e: TCommandEvent, args: (TCommand[_], Any)) {
		args._1.asInstanceOf[TCommand[Any]].handle(e, args._2)
	}

	override def parse(e: TCommandEvent) = if(e.args.positional.length < 1) {
		throw new InvalidUsageException("Not enough arguments")
	} else {
		val arg0 = e.args.positional(0)
		subCommandsMap.get(arg0).map((subcmd) => {
			(subcmd, subcmd.parse(new TCommandEvent {
				override def cmd = subcmd.name

				val args = e.args.slice(1)

				override def reply(msg: String) = e.reply(msg)

				override def genericReply(msg: String) = e.genericReply(msg)

				override def ircUser = e.ircUser

				override def privateReply(msg: String) = e.privateReply(msg)

				override def bot = e.bot
			}))
		}).getOrElse(throw new InvalidUsageException(s"Unknown subcommand: $arg0"))
	}
}