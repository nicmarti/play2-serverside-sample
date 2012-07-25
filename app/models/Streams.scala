package models

import play.libs.WS
import play.api.libs.json.JsValue
import play.api.libs.json.Json._
import scala.Some
import play.api.libs.EventSource
import org.apache.commons.lang.StringUtils

// Define a generic event,
trait ZapEvent {
  def event: String

  // event is a specific Server sent event attribute
  def price: String // a price holds the currency
}

// This is an airfare price update
case class AirfareMessage(price: String) extends ZapEvent {
  override def event = "airfare"
}

// This is an hotel price update
case class HotelMessage(price: String) extends ZapEvent {
  override def event = "hotel"
}

// Defines the Enumerator for various kind of ZapEvent
object Streams {

  import scala.util.Random

  import play.api.libs.iteratee._
  import play.api.libs.concurrent._

  // Please note that in Play 2.1.x fromCallback will be rename to generateM
  val airfareStream: Enumerator[ZapEvent] = Enumerator.fromCallback[ZapEvent] {
    () => Promise.timeout(Some(AirfareMessage(Random.nextInt(500) + 100 + " EUR")), Random.nextInt(3000))
  }

  val hotelStream: Enumerator[ZapEvent] = Enumerator.fromCallback[ZapEvent] {
    () => Promise.timeout(Some(HotelMessage(Random.nextInt(500) + 100 + " EUR")), Random.nextInt(1500))
  }

  val airfareEDreams: Enumerator[String] = Enumerator.fromCallback[String] {
    () => {
      play.api.libs.ws.WS.url("http://www.edreams.fr/engine/ItinerarySearch/search").post(Map(
        "buyPath" -> Seq("58"),
        "auxOrBt" -> Seq("0"),
        "searchMainProductTypeName" -> Seq("FLIGHT"),
        "departureLocation" -> Seq("PAR"),
        "arrivalLocation" -> Seq("BOD"),
        "departureDate" -> Seq("03082012"),
        "departureTime" -> Seq("0000"),
        "returnTime" -> Seq("0000"),
        "cabinClass" -> Seq(""),
        "tripTypeName" -> Seq("ROUND_TRIP"),
        "returnDate" -> Seq("05082012"),
        "filterDirectFlights" -> Seq(""),
        "mainAirportsOnly" -> Seq("false"),
        "numAdults" -> Seq("2"),
        "numChilds" -> Seq("0"),
        "numInfants" -> Seq("0"),
        "ctry" -> Seq("FR"),
        "utm_source" -> Seq("shareflights"),
        "utm_medium" -> Seq("web2"),
        "utm_content" -> Seq(""),
        "utm_campaign" -> Seq(""),
        "AirportsType" -> Seq(""),
        "departureCity" -> Seq(""),
        "arrivalCity" -> Seq(""),
        "onlyTrain" -> Seq("")
      )
      ).map {
        content =>
          content.status match {
            case 200 => Some(content.body)
            case error: Int => Some("Error " + error)
          }
      }
    }
  }

  // TODO See how we can use Enumeratee and Iteratee to filter the HTML content...
  val filterPrice: Enumeratee[String, ZapEvent] = Enumeratee.map[String] {
    content =>
      val tableHTML = content.substring(content.indexOf("<div class=\"singleItinerayPrice defaultWhiteText centerAlign\" style='font-size:24px;'>"), content.indexOf("<label id=\"desgloseLabel0"))
//      play.Logger.info("--------------------------------------")
//      play.Logger.info("Streams " + tableHTML)
//      play.Logger.info("--------------------------------------")
      val clean1=tableHTML.replaceAll("<div class=\"singleItinerayPrice defaultWhiteText centerAlign\" style='font-size:24px;'>","").replaceAll(" ","").replaceAll("\n","")
      val clean2=clean1.substring(0,clean1.indexOf("</div>"))
      AirfareMessage(clean2)
  }


  // Adapter from a stream of ZapEvent to a stream of JsValue, to generate Json content.
  // event is a specific keywork in the Server sent events specification.
  // See also http://dev.w3.org/html5/eventsource/
  val asJson: Enumeratee[ZapEvent, JsValue] = Enumeratee.map[ZapEvent] {
    zapEvent =>
      play.Logger.info("asJson> " + zapEvent)
      toJson(Map("event" -> toJson(zapEvent.event), "price" -> toJson(zapEvent.price)))
  }

  val events: Enumerator[ZapEvent] = {
    airfareEDreams.&>(filterPrice)
  }

}