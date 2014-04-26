package cpup.cbot.plugin

/**
 * Created by cpup on 4/25/14.
 */
object SavingPlugin {
	def load(bot: CBot, file: File) {
		val json: JsValue = Json.parse({
			val source = Source.fromFile(file)
			val contents = source.mkString
			source.close
			contents
		})

		(json \ "channels").as[Map[String, JsObject]].values.foreach((json) => {
			bot.channels.join(
				(json \ "name").as[String],
				(json \ "key").validate[String].getOrElse(null)
			).setRejoin((json \ "rejoin").as[Boolean])
		})

		(json \ "users").as[Map[String, JsObject]].values.foreach((userJSON) => {
			val user = bot.users.register(
				(userJSON \ "username").as[String],
				(userJSON \ "password").validate[String].getOrElse(null)
			)

			(userJSON \ "permissions").as[List[String]].foreach((perm) => {
				user.grantPermission(Symbol(perm))
			})

			(userJSON \ "channelPermissions").as[Map[String, List[String]]].foreach((kv) => {
				val (chan, perms) = kv
				perms.foreach((perm) => {
					user.grantPermission(chan, Symbol(perm))
				})
			})
		})

		(json \ "nickServUsers").as[Map[String, String]].foreach((kv) => {
			bot.users.registerNickServ(kv._1, bot.users(kv._2))
		})
	}
}

class SavingPlugin(val file: File) extends Plugin {
	var channels = Json.obj()
	var users = Json.obj()
	var nickServUsers = Json.obj()

	implicit val channelWrites = ChannelWrites
	implicit val userWrites = UserWrites

	def save {
		val json = Json.prettyPrint(Json.obj(
			"channels" -> channels,
			"users" -> users,
			"nickServUsers" -> nickServUsers
		))
		val writer = new PrintWriter(file)
		writer.write(json)
		writer.close
	}

	def updateChannel(chan: Channel) {
		channels -= chan.name
		channels += (chan.name -> Json.toJson(chan))
	}

	def updateUser(user: User) {
		users -= user.username
		users += (user.username -> Json.toJson(user))
	}

	override def enable(manager: PluginManager) = {
		if(!managers.isEmpty) {
			throw new AlreadyRegisteredException("already registered")
		}

		manager match {
			case bot: CBot =>
				super.enable(manager)
				channels = Json.toJson(bot.channels.current.map((chan) => {
					(chan.name, chan)
				}).toMap).asInstanceOf[JsObject]
				users = Json.toJson(bot.users.registeredUsers.toMap).asInstanceOf[JsObject]
				nickServUsers = Json.toJson(bot.users.nickServUsers.toMap.map((kv) => {
					(kv._1, kv._2.username)
				})).asInstanceOf[JsObject]

			case _ =>
				throw new ClassCastException("SavingPlugin only works for CBots")
		}
	}

	override def disable(manager: PluginManager) = {
		super.disable(manager)
		channels = Json.obj()
		users = Json.obj()
	}

	@Subscribe
	def join(e: JoinEvent) {
		updateChannel(e.channel)
		save
	}

	@Subscribe
	def leave(e: LeaveEvent) {
		channels -= e.channel.name
		save
	}

	@Subscribe
	def channelUpdate(e: ChannelUpdateEvent) {
		updateChannel(e.channel)
		save
	}

	@Subscribe
	def userUpdate(e: UserUpdateEvent) {
		updateUser(e.user)
		save
	}

	@Subscribe
	def registerNickServ(e: RegisterNickServEvent) {
		nickServUsers += (e.nickserv -> Json.toJson(e.user.username))
	}

	@Subscribe
	def register(e: RegisterEvent) {
		updateUser(e.user)
		save
	}

	// TODO: use this if unregistering is implemented
//	@Subscribe
//	def unregister(e: UnregisterEvent) {
//		users -= e.user.username
//		save
//	}
}