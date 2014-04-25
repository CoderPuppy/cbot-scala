package cpup.cbot.users

import cpup.cbot.CBot
import scala.util.Random
import scala.collection.mutable
import cpup.cbot.channels.Channel

case class User(val bot: CBot,
	            var username: String,
	            var password: String) {
	var users = Set[IRCUser]()

	val permissions = new mutable.HashSet[Symbol]()
	val channelPermissions: mutable.MultiMap[String, Symbol] = new mutable.HashMap[String, mutable.Set[Symbol]]() with mutable.MultiMap[String, Symbol]

	def grantPermission(permission: Symbol) = {
		permissions += permission
		this
	}

	def takePermission(permission: Symbol) = {
		permissions -= permission
		this
	}

	def grantPermission(chan: String, permission: Symbol) = {
		channelPermissions.addBinding(Channel.unifyName(chan), permission)
		this
	}

	def takePermission(chan: String, permission: Symbol) = {
		channelPermissions.removeBinding(Channel.unifyName(chan), permission)
		this
	}

	def grantPermission(chan: Channel, permission: Symbol): User = grantPermission(chan.name, permission)
	def takePermission(chan: Channel, permission: Symbol): User = takePermission(chan.name, permission)
}

object User {
	def hash(password: String) = password
}

class GuestUser(ircUser: IRCUser) extends User(ircUser.bot, s"guest_${ircUser.nick}_${Random.nextInt(100)}", null)
class GuestUserException(msg: String) extends Exception(msg)

class UnknownUserException(val username: String) extends Exception("Unknown user: " + username)
class IncorrectPasswordException(val user: User, val password: String) extends Exception("Incorrect password: \"" + password + "\" for user: " + user.username)