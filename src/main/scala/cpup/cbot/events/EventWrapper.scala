package cpup.cbot.events

import org.pircbotx.{PircBotX, hooks}
import scala.collection.mutable
import cpup.cbot.CBot
import java.lang.reflect.Constructor
import org.pircbotx.hooks.events.ConnectEvent

object EventWrapper {
	protected val events = new mutable.HashMap[Class[hooks.Event[PircBotX]], Class[Event]]

	protected def classify(pEvent: hooks.Event[PircBotX]) = pEvent.getClass.asInstanceOf[Class[hooks.Event[PircBotX]]]

	def canWrap(pEvent: hooks.Event[PircBotX]) = events.contains(classify(pEvent))
	def wrap(bot: CBot, pEvent: hooks.Event[PircBotX]) = if(canWrap(pEvent)) {
		events.get(classify(pEvent)).get.getConstructor(classOf[CBot], pEvent.getClass).newInstance(bot, pEvent)
	} else { null }

	def register[PE <: hooks.Event[PircBotX], E <: Event](pClass: Class[PE], eClass: Class[E]) {
		if(eClass.getConstructors.find((con: Constructor[_]) => {
			val params = con.getParameterTypes
			params.length == 2 && params(0) == classOf[CBot] && params(1) == pClass
		}).isEmpty) {
			println("no constructor(" + pClass.getCanonicalName + ")")
			System.exit(1)
			throw new Exception("no constructor(" + pClass.getCanonicalName + ")")
		}

		events.put(pClass.asInstanceOf[Class[hooks.Event[PircBotX]]], eClass.asInstanceOf[Class[Event]])
	}

	EventWrapper.register(classOf[ConnectEvent[PircBotX]], classOf[ConnectedEvent])
	EventWrapper.register(classOf[hooks.events.MessageEvent[PircBotX]], classOf[ChannelMessageEvent])
}