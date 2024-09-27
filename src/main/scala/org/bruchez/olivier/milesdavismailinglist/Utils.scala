package org.bruchez.olivier.milesdavismailinglist

import scala.annotation.tailrec

object Utils {
  @tailrec
  def splitMessages(
    lines: List[String],
    currentMessage: List[String],
    acc: List[String],
    splitTest: String => Boolean,
    includeSplitLine: Boolean
  ): List[String] = {
    lines match {
      case Nil if currentMessage.isEmpty   =>
        acc.reverse
      case Nil                             =>
        (currentMessage.reverse.mkString("\n") :: acc).reverse
      case head :: tail if splitTest(head) =>
        val newAcc = if (currentMessage.isEmpty) acc else currentMessage.reverse.mkString("\n") :: acc
        splitMessages(tail, if (includeSplitLine) List(head) else Nil, newAcc, splitTest, includeSplitLine)
      case head :: tail                    =>
        splitMessages(tail, Mbox.unescape(head) :: currentMessage, acc, splitTest, includeSplitLine)
    }
  }

  def beforeAndAfterEmptyLine(s: String): (String, String) = {
    val lines = s.split("\n")
    (lines.takeWhile(_.nonEmpty).mkString("\n"), lines.dropWhile(_.nonEmpty).drop(1).mkString("\n"))
  }
}
