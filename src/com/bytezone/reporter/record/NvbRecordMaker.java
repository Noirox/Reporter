package com.bytezone.reporter.record;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.reporter.text.EbcdicTextMaker;
import com.bytezone.reporter.text.TextMaker;

public class NvbRecordMaker extends DefaultRecordMaker
{
  private static final int HEADER_SIZE = 24;
  private static final int SOURCE_SIZE = 94;
  private static final TextMaker ebcdicTextMaker = new EbcdicTextMaker ();

  public NvbRecordMaker (byte[] buffer)
  {
    super (buffer);
  }

  @Override
  protected List<Record> split ()
  {
    List<Record> records = new ArrayList<Record> ();
    int linesLeft = 0;
    int ptr = 0;
    int recordNumber = 0;

    while (ptr < buffer.length)
    {
      int reclen = linesLeft == 0 ? HEADER_SIZE : SOURCE_SIZE;
      if (buffer.length - ptr < reclen)
      {
        System.out.println ("short buffer");
        break;
      }

      if (linesLeft > 0)
        --linesLeft;
      else if (buffer[ptr] != 0 && buffer[ptr] != (byte) 0xFF)
        linesLeft = Integer.parseInt (ebcdicTextMaker.getText (buffer, ptr + 18, 3));

      records.add (new Record (buffer, ptr, reclen, recordNumber++));
      ptr += reclen;
    }
    return records;
  }

  @Override
  protected byte[] join ()
  {
    int bufferLength = 0;
    for (Record record : records)
      bufferLength += record.length;

    byte[] buffer = new byte[bufferLength];

    int ptr = 0;
    for (Record record : records)
    {
      System.arraycopy (record.buffer, record.offset, buffer, ptr, record.length);
      ptr += record.length;
    }

    return buffer;
  }
}