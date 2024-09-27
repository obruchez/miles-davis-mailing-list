package org.bruchez.olivier.milesdavismailinglist

import scala.util.{Failure, Try}

// TODO: order Gmail emails by date
// TODO: associate a unique, incremental ID to each mailing list email
// TODO: detect "holes" in the archive
// TODO: export to individual EML files (one folder per year?)

object MilesDavisMailingList {
  def main(args: Array[String]): Unit = {
    val gmailEmails = Mbox.parse(args.head)
    println(s"Gmail emails: ${gmailEmails.size}")

    val mailingListEmails = gmailEmails.flatMap(MailingListEmail.fromGmailEmail)
    println(s"Mailing list emails: ${mailingListEmails.size}")

    val problematicTimezones =
      mailingListEmails
        .map(email => Try(email.date))
        .collect { case Failure(t) => t.getMessage.split("\n").head.split(' ').last }
        .distinct
        .sorted

    println(s"Problematic timezones: ${problematicTimezones.size}")
    problematicTimezones.foreach(println)

    val mailingListEmailsByYear = mailingListEmails.groupBy(_.date.getYear)
    mailingListEmailsByYear.toSeq.sortBy(_._1).foreach { case (year, emails) =>
      println(s"$year: ${emails.size}")
    }
  }
}
