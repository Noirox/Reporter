package com.bytezone.reporter.application;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.prefs.Preferences;

import com.bytezone.reporter.application.Formatter.EncodingType;
import com.bytezone.reporter.application.Formatter.FormatType;
import com.bytezone.reporter.record.CrRecordMaker;
import com.bytezone.reporter.record.CrlfRecordMaker;
import com.bytezone.reporter.record.FbRecordMaker;
import com.bytezone.reporter.record.LfRecordMaker;
import com.bytezone.reporter.record.NoRecordMaker;
import com.bytezone.reporter.record.NvbRecordMaker;
import com.bytezone.reporter.record.RavelRecordMaker;
import com.bytezone.reporter.record.RdwRecordMaker;
import com.bytezone.reporter.record.Record;
import com.bytezone.reporter.record.RecordMaker;
import com.bytezone.reporter.record.RecordMaker.RecordType;
import com.bytezone.reporter.record.VbRecordMaker;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class Reporter extends Application
{
  private static final String[] fontNames =
      { "Andale Mono", "Anonymous Pro", "Consolas", "Courier New", "DejaVu Sans Mono",
        "Hermit", "IBM 3270", "IBM 3270 Narrow", "Inconsolata", "Input Mono",
        "Input Mono Narrow", "Luculent", "Menlo", "Monaco", "M+ 1m", "Panic Sans",
        "PT Mono", "Source Code Pro", "Ubuntu Mono", "Monospaced" };

  private static final String[] files =
      { "MOLONYD.NCD", "password.txt", "denis-000.src", "denis-005.src", "SMLIB-001.src",
        "smutlib001.src", "DBALIB.SRC" };
  private final String[] types = { "FB252", "LF", "CRLF", "RAV", "VB", "RDW", "NVB" };

  private final TextArea textArea = new TextArea ();
  private WindowSaver windowSaver;
  private Preferences prefs;

  private final Formatter formatter = new Formatter ();

  private final ToggleGroup splitterGroup = new ToggleGroup ();
  private RadioButton btnCrlf;
  private RadioButton btnCr;
  private RadioButton btnLf;
  private RadioButton btnFb80;
  private RadioButton btnFb132;
  private RadioButton btnFbOther;
  private RadioButton btnVB;
  private RadioButton btnNvb;
  private RadioButton btnRDW;
  private RadioButton btnRavel;
  private RadioButton btnNoSplit;

  private final ToggleGroup encodingGroup = new ToggleGroup ();
  private RadioButton btnAscii;
  private RadioButton btnEbcdic;

  private final ToggleGroup formattingGroup = new ToggleGroup ();
  private RadioButton btnText;
  private RadioButton btnHex;
  private RadioButton btnNatload;

  private final ToggleGroup pagingGroup = new ToggleGroup ();
  private RadioButton btnNoPaging;
  private RadioButton btn66;
  private RadioButton btnOther;

  @Override
  public void start (Stage primaryStage) throws Exception
  {
    String home = System.getProperty ("user.home") + "/Dropbox/testfiles/";
    int choice = 6;
    Path currentPath = Paths.get (home + files[choice]);

    long fileLength = currentPath.toFile ().length ();
    byte[] buffer = Files.readAllBytes (currentPath);
    System.out.printf ("File size: %,d%n", buffer.length);
    int max = 100_000;
    if (fileLength > max)
    {
      System.out.printf ("Reducing buffer to %,d%n", max);
      byte[] shortBuffer = new byte[max];
      System.arraycopy (buffer, 0, shortBuffer, 0, max);
      buffer = shortBuffer;
    }

    System.out.println ("------------------------");
    for (int i = 0; i < files.length; i++)
      System.out.printf ("%d  %-5s %s%n", i, types[i], files[i]);
    System.out.println ("------------------------");
    System.out.printf ("Using %-5s %s%n", types[choice], files[choice]);

    textArea.setFont (Font.font (fontNames[18], FontWeight.NORMAL, 14));
    textArea.setEditable (false);

    //    Label lblSplit = addLabel ("Records", 20, 120);
    //    Label lblFormat = addLabel ("Formatting", 20, 120);
    //    Label lblEncode = addLabel ("Encoding", 20, 120);
    //    Label lblPrint = addLabel ("Paging", 20, 120);

    EventHandler<ActionEvent> rebuild = e -> rebuild ();

    VBox vbox1 = new VBox (10);
    vbox1.setPadding (new Insets (10));

    RecordMaker crlf = new CrlfRecordMaker (buffer);
    RecordMaker cr = new CrRecordMaker (buffer);
    RecordMaker lf = new LfRecordMaker (buffer);
    RecordMaker fb80 = new FbRecordMaker (buffer, 80);
    RecordMaker fb132 = new FbRecordMaker (buffer, 132);
    RecordMaker fb252 = new FbRecordMaker (buffer, 252);
    RecordMaker vb = new VbRecordMaker (buffer);
    RecordMaker nvb = new NvbRecordMaker (buffer);
    RecordMaker rdw = new RdwRecordMaker (buffer);
    RecordMaker ravel = new RavelRecordMaker (buffer);
    RecordMaker none = new NoRecordMaker (buffer);

    btnNoSplit =
        addRecordTypeButton (none, "None", splitterGroup, rebuild, RecordType.NONE);
    btnNoSplit.setSelected (true);
    btnCrlf = addRecordTypeButton (crlf, "CRLF", splitterGroup, rebuild, RecordType.CRLF);
    btnCr = addRecordTypeButton (cr, "CR", splitterGroup, rebuild, RecordType.CR);
    btnLf = addRecordTypeButton (lf, "LF", splitterGroup, rebuild, RecordType.LF);
    btnVB = addRecordTypeButton (vb, "VB", splitterGroup, rebuild, RecordType.VB);
    btnRDW = addRecordTypeButton (rdw, "RDW", splitterGroup, rebuild, RecordType.RDW);
    btnRavel =
        addRecordTypeButton (ravel, "Ravel", splitterGroup, rebuild, RecordType.RVL);
    btnFb80 = addRecordTypeButton (fb80, "FB80", splitterGroup, rebuild, RecordType.FB80);
    btnFb132 =
        addRecordTypeButton (fb132, "FB132", splitterGroup, rebuild, RecordType.FB132);
    btnFbOther =
        addRecordTypeButton (fb252, "FB252", splitterGroup, rebuild, RecordType.FB252);
    btnNvb = addRecordTypeButton (nvb, "NVB", splitterGroup, rebuild, RecordType.NVB);

    vbox1.getChildren ().addAll (btnNoSplit, btnCrlf, btnCr, btnLf, btnVB, btnNvb, btnRDW,
                                 btnRavel, btnFb80, btnFb132, btnFbOther);

    VBox vbox2 = new VBox (10);
    vbox2.setPadding (new Insets (10));

    btnAscii =
        addEncodingTypeButton ("ASCII", encodingGroup, rebuild, EncodingType.ASCII);
    btnAscii.setSelected (true);
    btnEbcdic =
        addEncodingTypeButton ("EBCDIC", encodingGroup, rebuild, EncodingType.EBCDIC);
    vbox2.getChildren ().addAll (btnAscii, btnEbcdic);

    VBox vbox3 = new VBox (10);
    vbox3.setPadding (new Insets (10));

    btnText = addFormatTypeButton ("Text", formattingGroup, rebuild, FormatType.TEXT);
    btnHex = addFormatTypeButton ("Hex", formattingGroup, rebuild, FormatType.HEX);
    btnNatload =
        addFormatTypeButton ("NatLoad", formattingGroup, rebuild, FormatType.NATLOAD);
    btnHex.setSelected (true);
    vbox3.getChildren ().addAll (btnHex, btnText, btnNatload);

    VBox vbox4 = new VBox (10);
    vbox4.setPadding (new Insets (10));

    btnNoPaging = addRadioButton ("None", pagingGroup, rebuild);
    btnNoPaging.setSelected (true);
    btn66 = addRadioButton ("66", pagingGroup, rebuild);
    btnOther = addRadioButton ("Other", pagingGroup, rebuild);
    vbox4.getChildren ().addAll (btnNoPaging, btn66, btnOther);

    CheckBox chkAsa = new CheckBox ("ASA");
    chkAsa.setOnAction (e -> rebuild ());
    vbox4.getChildren ().addAll (chkAsa);

    TitledPane t1 = new TitledPane ("Records", vbox1);
    TitledPane t2 = new TitledPane ("Encoding", vbox2);
    TitledPane t3 = new TitledPane ("Formatting", vbox3);
    TitledPane t4 = new TitledPane ("Paging", vbox4);

    Accordion accordion = new Accordion ();
    accordion.getPanes ().addAll (t1, t2, t3, t4);

    BorderPane borderPane = new BorderPane ();
    borderPane.setCenter (textArea);
    borderPane.setRight (accordion);

    Scene scene = new Scene (borderPane, 800, 592);
    primaryStage.setTitle ("Reporter");
    primaryStage.setScene (scene);
    primaryStage.setOnCloseRequest (e -> closeWindow ());

    prefs = Preferences.userNodeForPackage (this.getClass ());
    windowSaver = new WindowSaver (prefs, primaryStage, "Reporter");
    if (!windowSaver.restoreWindow ())
      primaryStage.centerOnScreen ();

    rebuild ();
    primaryStage.show ();
  }

  private RadioButton addRecordTypeButton (RecordMaker recordMaker, String text,
      ToggleGroup group, EventHandler<ActionEvent> evt, RecordType recordType)
  {
    RadioButton button = addRadioButton (text, group, evt);
    button.setUserData (recordMaker);
    return button;
  }

  private RadioButton addEncodingTypeButton (String text, ToggleGroup group,
      EventHandler<ActionEvent> evt, EncodingType encodingType)
  {
    RadioButton button = addRadioButton (text, group, evt);
    button.setUserData (encodingType);
    return button;
  }

  private RadioButton addFormatTypeButton (String text, ToggleGroup group,
      EventHandler<ActionEvent> evt, FormatType formatType)
  {
    RadioButton button = addRadioButton (text, group, evt);
    button.setUserData (formatType);
    return button;
  }

  private RadioButton addRadioButton (String text, ToggleGroup group,
      EventHandler<ActionEvent> evt)
  {
    RadioButton button = new RadioButton (text);
    button.setToggleGroup (group);
    button.setOnAction (evt);
    return button;
  }

  //  private Label addLabel (String text, double height, double width)
  //  {
  //    Label label = new Label (text);
  //
  //    label.setPrefWidth (width);
  //    label.setAlignment (Pos.CENTER);
  //    label.setPrefHeight (height);
  //
  //    return label;
  //  }

  private void rebuild ()
  {
    textArea.clear ();

    List<Record> records = setRecordMaker ();
    System.out.printf ("%,d records%n", records.size ());
    setFormatter (records);

    setPageMaker ();

    textArea.positionCaret (0);
  }

  private List<Record> setRecordMaker ()
  {
    RadioButton btn = (RadioButton) splitterGroup.getSelectedToggle ();
    RecordMaker recordMaker = (RecordMaker) btn.getUserData ();

    return recordMaker.getRecords ();
  }

  private void setFormatter (List<Record> records)
  {
    RadioButton btn2 = (RadioButton) formattingGroup.getSelectedToggle ();
    FormatType formatType = (FormatType) btn2.getUserData ();
    formatter.setFormatter (formatType);

    RadioButton btn = (RadioButton) encodingGroup.getSelectedToggle ();
    EncodingType encodingType = (EncodingType) btn.getUserData ();
    formatter.setTextMaker (encodingType);

    formatter.setRecords (records);

    StringBuilder text = new StringBuilder ();
    for (String record : formatter.getFormattedRecords ())
    {
      text.append (record);
      text.append ('\n');
    }

    while (text.length () > 0 && text.charAt (text.length () - 1) == '\n')
      text.deleteCharAt (text.length () - 1);

    textArea.setText (text.toString ());
  }

  private void setPageMaker ()
  {
  }

  private void closeWindow ()
  {
    windowSaver.saveWindow ();
  }

  public static void main (String[] args)
  {
    launch (args);
  }
}