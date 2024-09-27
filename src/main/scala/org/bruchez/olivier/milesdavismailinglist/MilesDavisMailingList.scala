package org.bruchez.olivier.milesdavismailinglist

import scala.util.{Failure, Try}

object MilesDavisMailingList {
  val StartYear = 1995
  val EndYear = 2010

  def main(args: Array[String]): Unit = {
    val gmailEmails = Mbox.parse(args.head).filter(_.mailingListEmails.nonEmpty).sortBy(_.averageEpochSecond)
    println(s"Gmail emails: ${gmailEmails.size}")

    val mailingListEmails = gmailEmails.flatMap(_.mailingListEmails).zipWithIndex.map { case (email, index) =>
      email.copy(idOpt = Some(index + 1))
    }
    println(s"Mailing list emails: ${mailingListEmails.size}")

    mailingListEmails.foreach(m => println(m.filename))

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
