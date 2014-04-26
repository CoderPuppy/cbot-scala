package cpup.cbot.events.channel

import cpup.cbot.CBot
import cpup.cbot.channels.Channel
import cpup.cbot.events.Event

case class LeaveEvent(bot: CBot, channel: Channel) extends Event with ChannelEvent