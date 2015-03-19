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

object Sha128 {
  private val sha = MessageDigest.getInstance("SHA-256")
  def digest(s: String): Array[Byte] = {
    val ar = sha.digest(s.getBytes)
    Array[Byte](
      ar(0), ar(2), ar(4), ar(6), 
      ar(8), ar(10), ar(12), ar(14), 
      ar(16), ar(18), ar(20), ar(22), 
      ar(24), ar(26), ar(28), ar(30)
    )
  }
}

class Crypt(algorithmName: String) {
  
  def encrypt(bytes: Array[Byte], secret: String): Array[Byte] = {
    val secretKey = new SecretKeySpec(Sha128.digest(secret), algorithmName)
    val encipher = Cipher.getInstance(algorithmName + "/ECB/PKCS5Padding")
    encipher.init(Cipher.ENCRYPT_MODE, secretKey)
    encipher.doFinal(bytes)
  }
 
  def decrypt(bytes: Array[Byte], secret: String): Array[Byte] = {
    val secretKey = new SecretKeySpec(Sha128.digest(secret), algorithmName)
    val encipher = Cipher.getInstance(algorithmName + "/ECB/PKCS5Padding")
    encipher.init(Cipher.DECRYPT_MODE, secretKey)
    encipher.doFinal(bytes)
  }

}

object AES extends Crypt("AES")  