package com.bytezone.reporter.reports;

import java.util.List;

import com.bytezone.reporter.record.Record;

public class HexReport extends DefaultReport
{
  static final int HEX_LINE_SIZE = 16;

  public HexReport (List<Record> records)
  {
    super (records);
  }

  @Override
  public String getFormattedRecord (Record record)
  {
    if (record.length == 0)
      return String.format ("%06X", record.offset);

    StringBuilder text = new StringBuilder ();

    int max = record.offset + record.length;
    for (int ptr = record.offset; ptr < max; ptr += HEX_LINE_SIZE)
    {
      StringBuilder hexLine = new StringBuilder ();

      int lineMax = Math.min (ptr + HEX_LINE_SIZE, max);
      for (int linePtr = ptr; linePtr < lineMax; linePtr++)
        hexLine.append (String.format ("%02X ", record.buffer[linePtr] & 0xFF));

      text.append (String.format ("%06X  %-48s %s%n", ptr, hexLine.toString (),
                                  textMaker.getText (record.buffer, ptr, lineMax - ptr)));
    }

    if (text.length () > 0)
      text.deleteCharAt (text.length () - 1);

    return text.toString ();
  }

  @Override
  protected void paginate ()
  {
    int lineCount = 0;
    Page page = new Page ();
    pages.clear ();

    for (int i = 0; i < records.size (); i++)
    {
      Record record = records.get (i);
      int lines = (record.length - 1) / 16 + 1;
      lineCount += lines;
      if (lineCount > pageSize)
      {
        pages.add (page);
        page = new Page ();
        lineCount = lines;
      }
      if (newlineBetweenRecords)
        lineCount++;
      page.records.add (record);
    }

    if (page.records.size () > 0)
      pages.add (page);
  }
}