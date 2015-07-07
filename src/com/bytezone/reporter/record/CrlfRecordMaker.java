package com.bytezone.reporter.record;

import java.util.List;

public class CrlfRecordMaker extends DefaultRecordMaker
{
  public CrlfRecordMaker (byte[] buffer)
  {
    super (buffer);
  }

  public CrlfRecordMaker (List<Record> records)
  {
    super (records);
  }

  @Override
  protected void split ()
  {
    int start = 0;
    int recordNumber = 0;
    for (int ptr = 0; ptr < buffer.length; ptr++)
    {
      if (buffer[ptr] == 0x0A && ptr > start && buffer[ptr - 1] == 0x0D)
      {
        records.add (new Record (buffer, start, ptr - start - 1, recordNumber++));
        start = ptr + 1;
      }
    }
  }

  @Override
  protected byte[] join ()
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

    return buffer;
  }
}