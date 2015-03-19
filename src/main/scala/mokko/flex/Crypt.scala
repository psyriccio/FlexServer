/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mokko.flex

import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import java.security.MessageDigest
 
object Sha256 {
  private val sha = MessageDigest.getInstance("SHA-256")
  def digest(s: String): Array[Byte] = {
    sha.digest(s.getBytes)
  }
}

class Crypt(algorithmName: String) {
  
  def encrypt(bytes: Array[Byte], secret: String): Array[Byte] = {
    val secretKey = new SecretKeySpec(Sha256.digest(secret), algorithmName)
    val encipher = Cipher.getInstance(algorithmName + "/ECB/PKCS5Padding")
    encipher.init(Cipher.ENCRYPT_MODE, secretKey)
    encipher.doFinal(bytes)
  }
 
  def decrypt(bytes: Array[Byte], secret: String): Array[Byte] = {
    val secretKey = new SecretKeySpec(Sha256.digest(secret), algorithmName)
    val encipher = Cipher.getInstance(algorithmName + "/ECB/PKCS5Padding")
    encipher.init(Cipher.DECRYPT_MODE, secretKey)
    encipher.doFinal(bytes)
  }

}

object AES extends Crypt("AES")  