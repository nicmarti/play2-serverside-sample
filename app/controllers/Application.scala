package controllers

import play.api._
import libs.EventSource.EventNameExtractor
import play.api.mvc._

import play.api.libs._
import play.api.libs.json._
import play.api.libs.iteratee._

import play.api.libs.json.Json._
import models._


object Application extends Controller {

  def index = Action {
    implicit request =>
      Ok(views.html.index())
  }

  val asJson: Enumeratee[ZapEvent, JsValue] = Enumeratee.map[ZapEvent] {
    zapEvent =>
      toJson(Map("event" -> toJson(zapEvent.event), "price" -> toJson(zapEvent.price)))
  }

  def stream(sessionId: String) = Action {
    implicit val eventNameExtractor: EventNameExtractor[JsValue]=EventNameExtractor[JsValue](eventName = (zepEvent)=>zepEvent.\("event").asOpt[String])
    Ok.feed(Streams.events &> asJson ><> EventSource()).as("text/event-stream")
  }
}


