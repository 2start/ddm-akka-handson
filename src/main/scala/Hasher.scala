import java.security.MessageDigest

trait Hasher {
  val sha256: MessageDigest = MessageDigest.getInstance("SHA-256")

  def calculateHash(password: String): String = {
    val hashedBytes = sha256.digest(password.getBytes("UTF-8"))
    val stringBuffer = new StringBuffer()
    for (i <- 0 until hashedBytes.length) {
      stringBuffer.append(Integer.toString((hashedBytes(i) & 0xff) + 0x100, 16).substring(1))
    }
    stringBuffer.toString
  }
}