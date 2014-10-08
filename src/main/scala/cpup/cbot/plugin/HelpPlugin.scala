package cpup.cbot.plugin

import com.google.common.eventbus.Subscribe
import cpup.cbot.CBot
import cpup.cbot.events.Replyable
import cpup.cbot.plugin.commandPlugin.CommandPlugin.{TCommandEvent, TCommandCheckEvent}
import cpup.cbot.plugin.commandPlugin._
import cpup.cbot.plugin.commandPlugin.BasicArguments

object HelpPlugin extends SingletonPlugin {
	def name = "help"

	@Subscribe
	def help(e: TCommandCheckEvent) {
		e.command(new BasicCommand {
			override def name = "help"

			val filterA = ArgDef.str.default((bot, context, ircUser) => ArgResult.from(""))
			override def args = List(filterA)

			override def handle(e: TCommandEvent, args: BasicArguments) {
				val query = new HelpPlugin.HelpQueryEvent(
					e.bot,
					args(filterA),
					e
				)

				e.context.bus.post(query)
				e.bot.bus.post(query)
			}
		})
	}

	class HelpQueryEvent(val bot: CBot, val filter: String, val reply: Replyable) extends TCommandCheckEvent {
		override def command(cmd: TCommand[_]) {
			if(filter == "" || cmd.name == filter) {
				if(cmd.usages.length > 0) {
					if(cmd.usages.length == 1) {
						reply.genericReply(s" - ${cmd.name} ${cmd.usages(0)}")
					} else {
						reply.genericReply(s" -- ${cmd.name}")
						for(usage <- cmd.usages) {
							reply.genericReply(s"  - ${cmd.name} $usage")
						}
					}
				} else {
					reply.reply(s"${cmd.name} - No usage information")
				}
			}
		}

		override def context = bot
	}
}
