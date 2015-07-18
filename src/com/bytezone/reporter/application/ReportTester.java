package com.bytezone.reporter.application;

import com.bytezone.reporter.record.Record;
import com.bytezone.reporter.reports.ReportMaker;
import com.bytezone.reporter.text.TextMaker;

public class ReportTester
{
  final ReportMaker reportMaker;
  int validRecords;
  final TextMaker textMaker;

  public ReportTester (ReportMaker reportMaker, TextMaker textMaker)
  {
    this.reportMaker = reportMaker;
    this.textMaker = textMaker;
  }

  public void testRecord (Record record)
  {
    if (reportMaker.test (record, textMaker))
      ++validRecords;
  }
}