package cpup.cbot.events

import cpup.cbot.users.IRCUser

trait IRCUserEvent extends Event with UserEvent {
	def ircUser: IRCUser
	def user = ircUser.user
}