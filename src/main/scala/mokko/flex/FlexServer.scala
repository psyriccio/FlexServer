/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mokko.flex

import akka.actor.ActorSystem

object FlexServer extends App {
  
  val akkaSystem = ActorSystem("flex-server")

  val log = akkaSystem.log
  
  
}

trait AppLogging {
  val log = FlexServer.log
}
