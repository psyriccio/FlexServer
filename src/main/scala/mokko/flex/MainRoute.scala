/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mokko.flex

import com.google.common.io.Files
import java.io.File
import spray.http.HttpData
import spray.http.HttpEntity
import spray.http.MediaType
import spray.http.MediaTypes._
import spray.routing.Directives
import spray.routing.Route

trait MainRoute extends Directives with AppLogging {

  val MainRoute: Route = {
    
    def path_(value: String) = {
      (pathPrefix(value) & pathEndOrSingleSlash)
    }
    
   def static(urlPath: String, fromPath: String) = {
      path(urlPath / Rest) { pathRest =>
        log.info(s"GET ${urlPath}/${pathRest}")
        def result(mediaType: MediaType) = {
          respondWithMediaType(mediaType) {
            complete {
              val source = Files.toByteArray(new File(s"${fromPath}/${pathRest}"))
              val entity = HttpEntity(HttpData(source))
              entity
            }
          }
        }
        val suff = pathRest.substring(pathRest.size-3)
        suff match {
          case "css" => result(`text/css`)
          case ".js" => result(`application/javascript`) 
          case _ => result(`application/octet-stream`)
        }
      }
    }
    get {
      //static("css", "../../scala-2.11/resource_managed/main/css") ~
      //static("pub", "../../../web") ~
      //static("js", "../../scala-2.11") ~
      path_("") {
        complete {
          s"Flex-server ${buildinfo.buildInfo.name} ${buildinfo.buildInfo.version}"
        }
      }
    }    
  }
}
