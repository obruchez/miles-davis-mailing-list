package org.bruchez.olivier.milesdavismailinglist

case class GmailEmail(headers: String, message: String) {
  lazy val mailingListEmails: Seq[MailingListEmail] = MailingListEmail.fromGmailEmail(this)

  lazy val averageEpochSecond: Long = {
    val epochSeconds =
      mailingListEmails
        .map(_.date)
        .filter(d => d.getYear >= MilesDavisMailingList.StartYear && d.getYear <= MilesDavisMailingList.EndYear)
        .map(_.toInstant.getEpochSecond.toDouble)

    (epochSeconds.sum / epochSeconds.size).toLong
  }

}

object GmailEmail {
  def fromString(s: String): GmailEmail = {
    val (headers, message) = Utils.beforeAndAfterEmptyLine(s)
    GmailEmail(headers, message)
  }
}
