package cpup.cbot.events.user

import cpup.cbot.users.IRCUser
import cpup.cbot.events.Event

trait IRCUserEvent extends Event with UserEvent {
	def ircUser: IRCUser
	def user = ircUser.user
}