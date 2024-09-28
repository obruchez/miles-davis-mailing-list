package org.bruchez.olivier.milesdavismailinglist

import java.nio.file.Paths

object MilesDavisMailingList {
  val StartYear = 1995
  val EndYear = 2010

  def main(args: Array[String]): Unit = {
    val mboxFile = args.head
    val outputDirectory = args(1)

    println(s"MBOX file: $mboxFile")
    println(s"Output directory: $outputDirectory")

    val gmailEmails = Mbox.parse(mboxFile).filter(_.mailingListEmails.nonEmpty).sortBy(_.averageEpochSecond)
    println(s"Gmail emails: ${gmailEmails.size}")

    GmailEmail.dumpMissingLogs(gmailEmails)

    val mailingListEmails = gmailEmails.flatMap(_.mailingListEmails)
    MailingListEmail.checkProblematicTimezones(mailingListEmails)
    val mailingListEmailsWithIds = MailingListEmail.withIds(mailingListEmails)
    val mailingListEmailsWithFixedDates = MailingListEmail.withFixedDates(mailingListEmailsWithIds)
    println(s"Mailing list emails: ${mailingListEmailsWithFixedDates.size}")

    mailingListEmailsWithFixedDates.foreach { email =>
      if (email.date.toInstant.getEpochSecond == email.fixedDateOpt.get.toInstant.getEpochSecond) {
        println(s"${email.filename}: ${email.date}")
      } else {
        println(s"${email.filename}: ${email.date} -> ${email.fixedDateOpt.get}")
      }
    }

    MailingListEmail.checkMissingMonths(mailingListEmailsWithFixedDates)

    val mailingListEmailsByYear = mailingListEmailsWithFixedDates.groupBy(_.fixedDateOpt.get.getYear)
    mailingListEmailsByYear.toSeq.sortBy(_._1).foreach { case (year, emails) =>
      println(s"$year: ${emails.size}")
    }

    MailingListEmail.saveAllAsEmlAndMbox(Paths.get(outputDirectory), mailingListEmailsWithFixedDates)
  }
}
