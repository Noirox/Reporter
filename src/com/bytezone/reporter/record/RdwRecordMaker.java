package com.bytezone.reporter.record;

import java.util.ArrayList;
import java.util.List;

public class RdwRecordMaker extends DefaultRecordMaker
{
  @Override
  protected List<Record> split (byte[] buffer, int offset, int length)
  {
    List<Record> records = new ArrayList<Record> ();
    int ptr = offset;
    int recordNumber = 0;

    int max = Math.min (offset + length, buffer.length);
    while (ptr < max)
    {
      int reclen = (buffer[ptr++] & 0xFF) << 8;
      reclen |= buffer[ptr++] & 0xFF;
      if (reclen == 0)
        break;

      int filler = (buffer[ptr++] & 0xFF) << 8;
      filler |= buffer[ptr++] & 0xFF;
      if (filler != 0)
        break;

      int reclen2 = Math.min (reclen - 4, buffer.length - ptr);

      Record record = new Record (buffer, ptr, reclen2, recordNumber++);
      ptr += reclen2;

      records.add (record);
    }
    return records;
  }

  @Override
  protected byte[] join (List<Record> records)
  {
    int bufferLength = 0;
    for (Record record : records)
      bufferLength += record.length + 4;

    byte[] buffer = new byte[bufferLength];
    int ptr = 0;
    for (Record record : records)
    {
      buffer[ptr++] = (byte) ((record.length & 0xFF00) >> 8);
      buffer[ptr++] = (byte) (record.length & 0xFF);
      buffer[ptr++] = (byte) 0;
      buffer[ptr++] = (byte) 0;
      System.arraycopy (record.buffer, record.offset, buffer, ptr, record.length);
    }

    return buffer;
  }
}