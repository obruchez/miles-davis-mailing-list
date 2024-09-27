package org.bruchez.olivier.milesdavismailinglist

import scala.io.Source
import scala.util.Using

object Mbox {
  def unescape(string: String): String = {
    val From = "^(>+)From (.+)?".r

    string match {
      case From(quotes, rest) => quotes.substring(1) + "From " + Option(rest).getOrElse("")
      case _                  => string
    }
  }

  def parse(file: String): Seq[GmailEmail] = {
    val source = Source.fromFile(file)

    Using.resource(source) { source =>
      Utils.splitMessages(
        lines = source.getLines.toList,
        currentMessage = List.empty,
        acc = List.empty,
        splitTest =  _.startsWith("From "),
        includeSplitLine = true
      ).map(GmailEmail.fromString)
    }
  }
}
