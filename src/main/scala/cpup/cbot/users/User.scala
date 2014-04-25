package cpup.cbot.users

import cpup.cbot.{Context, CBot}
import scala.util.Random
import scala.collection.mutable
import cpup.cbot.channels.Channel

case class User(val bot: CBot,
	            var username: String,
	            var password: String) extends Context {
	override def toString = s"@$username"

	var users = Set[IRCUser]()

	val permissions = new mutable.HashSet[Symbol]()
	val channelPermissions = new mutable.HashMap[String, mutable.Set[Symbol]]() with mutable.MultiMap[String, Symbol]

	override def getPermissions(user: User) = if(user == this) Set('all) else Set()
	override def checkPermission(user: User, permission: Symbol) = user == this
	override def grantPermission(user: User, permission: Symbol) = this
	override def takePermission(user: User, permission: Symbol) = this

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