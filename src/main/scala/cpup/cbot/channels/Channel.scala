package cpup.cbot.channels

import cpup.cbot.{Context, CBot}
import cpup.cbot.users.User
import scala.collection.mutable
import cpup.cbot.events.channel.UpdateChannelEvent

case class Channel(bot: CBot, name: String, key: String) extends Context {
	def this(bot: CBot, name: String) {
		this(bot, name, null)
	}

	override def toString = s"#$name"

	protected var _rejoin = false
	def rejoin = _rejoin
	def setRejoin(newVal: Boolean) = {
		_rejoin = newVal
		bot.bus.post(new UpdateChannelEvent(bot, this))
		this
	}

	val send = new ChannelSend(this)

	override def getPermissions(user: User) = {
		bot.getPermissions(user) ++ user.channelPermissions.getOrElseUpdate(name, new mutable.HashSet[Symbol]())
	}
	override protected def _grantPermission(user: User, permission: Symbol) {
		user.channelPermissions.addBinding(name, permission)
	}
	override protected def _takePermission(user: User, permission: Symbol) {
		user.channelPermissions.removeBinding(name, permission)
	}
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