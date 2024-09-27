package org.bruchez.olivier.milesdavismailinglist

import java.time.format.{DateTimeFormatter, DateTimeFormatterBuilder, DateTimeParseException}
import java.time.temporal.ChronoField
import java.time.{LocalDateTime, ZoneId, ZoneOffset, ZonedDateTime}
import java.util.Locale
import scala.util.Try

case class MailingListEmail(headers: String, message: String) {
  lazy val date: ZonedDateTime = try {
    val dateString = headers.split('\n').find(_.startsWith("Date:")).map(_.drop("Date: ".length).trim).head
    MailingListEmail.parsedEmailDate(dateString)
  } catch {
    case e: Exception =>
      throw new Exception(s"Unable to parse date in headers: $headers", e)
  }
}

object MailingListEmail {
  val separator = "========================================================================="

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