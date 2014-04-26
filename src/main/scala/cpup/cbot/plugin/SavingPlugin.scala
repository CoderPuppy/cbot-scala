package cpup.cbot.plugin

import cpup.cbot.CBot
import java.io.{PrintWriter, File}
import play.api.libs.json._
import scala.io.Source
import cpup.cbot.channels.Channel
import cpup.cbot.users.{AlreadyRegisteredException, User}
import com.google.common.eventbus.Subscribe
import cpup.cbot.events.user._
import cpup.cbot.events.user.UnregisterEvent
import cpup.cbot.events.user.UnregisterNickServEvent
import cpup.cbot.events.user.RegisterEvent
import cpup.cbot.events.channel.JoinEvent
import cpup.cbot.events.user.RegisterNickServEvent
import play.api.libs.json.JsObject
import cpup.cbot.events.channel.UpdateChannelEvent
import cpup.cbot.events.channel.LeaveEvent
import cpup.cbot.events.plugin.{DisablePluginEvent, EnablePluginEvent}
import play.api.data.validation.ValidationError

class SavingPlugin(val pluginManagement: PluginManagementPlugin, pluginTypes: Map[String, PluginType[Plugin]], val file: File) extends Plugin {
	def pluginType: PluginType[_ <: SavingPlugin] = SavingPlugin

	val reversePluginTypes = pluginTypes.map(_.swap)

	var bot: CBot = null

	var channels = Json.obj()
	var users = Json.obj()
	var nickServUsers = Json.obj()
	var plugins = Json.arr()
	var pluginInsts = Json.obj()

	implicit val channelWrites = new Writes[Channel] {
		def writes(chan: Channel) = Json.obj(
			"name" -> chan.name,
			"key" -> chan.key,
			"rejoin" -> chan.rejoin,
			"plugins" -> getPluginsForSaving(chan)
		)
	}
	implicit val userWrites = new Writes[User] {
		override def writes(user: User) = Json.obj(
			"username" -> user.username,
			"password" -> user.password,
			"permissions" -> user.permissions.toList.map(_.name),
			"channelPermissions" -> user.channelPermissions.toMap.map((kv) => (kv._1, kv._2.toList.map(_.name))),
			"plugins" -> getPluginsForSaving(user)
		)
	}

	def save {
		val json = Json.prettyPrint(Json.obj(
			"channels" -> channels,
			"users" -> users,
			"nickServUsers" -> nickServUsers,
			"plugins" -> plugins,
			"pluginInsts" -> pluginInsts
		))
		val writer = new PrintWriter(file)
		writer.write(json)
		writer.close
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
				plugins = getPluginsForSaving(bot)
				this.bot = bot

			case _ =>
				throw new ClassCastException("SavingPlugin only works for CBots")
		}
	}

	override def disable(manager: PluginManager) = {
		super.disable(manager)
		channels = Json.obj()
		users = Json.obj()
		nickServUsers = Json.obj()
		plugins = Json.arr()
		pluginInsts = Json.obj()
	}

	// -- Channels -- \\
	def updateChannel(chan: Channel) {
		channels -= chan.name
		channels += (chan.name -> Json.toJson(chan))
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
	def channelUpdate(e: UpdateChannelEvent) {
		updateChannel(e.channel)
		save
	}

	// -- Users -- \\
	def updateUser(user: User) {
		users -= user.username
		users += (user.username -> Json.toJson(user))
	}

	@Subscribe
	def userUpdate(e: UpdateUserEvent) {
		updateUser(e.user)
		save
	}

	@Subscribe
	def registerNickServ(e: RegisterNickServEvent) {
		nickServUsers += (e.nickserv -> Json.toJson(e.user.username))
	}

	@Subscribe
	def unregisterNickServ(e: UnregisterNickServEvent) {
		nickServUsers -= e.nickserv
		save
	}

	@Subscribe
	def register(e: RegisterEvent) {
		updateUser(e.user)
		save
	}

	@Subscribe
	def unregister(e: UnregisterEvent) {
		users -= e.user.username
		save
	}

	// -- Plugins -- \\
	def getPluginName(pl: Plugin) = pluginManagement.reversePlugins.get(pl) match {
		case Some(name) => s"@${name}"
		case _ => s"${pl.getClass.getName}@${pl.hashCode}"
	}

	def updatePlugin(pl: Plugin): String = {
		val name = getPluginName(pl)
		val inst = (pluginInsts \ name).validate[JsObject].getOrElse(Json.obj(
			"name" -> name,
			"type" -> pl.pluginType.name,
			"data" -> Json.toJson(pl)(pl.pluginType.format(bot, pluginManagement, pluginTypes, file).get.asInstanceOf[Format[Plugin]])
		))
		pluginInsts -= name
		pluginInsts += (name -> inst)
		name
	}

	def getPluginsForSaving(manager: PluginManager): JsArray = new JsArray(manager.plugins.toList.filter((pl) => {
		pl.pluginType.format(bot, pluginManagement, pluginTypes, file).isDefined
	}).map((pl) => {
		new JsString(updatePlugin(pl))
	}))

	@Subscribe
	def enable(e: EnablePluginEvent) {
		if(e.plugin.pluginType.format(bot, pluginManagement, pluginTypes, file).isDefined) {
			val name = updatePlugin(e.plugin)
			e.manager match {
				case bot: CBot =>
					if(plugins.value.find(_ match {
						case str: JsString if str.value == name => true
						case _ => false
					}).isEmpty) {
						plugins :+= Json.toJson(name)
					}

				case chan: Channel =>
					updateChannel(chan)

				case user: User =>
					updateUser(user)

				case _ =>
			}
			save
		}
	}

	@Subscribe
	def disable(e: DisablePluginEvent) {
		if(e.plugin.pluginType.format(bot, pluginManagement, pluginTypes, file).isDefined) {
			val name = getPluginName(e.plugin)
			e.manager match {
				case bot: CBot =>
					plugins = new JsArray(plugins.value.filter(_ match {
						case str: JsString if str == name => false
						case _ => true
					}))

				case user: User =>
					updateUser(user)

				case chan: Channel =>
					updateChannel(chan)

				case _ =>
			}
			save
		}
	}
}

object SavingPlugin extends PluginType[SavingPlugin] {
	def name = "saving"
	def format(bot: CBot, pluginManagement: PluginManagementPlugin, pluginTypes: Map[String, PluginType[Plugin]], file: File) = Some(Format[SavingPlugin](
		new Reads[SavingPlugin] {
			override def reads(json: JsValue) = JsSuccess(new SavingPlugin(pluginManagement, pluginTypes, file))
		},
		new Writes[SavingPlugin] {
			override def writes(o: SavingPlugin) = JsNull
		}
	))

	def load(bot: CBot, pluginManagement: PluginManagementPlugin, pluginTypes: Map[String, PluginType[Plugin]], file: File) {
		val json: JsValue = Json.parse({
			val source = Source.fromFile(file)
			val contents = source.mkString
			source.close
			contents
		})

		implicit val pluginReads = new Reads[Plugin] {
			override def reads(json: JsValue) = {
				(json \ "type")
					.validate[String]
					.flatMap((typeStr) => {
						pluginTypes.get(typeStr) match {
							case Some(pluginType) => JsSuccess(pluginType)
							case _ => JsError(ValidationError("Unknown PluginType", typeStr))
						}
					})
					.flatMap((pluginType) => {
						pluginType.format(bot, pluginManagement, pluginTypes, file) match {
							case Some(format) => JsSuccess(format)
							case _ => JsError(ValidationError("Unloadable", pluginType.name))
						}
					})
					.flatMap((format) => {
						(json \ "data").validate(format)
					})
			}
		}

		val plugins = (json \ "pluginInsts").as[Map[String, Plugin]]

		for(kv <- plugins) {
			if(kv._1.startsWith("@")) {
				pluginManagement.registerPlugin(kv._1.substring(1), kv._2)
			}
		}

		(json \ "plugins").as[List[String]].flatMap(plugins.get(_)).foreach(bot.enablePlugin(_))

		(json \ "channels").as[Map[String, JsObject]].values.foreach((chanJSON) => {
			val chan = bot.channels.join(
				(chanJSON \ "name").as[String],
				(chanJSON \ "key").validate[String].getOrElse(null)
			)

			chan.setRejoin((chanJSON \ "rejoin").as[Boolean])

			(chanJSON \ "plugins").as[List[String]].flatMap(plugins.get(_)).foreach(chan.enablePlugin(_))
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

			(userJSON \ "plugins").as[List[String]].flatMap(plugins.get(_)).foreach(user.enablePlugin(_))
		})

		(json \ "nickServUsers").as[Map[String, String]].foreach((kv) => {
			bot.users.registerNickServ(kv._1, bot.users(kv._2))
		})
	}
}
