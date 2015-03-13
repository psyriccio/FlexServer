/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mokko.flex

import com.google.common.io.Files
import java.io.ByteArrayInputStream
import java.io.File
import spray.http._
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset
import spray.http.MediaTypes._
import spray.routing._

trait MainRoute extends Directives with AppLogging {

  val mainRoute: Route = {
    
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
    //static("css", "../../scala-2.11/resource_managed/main/css") ~
    //static("pub", "../../../web") ~
    static("exchange", FlexServer.exchangeDir.getCanonicalPath()) ~
    path_("") {
      get {
        log.info(s"GET ${requestUri.toString}")
        complete {
          s"${buildinfo.buildInfo.name} ${buildinfo.buildInfo.version}"
        }
      } ~
      post {
        log.info(s"POST ${requestUri.toString}")
        entity(as[MultipartFormData]) { formData =>
          log.info(s"formData = ${formData.fields.mkString}")
          complete {
            val details = formData.fields.map {
              case (BodyPart(entity, headers)) =>
                log.info(s"data.length = ${entity.data.length}")
                val content = new ByteArrayInputStream(entity.data.toByteArray)
                log.info(s"length = ${content.available}")
                //val contentType = headers.find(h => h.is("content-type")).get.value
                //log.info(s"content-type = ${contentType}")
                val fileName = headers.find(h => h.is("content-disposition")).get.value.split("filename=").last
                log.info(s"fileName = ${fileName}")
                val result = saveAttachment(s"${FlexServer.exchangeDir.getCanonicalPath()}/${fileName}", content)
                //(contentType, fileName, result)
              case _ =>
            }
            s"""{"status": "Processed POST request, details=$details" }"""
          }
        }
      }
    }
  }
  private def saveAttachment(fileName: String, content: Array[Byte]): Boolean = {
    saveAttachment[Array[Byte]](fileName, content, {(is, os) => os.write(is)})
    true
  }

  private def saveAttachment(fileName: String, content: InputStream): Boolean = {
    saveAttachment[InputStream](fileName, content,
    { (is, os) =>
      val buffer = new Array[Byte](16384)
      Iterator
        .continually (is.read(buffer))
        .takeWhile (-1 !=)
        .foreach (read=>os.write(buffer,0,read))
    }
    )
  }

  private def saveAttachment[T](fileName: String, content: T, writeFile: (T, OutputStream) => Unit): Boolean = {
    try {
      val fos = new java.io.FileOutputStream(fileName)
      writeFile(content, fos)
      fos.close()
      true
    } catch {
      case _:Throwable => false
    }
  }
  

}
