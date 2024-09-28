package org.bruchez.olivier.milesdavismailinglist

import java.nio.charset.Charset
import java.nio.file.{Files, Path}
import java.time.format.{DateTimeFormatter, DateTimeFormatterBuilder, DateTimeParseException}
import java.time.temporal.ChronoField
import java.time.{LocalDateTime, ZoneId, ZoneOffset, ZonedDateTime}
import java.util.Locale
import scala.annotation.tailrec
import scala.util.{Failure, Try}

case class MailingListEmail(
  headers: String,
  message: String,
  idOpt: Option[Int] = None,
  fixedDateOpt: Option[ZonedDateTime] = None) {

  lazy val date: ZonedDateTime = try {
    val dateString = headers.split('\n').find(_.startsWith("Date:")).map(_.drop("Date: ".length).trim).head
    MailingListEmail.parsedEmailDate(dateString)
  } catch {
    case e: Exception =>
      throw new Exception(s"Unable to parse date in headers: $headers", e)
  }

  val filename: String =
    s"${idOpt.map(id => f"$id%06d").getOrElse("x" * 6)}-${fixedDateOpt.getOrElse(date).toInstant}.eml".replaceAll(":", "-")

  def saveAsEml(directory: Path): Unit = {
    val emlPath = directory.resolve(fixedDateOpt.getOrElse(date).getYear.toString).resolve(filename)
    Files.createDirectories(emlPath.getParent)
    Files.write(emlPath, (headers + "\n\n" + message).getBytes(Charset.forName("UTF-8")))
  }

  def escapedContentForMboxFile: String = {
    val escapedMessageLines = message.split('\n').map { line =>
      val FromToEscape = "^>*From .*".r

      line match{
        case FromToEscape() => ">" + line
        case _              => line
      }
    }

    "From listserv@nic.surfnet.nl\n" + headers + "\n\n" + escapedMessageLines.mkString("\n")
  }
}

object MailingListEmail {
  val separator = "========================================================================="

  def saveAllAsEmlAndMbox(directory: Path, emails: Seq[MailingListEmail]): Unit = {
    emails.foreach(_.saveAsEml(directory))
    emails.groupBy(_.fixedDateOpt.get.getYear).toSeq.sortBy(_._1).foreach { case (year, yearEmails) =>
      saveAsMbox(directory.resolve(s"$year.mbox"), yearEmails)
    }
  }

  def saveAsMbox(mboxFile: Path, emails: Seq[MailingListEmail]): Unit = {
    val mboxContent = emails.map(_.escapedContentForMboxFile).mkString("\n\n") + "\n"
    Files.write(mboxFile, mboxContent.getBytes(Charset.forName("UTF-8")))
  }

  def withIds(emails: Seq[MailingListEmail]): Seq[MailingListEmail] =
    emails.zipWithIndex.map { case (email, index) =>
      email.copy(idOpt = Some(index + 1))
    }

  def withFixedDates(emails: Seq[MailingListEmail]): Seq[MailingListEmail] = {
    val indexedEmails = emails.toIndexedSeq

    def wrongDate(email: MailingListEmail): Boolean =
      email.date.getYear < MilesDavisMailingList.StartYear || email.date.getYear > MilesDavisMailingList.EndYear

    @tailrec
    def emailWithCorrectDate(email: MailingListEmail, startIndex: Int, step: Int): (MailingListEmail, Int) =
      if (! wrongDate(email)) {
        (email, startIndex)
      } else {
        val newStartIndex = startIndex + step

        if (newStartIndex < 0 || newStartIndex >= indexedEmails.size) {
          throw new Exception(s"Unable to fix date at $startIndex with step $step")
        }

        emailWithCorrectDate(indexedEmails(newStartIndex), newStartIndex, step)
      }

    indexedEmails.zipWithIndex.map { case (email, index) =>
      val fixedDate =
        if (! wrongDate(email)) {
          // Keep date as is
          email.date
        } else {
          val (emailBefore, indexBefore) = emailWithCorrectDate(email, index, step = -1)
          val (emailAfter, indexAfter) = emailWithCorrectDate(email, index, step = +1)

          val epochBefore = emailBefore.date.toInstant.getEpochSecond.toDouble
          val epochAfter = emailAfter.date.toInstant.getEpochSecond.toDouble

          val (minEpoch, maxEpoch) =
            if (epochBefore < epochAfter) {
              (epochBefore, epochAfter)
            } else {
              (epochAfter, epochBefore)
            }

          val interpolatedEpoch = minEpoch + (maxEpoch - minEpoch) * (index - indexBefore) / (indexAfter - indexBefore)

          // Fixed date is interpolated date
          ZonedDateTime.ofInstant(java.time.Instant.ofEpochSecond(interpolatedEpoch.round), ZoneOffset.UTC)
        }

      email.copy(fixedDateOpt = Some(fixedDate))
    }
  }

  def checkProblematicTimezones(emails: Seq[MailingListEmail]): Unit = {
    val problematicTimezones =
      emails
        .map(email => Try(email.date))
        .collect { case Failure(t) => t.getMessage.split("\n").head.split(' ').last }
        .distinct
        .sorted

    println(s"Problematic timezones: ${problematicTimezones.size}")
    problematicTimezones.foreach(println)
  }

  def checkMissingMonths(emails: Seq[MailingListEmail]): Unit = {
    val emailsByYear = emails.groupBy(_.date.getYear)
    val missingMonthsByYear = emailsByYear.map { case (year, emails) =>
      val emailsByMonth = emails.groupBy(_.date.getMonthValue)
      val months = emailsByMonth.toSeq.filter(_._2.map(_.date.getDayOfMonth).distinct.size > 5).map(_._1).toSet
      val missingMonths = (1 to 12).filterNot(months.contains)
      (year, missingMonths)
    }

    println(s"Missing months:")
    missingMonthsByYear.toSeq.sortBy(_._1).foreach { case (year, missingMonths) =>
      println(s"$year: ${missingMonths.mkString(", ")}")
    }
  }

  def fromString(s: String): MailingListEmail = {
    val (headers, message) = Utils.beforeAndAfterEmptyLine(s)
    MailingListEmail(headers, message)
  }

  def fromGmailEmail(email: GmailEmail): Seq[MailingListEmail] =
    if (! email.message.contains(separator)) {
      Seq()
    } else {
      val emailStrings = Utils.splitMessages(
        lines = email.message.split("\n").toList,
        currentMessage = List.empty,
        acc = List.empty,
        splitTest = _ == separator,
        includeSplitLine = false
      )

      emailStrings
        .map(_.trim)
        .filter(_.nonEmpty)
        .map(fromString)
    }

  private val zoneIdsFromString: Map[String, ZoneId] = Map(
    "+-100"     -> ZoneOffset.ofHours(+1),
    "+-1000"    -> ZoneOffset.ofHours(+10),
    "+-1100"    -> ZoneOffset.ofHours(+11),
    "+-1300"    -> ZoneOffset.ofHours(+13),
    "+-200"     -> ZoneOffset.ofHours(+2),
    "+-800"     -> ZoneOffset.ofHours(+8),
    "+100"      -> ZoneOffset.ofHours(+1),
    "+2000"     -> ZoneOffset.ofHours(-4),
    "+22306404" -> zoneId("EST"),
    "+22306504" -> zoneId("EST"),
    "+22312530" -> zoneId("EST"),
    "+2910"     -> zoneId("Australia/Adelaide"),
    "+73900"    -> zoneId("EST"),
    "--100"     -> zoneId("Europe/Oslo"),
    "-1850"     -> zoneId("Australia/Adelaide"),
    "-24000"    -> zoneId("EST"),
    "-2900"     -> zoneId("EST"),
    "-30000"    -> zoneId("EST"),
    "00100"     -> ZoneOffset.ofHours(+1),
    "ARG"       -> zoneId("America/Argentina/Buenos_Aires"),
    "AST4ADT"   -> zoneId("Atlantic/Bermuda"),
    "BST-1"     -> ZoneOffset.ofHours(0),
    "GMT+200"   -> ZoneOffset.ofHours(+2),
    "MEZ"       -> zoneId("CET"),
    "NSK-7"     -> ZoneOffset.ofHours(+7),
    "SIN"       -> zoneId("Asia/Singapore"),
    "U"         -> ZoneOffset.ofHours(0)
  )

  private val formatter: DateTimeFormatter = new DateTimeFormatterBuilder()
    .appendPattern("EEE, d MMM yyyy HH:mm:ss ")
    .appendPattern("[ZZZZZ][XXXXX][XXX][X]")
    .appendPattern("[z]")
    .parseDefaulting(ChronoField.OFFSET_SECONDS, 0)
    .toFormatter(Locale.ENGLISH)

  private def parsedEmailDate(dateString: String): ZonedDateTime =
    try {
      ZonedDateTime.parse(dateString, formatter)
    } catch {
      case _: DateTimeParseException =>
        val parts = dateString.split(" ")
        val dateWithoutZone = parts.dropRight(1).mkString(" ")
        val localDateTime = LocalDateTime.parse(
          dateWithoutZone,
          DateTimeFormatter.ofPattern("EEE, d MMM yyyy HH:mm:ss", Locale.ENGLISH)
        )
        ZonedDateTime.of(localDateTime, zoneId(parts.last))
    }

  private def zoneId(zoneString: String): ZoneId =
    Try(ZoneId.of(zoneString))
      .orElse(Try(ZoneId.of(ZoneId.SHORT_IDS.get(zoneString))))
      .getOrElse(zoneIdsFromString(zoneString))
}