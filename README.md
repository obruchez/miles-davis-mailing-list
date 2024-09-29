# miles-davis-mailing-list

Parser for the Miles Davis SURFnet mailing list archives (1995-2010).

## Context

Back in 2010, when it was announced that the Miles Davis mailing list was going to be shut down, I retrieved most of the messages by issuing `get miles LOGxxxx` commands to the mailing list server. I didn't do anything with the data until 2024, when [someone in the Miles Davis Google Group asked about the old mailing list](https://groups.google.com/g/mileslist/c/9xpZNisc-WA). I then decided to parse the messages stored in my Gmail account and export them to a more manageable format.

Most of the code in this repository tries to detect wrong dates, fix them (while keeping the original headers and message bodies intact), and exports the mailing list messages either as individual EML files or as MBOX files (one per year).

Since the mailing list was private, I can't make those messages publicly available. If you are interested in the mailing list archive, contact me ([olivier@bruchez.org](mailto:olivier@bruchez.org)), and I'll send you a copy of the data.

## Missing messages

For some reason, I'm missing the following batches of emails:

| Log     | Size (bytes) | Date                  | Description                  |
|---------|--------------|-----------------------|------------------------------|
| LOG0910 | 35,686,787   | 2009-10-31 22:30:40   | October 2009 messages        |
| LOG1008 | 43,576,720   | 2010-08-31 22:36:07   | August 2010 messages         |
| LOG1009 | 50,325,598   | 2010-09-30 23:59:59   | September 2010 messages      |
| LOG1012 | 6,801,980    | 2010-12-11 19:01:23   | Early-December 2010 messages |

I did send `get miles LOGxxxx` commands for those batches, multiple times (I still have the emails sent to the mailing list server), but the server never replied with the requested logs.

I'm also missing all messages before 1995.

If you happen to have any of those missing messages (even individual ones), please forward them to me ([olivier@bruchez.org](mailto:olivier@bruchez.org)). I'll then integrate them into the archive.

## Current content of the archive

As of 2024-09-29, the Miles Davis mailing list archive contains 209,050 messages.

- 1995: 5490
- 1996: 7696
- 1997: 10699
- 1998: 9632
- 1999: 11836
- 2000: 10822
- 2001: 17137
- 2002: 10735
- 2003: 13292
- 2004: 12556
- 2005: 17254
- 2006: 19068
- 2007: 13866
- 2008: 18062
- 2009: 17314
- 2010: 13591