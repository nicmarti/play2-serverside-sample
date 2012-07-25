package controllers

import play.api._
import libs.EventSource.EventNameExtractor
import play.api.mvc._

import play.api.libs._
import play.api.libs.json._
import play.api.libs.iteratee._

import play.api.libs.json.Json._
import models._

/**
 * Main application controller
 */
object Application extends Controller {

  // Returns the index page
  def index = Action {
    implicit request =>
      Ok(views.html.index())
  }

  // Adapter from a stream of ZapEvent to a stream of JsValue, to generate Json content.
  // event is a specific keywork in the Server sent events specification.
  // See also http://dev.w3.org/html5/eventsource/
  val asJson: Enumeratee[ZapEvent, JsValue] = Enumeratee.map[ZapEvent] {
    zapEvent =>
      toJson(Map("event" -> toJson(zapEvent.event), "price" -> toJson(zapEvent.price)))
  }

  // This action streams to the web browser some Json content
  def stream = Action {
    // Define an implicit EventNameExtractor wich extract the "event" name from the Json event so that the EventSource() sets the event in the message
    implicit val eventNameExtractor: EventNameExtractor[JsValue]=EventNameExtractor[JsValue](eventName = (zepEvent)=>zepEvent.\("event").asOpt[String])
    // Streams.events is a composition of HotelPrice and AirfarePrice.
    Ok.feed(Streams.events &> asJson ><> EventSource()).as("text/event-stream")

    // identical to infix notation:
    //Ok.feed(Streams.events.&>(asJson.><>(EventSource()))).as("text/event-stream")

    // and identical to :
    //  Ok.feed(Streams.events.through(asJson.compose(EventSource()))).as("text/event-stream")
  }
}


