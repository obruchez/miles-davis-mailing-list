package org.bruchez.olivier.milesdavismailinglist

import java.nio.file.Paths

object MilesDavisMailingList {
  val StartYear = 1995
  val EndYear = 2010

  def main(args: Array[String]): Unit = {
    if (args.length < 2) {
      println("Usage: MilesDavisMailingList <mbox files>... <output directory>")
      sys.exit(1)
    }

    val mboxFiles = args.init.toSeq
    val outputDirectory = args.last

    println(s"MBOX file(s): ${mboxFiles.mkString(", ")}")
    println(s"Output directory: $outputDirectory")
    println()

    val gmailEmails = mboxFiles.flatMap(Mbox.parse).filter(_.mailingListEmails.nonEmpty).sortBy(_.averageEpochSecond).distinctBy(_.averageEpochSecond)
    println(s"Gmail emails: ${gmailEmails.size}")
    println()

    GmailEmail.dumpMissingLogs(gmailEmails)

    val mailingListEmails = gmailEmails.flatMap(_.mailingListEmails)
    MailingListEmail.checkProblematicTimezones(mailingListEmails)
    val mailingListEmailsWithIds = MailingListEmail.withIds(mailingListEmails)
    val mailingListEmailsWithFixedDates = MailingListEmail.withFixedDates(mailingListEmailsWithIds)
    println(s"Mailing list emails: ${mailingListEmailsWithFixedDates.size}")
    println()

    println("Fixed dates:")
    mailingListEmailsWithFixedDates.foreach { email =>
      if (email.date.toInstant.getEpochSecond == email.fixedDateOpt.get.toInstant.getEpochSecond) {
        println(s" - ${email.date}")
      } else {
        println(s" - ${email.date} -> ${email.fixedDateOpt.get}")
      }
    }
    println()

    MailingListEmail.checkMissingMonths(mailingListEmailsWithFixedDates)

    println("Mailing list emails by year:")
    val mailingListEmailsByYear = mailingListEmailsWithFixedDates.groupBy(_.fixedDateOpt.get.getYear)
    mailingListEmailsByYear.toSeq.sortBy(_._1).foreach { case (year, emails) =>
      println(s"$year: ${emails.size}")
    }

    MailingListEmail.saveAllAsEmlAndMbox(Paths.get(outputDirectory), mailingListEmailsWithFixedDates)
  }
}
