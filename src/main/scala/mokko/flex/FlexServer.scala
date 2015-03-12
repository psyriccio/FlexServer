/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mokko.flex

import akka.actor.ActorSystem
import akka.io.IO
import com.typesafe.config.ConfigFactory
import java.io.File
import scala.io.StdIn
import spray.can.Http

object FlexServer extends App {
  
  val conf = ConfigFactory.load(
    ConfigFactory.parseFile(
      new File("flexsrv.conf")
    )
  )

  val akkaSystem = ActorSystem("flex-server", conf)
  val log = akkaSystem.log

  val host = if(args.length < 1) conf.getString("flex-server.host") else args(0)
  val port = if(args.length < 2) conf.getInt("flex-server.port") else args(1).toInt
  
  val fileNamePattern = conf.getString("flex-server.fileNamePattern")

  val exchangeDir = new File("exchange")
  exchangeDir.mkdirs()
  log.info(s"${buildinfo.buildInfo.name} ${buildinfo.buildInfo.version}")
  
  val webService = akkaSystem.actorOf(WebServiceActor.props, "flex-server")
  IO(Http)(akkaSystem) ! Http.Bind(webService, host, port = port)
  log.info(s"Bounded to ${host}:${port}")
  
  StdIn.readLine
  akkaSystem.shutdown
  
}

trait AppLogging {
  val log = FlexServer.log
}
