package cpup.cbot.events.channel

import cpup.cbot.events.Event
import cpup.cbot.CBot
import cpup.cbot.channels.Channel

case class UpdateChannelEvent(bot: CBot, channel: Channel) extends Event with ChannelEvent