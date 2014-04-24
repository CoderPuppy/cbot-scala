package cpup.cbot.plugin

import com.google.common.eventbus.Subscribe
import cpup.cbot.plugin.CommandPlugin.{TCommandEvent, TCommandCheckEvent}
import cpup.cbot.CBot
import cpup.cbot.events.Replyable

class HelpPlugin extends Plugin {
	@Subscribe
	def help(e: TCommandCheckEvent) {
		e.command(
			name = "help",
			usage = "[command]",
			handle = (e: TCommandEvent, printUsage: () => Unit) => {
				val query = new HelpPlugin.HelpQueryEvent(
					e.bot,
					if(e.args.length == 0) {
						null
					} else {
						e.args(0)
					},
					e
				)

				e.pluginManager.bus.post(query)
				e.bot.bus.post(query)

				()
			}
		)
	}
}

object HelpPlugin {
	class HelpQueryEvent(val bot: CBot, val filter: String, val reply: Replyable) extends TCommandCheckEvent {
		override def command(name: String, usages: List[String], handle: (TCommandEvent, () => Unit) => Any) {
			if(name == filter || filter == null) {
				if(usages.length > 0) {
					if(usages.length == 1) {
						reply.genericReply(s" - $name ${usages(0)}")
					} else {
						reply.genericReply(s" -- $name")
						for(usage <- usages) {
							reply.genericReply(s"  - $name $usage")
						}
					}
				} else {
					reply.reply(s"$name - No usage information")
				}
			}
		}

		override def pluginManager = bot
	}
}
