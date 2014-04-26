package cpup.cbot.users

import cpup.cbot.{Context, CBot}
import scala.util.Random
import scala.collection.mutable
import cpup.cbot.channels.Channel
import play.api.libs.json.Json
import cpup.cbot.events.user.ChangePasswordEvent

case class User(val bot: CBot,
	            var username: String,
	            protected var _password: String) extends Context {
	override def toString = s"@$username"

	def password = _password
	def password_=(newVal: String) = {
		bot.bus.post(new ChangePasswordEvent(bot, this, _password, newVal))
		_password = newVal
		newVal
	}

	var users = Set[IRCUser]()

	val permissions = new mutable.HashSet[Symbol]()
	val channelPermissions = new mutable.HashMap[String, mutable.Set[Symbol]]() with mutable.MultiMap[String, Symbol]

	override def getPermissions(user: User) = if(user == this) Set('all) else Set()
	override def checkPermission(user: User, permission: Symbol) = user == this
	override def _grantPermission(user: User, permission: Symbol) {}
	override def _takePermission(user: User, permission: Symbol) {}

	def grantPermission(permission: Symbol) = {
		bot.grantPermission(this, permission)
		this
	}

	def takePermission(permission: Symbol) = {
		bot.takePermission(this, permission)
		this
	}

	def grantPermission(chan: String, permission: Symbol) = {
		bot.channels(chan).grantPermission(this, permission)
		this
	}

	def takePermission(chan: String, permission: Symbol) = {
		bot.channels(chan).takePermission(this, permission)
		this
	}

	def grantPermission(chan: Channel, permission: Symbol): User = grantPermission(chan.name, permission)
	def takePermission(chan: Channel, permission: Symbol): User = takePermission(chan.name, permission)
}

object User {
	def hash(password: String) = password
}

class GuestUser(ircUser: IRCUser) extends User(ircUser.bot, s"guest_${ircUser.nick}_${Random.nextInt(100)}", null) {
	override def toString = s"guest:${ircUser.nick}"
}
class GuestUserException(msg: String) extends Exception(msg)

class UnknownUserException(val username: String) extends Exception("Unknown user: " + username)
class IncorrectPasswordException(val user: User, val password: String) extends Exception("Incorrect password: \"" + password + "\" for user: " + user.username)