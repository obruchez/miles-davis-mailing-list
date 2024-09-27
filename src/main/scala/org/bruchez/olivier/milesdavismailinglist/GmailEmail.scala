package org.bruchez.olivier.milesdavismailinglist

case class GmailEmail(headers: String, message: String)

object GmailEmail {
  def fromString(s: String): GmailEmail = {
    val (headers, message) = Utils.beforeAndAfterEmptyLine(s)
    GmailEmail(headers, message)
  }
}
