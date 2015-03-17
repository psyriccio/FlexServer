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
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import spray.http.MediaTypes._
import spray.routing._
import spray.routing.authentication.BasicAuth
import spray.routing.authentication.UserPass

trait MainRoute extends Directives with AppLogging {

  implicit val executionContext = ExecutionContext.global
  
  def flexAuthenticator(userPass: Option[UserPass]): Future[Option[String]] =
  Future {
    if (userPass.exists(up => up.user == FlexServer.httpUser && up.pass == FlexServer.httpPass)) Some(FlexServer.httpUser)
    else None
  }
  
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
    
    def staticFile(urlPath: String, fileDir: String, fileName: String, mediaType: MediaType) = {
      path(urlPath) {
        log.info(s"GET ${urlPath}")
        respondWithMediaType(mediaType) {
          complete {
            val file = new File(new File(fileDir), fileName)
            if(file.exists()) {
              val result = HttpEntity(HttpData(Files.toByteArray(file)))
              file.delete()
              result  
            } else {
              StatusCodes.NotFound
            }
          }
        }
      }
    }
    
    def staticText(urlPath: String, text: String) = {
      path(urlPath) {
        log.info(s"GET ${urlPath}")
        respondWithMediaType(`text/plain`) {
          complete {
            HttpEntity(HttpData(text))
          }
        }
      }
    }
    
    //
    //static("css", "../../scala-2.11/resource_managed/main/css") ~
    //static("pub", "../../../web") ~
    staticFile("get", FlexServer.exchangeDir.getCanonicalPath(), FlexServer.fileNamePattern, `application/octet-stream`) ~
    staticText("name", FlexServer.fileNamePattern) ~
    path_("") {
      headerValueByName("User-Agent") { userAgent =>
        log.info(s"User-Agent = ${userAgent}")
        authenticate(BasicAuth(flexAuthenticator _, realm = "secured")) { userName =>
          log.info(s"AUTH_USER: ${userName}")
          get {
            log.info(s"GET ${requestUri.toString}")
            complete {
              s"${buildinfo.buildInfo.name} ${buildinfo.buildInfo.version}"
            }
          } ~
          post {
            log.info(s"POST ${requestUri.toString()}")
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
                  case any =>
                    log.info("unknown part: ", any)
                }
                s"""{"status": "Processed POST request, details=$details" }"""
              }
            }
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
      case _: Throwable => false
    }
  }
  

}
