package models

trait ZapEvent{
  def event:String // event is a specific SSE attribute
  def price:String
}

case class AirfareMessage(price:String) extends ZapEvent {
  override def event="airfare"
}

case class HotelMessage(price:String) extends ZapEvent{
  override def event="hotel"
}

object Streams {
  
  import scala.util.Random
  
  import play.api.libs.iteratee._
  import play.api.libs.concurrent._

	val airfareStream:Enumerator[ZapEvent] = Enumerator.fromCallback[ZapEvent] {
		() => Promise.timeout( Some(AirfareMessage(Random.nextInt(500)+100+" EUR")), Random.nextInt(3000))
 	}
	
	val hotelStream:Enumerator[ZapEvent] = Enumerator.fromCallback[ZapEvent] {
		() => Promise.timeout( Some(HotelMessage(Random.nextInt(500)+100+" EUR")), Random.nextInt(1500))
 	}

	val events:Enumerator[ZapEvent] = airfareStream >- hotelStream

}