package cpup.cbot.channels

import cpup.cbot.{Context, CBot}
import cpup.cbot.users.User
import scala.collection.mutable
import play.api.libs.json.{Writes, Json}

case class Channel(bot: CBot, name: String, key: String) extends Context {
	def this(bot: CBot, name: String) {
		this(bot, name, null)
	}

	override def toString = s"#$name"

	var rejoin = false
	def setRejoin(newVal: Boolean) = {
		rejoin = newVal
		this
	}

	val send = new ChannelSend(this)

	override def getPermissions(user: User) = {
		bot.getPermissions(user) ++ user.channelPermissions.getOrElseUpdate(name, new mutable.HashSet[Symbol]())
	}
	override def grantPermission(user: User, permission: Symbol) = {
		user.channelPermissions.addBinding(name, permission)
		this
	}
	override def takePermission(user: User, permission: Symbol) = {
		user.channelPermissions.removeBinding(name, permission)
		this
	}
}

object ChannelWrites extends Writes[Channel] {
	def writes(chan: Channel) = Json.obj(
		"name" -> chan.name,
		"key" -> chan.key,
		"rejoin" -> chan.rejoin
	)
}

object Channel {
	def unifyName(name: String) = if(name.charAt(0) == '#') {
		name.substring(1)
	} else { name }.toLowerCase
}

case class ChannelSend(chan: Channel) {
	def msg(msg: String) = {
		chan.bot.pBot.sendIRC.message(s"#${chan.name}", msg)
		this
	}
}