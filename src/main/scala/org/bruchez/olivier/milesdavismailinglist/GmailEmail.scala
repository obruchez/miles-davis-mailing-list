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

  def dumpMissingLogs(emails: Seq[GmailEmail]): Unit =
    allLogs.sorted.foreach { log =>
      val lineToFind = s"""Subject: File: "MILES $log""""
      val found = emails.exists(_.headers.contains(lineToFind))
      if (! found) {
        println(s"Missing: $log")
      }
    }

  private val allLogs = Seq(
    "LOG9501",
    "LOG9502",
    "LOG9503",
    "LOG9504",
    "LOG9505",
    "LOG9506",
    "LOG9507",
    "LOG9508",
    "LOG9509",
    "LOG9510",
    "LOG9511",
    "LOG9512",
    "LOG9601",
    "LOG9602",
    "LOG9603",
    "LOG9604",
    "LOG9605",
    "LOG9606",
    "LOG9607",
    "LOG9608",
    "LOG9609",
    "LOG9610",
    "LOG9611",
    "LOG9612",
    "LOG9701",
    "LOG9702",
    "LOG9703",
    "LOG9704",
    "LOG9705",
    "LOG9706",
    "LOG9707",
    "LOG9708",
    "LOG9709",
    "LOG9710",
    "LOG9711",
    "LOG9712",
    "LOG9801",
    "LOG9802",
    "LOG9803",
    "LOG9804",
    "LOG9805",
    "LOG9806",
    "LOG9807",
    "LOG9808",
    "LOG9809",
    "LOG9810",
    "LOG9811",
    "LOG9812",
    "LOG9901",
    "LOG9902",
    "LOG9903",
    "LOG9904",
    "LOG9905",
    "LOG9906",
    "LOG9907",
    "LOG9908",
    "LOG9909",
    "LOG9910",
    "LOG9911",
    "LOG9912",
    "LOG0001",
    "LOG0002",
    "LOG0003",
    "LOG0004",
    "LOG0005",
    "LOG0006",
    "LOG0007",
    "LOG0008",
    "LOG0009",
    "LOG0010",
    "LOG0011",
    "LOG0012",
    "LOG0101",
    "LOG0102",
    "LOG0103",
    "LOG0104",
    "LOG0105",
    "LOG0106",
    "LOG0107",
    "LOG0108",
    "LOG0109",
    "LOG0110",
    "LOG0111",
    "LOG0112",
    "LOG0201",
    "LOG0202",
    "LOG0203",
    "LOG0204",
    "LOG0205",
    "LOG0206",
    "LOG0207",
    "LOG0208",
    "LOG0209",
    "LOG0210",
    "LOG0211",
    "LOG0212",
    "LOG0301",
    "LOG0302",
    "LOG0303",
    "LOG0304",
    "LOG0305",
    "LOG0306",
    "LOG0307",
    "LOG0308",
    "LOG0309",
    "LOG0310",
    "LOG0311",
    "LOG0312",
    "LOG0401",
    "LOG0402",
    "LOG0403",
    "LOG0404",
    "LOG0405",
    "LOG0406",
    "LOG0407",
    "LOG0408",
    "LOG0409",
    "LOG0410",
    "LOG0411",
    "LOG0412",
    "LOG0501",
    "LOG0502",
    "LOG0503",
    "LOG0504",
    "LOG0505",
    "LOG0506",
    "LOG0507",
    "LOG0508",
    "LOG0509",
    "LOG0510",
    "LOG0511",
    "LOG0512",
    "LOG0601",
    "LOG0602",
    "LOG0603",
    "LOG0604",
    "LOG0605",
    "LOG0606",
    "LOG0607",
    "LOG0608",
    "LOG0609",
    "LOG0610",
    "LOG0611",
    "LOG0612",
    "LOG0701",
    "LOG0702",
    "LOG0703",
    "LOG0704",
    "LOG0705",
    "LOG0706",
    "LOG0707",
    "LOG0708",
    "LOG0709",
    "LOG0710",
    "LOG0711",
    "LOG0712",
    "LOG0801",
    "LOG0802",
    "LOG0803",
    "LOG0804",
    "LOG0805",
    "LOG0806",
    "LOG0807",
    "LOG0808",
    "LOG0809",
    "LOG0810",
    "LOG0811",
    "LOG0812",
    "LOG0901",
    "LOG0902",
    "LOG0903",
    "LOG0904",
    "LOG0905",
    "LOG0906",
    "LOG0907",
    "LOG0908",
    "LOG0909",
    "LOG0910",
    "LOG0911",
    "LOG0912",
    "LOG1001",
    "LOG1002",
    "LOG1003",
    "LOG1004",
    "LOG1005",
    "LOG1006",
    "LOG1007",
    "LOG1008",
    "LOG1009",
    "LOG1010",
    "LOG1011",
    "LOG1012"
  )
}
