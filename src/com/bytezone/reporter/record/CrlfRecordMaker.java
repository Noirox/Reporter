package com.bytezone.reporter.record;

import java.util.ArrayList;
import java.util.List;

public class CrlfRecordMaker extends DefaultRecordMaker
{
  public CrlfRecordMaker ()
  {
    super ("CRLF");
  }

  @Override
  protected List<Record> split (byte[] buffer, int offset, int length)
  {
    List<Record> records = new ArrayList<Record> ();
    int start = offset;
    int recordNumber = 0;

    int max = Math.min (offset + length, buffer.length);
    for (int ptr = offset; ptr < max; ptr++)
    {
      if (buffer[ptr] == 0x0A && ptr > offset && buffer[ptr - 1] == 0x0D)
      {
        records.add (new Record (buffer, start, ptr - start - 1, recordNumber++));
        start = ptr + 1;
      }
    }

    if (start < max)
    {
      // ignore 0x1A on the end - added by IND$FILE
      if (start != max - 1 || buffer[max - 1] != 0x1A)
        records.add (new Record (buffer, start, max - start, recordNumber++));
    }

    return records;
  }

  @Override
  protected byte[] join (List<Record> records)
  {
    int bufferLength = 0;

    for (Record record : records)
      bufferLength += record.length + 2;

    byte[] buffer = new byte[bufferLength];

    int ptr = 0;
    for (Record record : records)
    {
      System.arraycopy (record.buffer, record.offset, buffer, ptr, record.length);
      ptr += record.length;
      buffer[ptr++] = 0x0D;
      buffer[ptr++] = 0x0A;
    }

    assert ptr == buffer.length;

    return buffer;
  }
}