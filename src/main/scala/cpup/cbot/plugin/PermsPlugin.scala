package cpup.cbot.plugin

import com.google.common.eventbus.Subscribe
import cpup.cbot.plugin.commandPlugin.CommandPlugin.{TCommandEvent, TCommandCheckEvent}
import cpup.cbot.plugin.commandPlugin.{ArgDef, BasicArguments, BasicCommand, SubCommand}

object PermsPlugin extends SingletonPlugin {
	def name = "perms"

	@Subscribe
	def perms(e: TCommandCheckEvent) {
		e.command(SubCommand("perms",
			new BasicCommand {
				override def name = "list"

				val contextA = ArgDef.context
				val userA = ArgDef.user
				override def args = List(contextA, userA)

				override def handle(e: TCommandEvent, args: BasicArguments) {
					val context = args(contextA)
					val user = args(userA)
					e.reply(s"${user.username}'s permissions in $context: ${context.getPermissions(user).mkString(", ")}")
				}
			},
			new BasicCommand {
				override def name = "grant"

				val contextA = ArgDef.context
				val userA = ArgDef.user
				val permissionsA = ArgDef.commas(ArgDef.str.withName("permission"))
				override def args = List(contextA, userA, permissionsA)

				override def handle(e: TCommandEvent, args: BasicArguments) {
					val context = args(contextA)
					val user = args(userA)
					val permissions = args(permissionsA)

					val allPermissions = context.checkPermission(e.ircUser.user, 'allPermissions)
					val sharePermissions = context.checkPermission(e.ircUser.user, 'sharePermissions)

					for(permStr <- permissions) {
						val perm = Symbol(permStr)

						if(!(allPermissions || (context.checkPermission(e.ircUser.user, perm) && sharePermissions))) {
							e.reply("Insufficient Permissions")

							return ()
						}

						context.grantPermission(user, perm)
						e.genericReply(s"Granted $perm to ${user.username} in $context")
					}
				}
			},
			new BasicCommand {
				override def name = "take"

				val contextA = ArgDef.context
				val userA = ArgDef.user
				val permissionsA = ArgDef.commas(ArgDef.str.withName("permission"))
				override def args = List(contextA, userA, permissionsA)

				override def handle(e: TCommandEvent, args: BasicArguments) {
					val context = args(contextA)
					val user = args(userA)
					val permissions = args(permissionsA)

					val allPermissions = context.checkPermission(e.ircUser.user, 'allPermissions)
					val takeSamePerms = context.checkPermission(e.ircUser.user, 'takePermissions)

					for(permStr <- permissions) {
						val perm = Symbol(permStr)

						if(!(allPermissions || (context.checkPermission(e.ircUser.user, perm) && takeSamePerms))) {
							e.reply("Insufficient Permissions")

							return ()
						}

						context.takePermission(user, perm)
						e.genericReply(s"Took $perm from ${user.username} in $context")
					}
				}
			}
		))
	}
}