package com.bytezone.reporter.application;

import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.SwingUtilities;

import com.bytezone.reporter.application.TreePanel.FileNode;
import com.bytezone.reporter.file.ReportData;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Pagination;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;

public class ReporterNode implements PaginationChangeListener, NodeSelectionListener
{
  private final static String OS = System.getProperty ("os.name");
  private final static boolean SYSTEM_MENUBAR = OS != null && OS.startsWith ("Mac");

  private final Set<NodeSelectionListener> nodeSelectionListeners = new HashSet<> ();
  private final FormatBox formatBox = new FormatBox (this);
  private final TreePanel treePanel;

  private final BorderPane borderPane = new BorderPane ();
  private final MenuBar menuBar = new MenuBar ();

  private FileNode currentFileNode;

  public ReporterNode (Preferences prefs)
  {
    Path path = Paths.get (System.getProperty ("user.home"), "dm3270", "files");

    treePanel = new TreePanel (prefs);
    treePanel.addNodeSelectionListener (this);

    StackPane stackPane = new StackPane ();
    stackPane.getChildren ().add (treePanel.getTree (path));

    borderPane.setLeft (stackPane);
    borderPane.setTop (menuBar);
    borderPane.setRight (formatBox.getPanel ());

    menuBar.getMenus ().addAll (getFileMenu ());
    menuBar.useSystemMenuBarProperty ().set (SYSTEM_MENUBAR);
  }

  public HBox getRootNode ()
  {
    HBox hbox = new HBox ();
    hbox.getChildren ().add (borderPane);
    return hbox;
  }

  public MenuBar getMenuBar ()
  {
    return menuBar;
  }

  public void requestFocus ()
  {
    treePanel.getTree ().requestFocus ();
  }

  public void addBuffer (String name, byte[] buffer)
  {
    treePanel.addBuffer (name, buffer);
  }

  @Override
  public void nodeSelected (FileNode fileNode)
  {
    currentFileNode = fileNode;

    ReportData reportData = fileNode.getReportData ();
    if (!reportData.hasData ())
      reportData.fillBuffer (fileNode.getFile ());
    if (!reportData.hasScores ())
      reportData.createScores ();
    formatBox.setFileNode (reportData);

    fireNodeSelected (fileNode);
  }

  public FileNode getSelectedNode ()
  {
    return currentFileNode;
  }

  private Menu getFileMenu ()
  {
    Menu menuFile = new Menu ("File");

    getMenuItem (menuFile, "Open...", e -> openFile (), KeyCode.O);
    getMenuItem (menuFile, "Save...", e -> saveFile (), KeyCode.S);

    menuFile.getItems ().add (new SeparatorMenuItem ());

    getMenuItem (menuFile, "Page setup", e -> pageSetup (), null);
    getMenuItem (menuFile, "Print", e -> printFile (), KeyCode.P);

    return menuFile;
  }

  private MenuItem getMenuItem (Menu menu, String text,
      EventHandler<ActionEvent> eventHandler, KeyCode keyCode)
  {
    MenuItem menuItem = new MenuItem (text);

    menuItem.setOnAction (eventHandler);
    if (keyCode != null)
      menuItem.setAccelerator (new KeyCodeCombination (keyCode,
          KeyCombination.SHORTCUT_DOWN));
    menu.getItems ().add (menuItem);

    return menuItem;
  }

  private void openFile ()
  {
    System.out.println ("Open");
  }

  private void pageSetup ()
  {
    SwingUtilities.invokeLater (new Runnable ()
    {
      @Override
      public void run ()
      {
        PrinterJob printerJob = PrinterJob.getPrinterJob ();// AWT
        PageFormat pageFormat = printerJob.defaultPage ();
        printerJob.pageDialog (pageFormat);
      }
    });
  }

  private void printFile ()
  {
    SwingUtilities.invokeLater (new Runnable ()
    {
      @Override
      public void run ()
      {
        PrinterJob printerJob = PrinterJob.getPrinterJob ();// AWT!!!

        //        if (printerJob.printDialog ())
        //        {
        //          try
        //          {
        //            printerJob.setPrintable (formatBox.getSelectedReportMaker ());
        //            printerJob.print ();
        //          }
        //          catch (PrinterException e)
        //          {
        //            e.printStackTrace ();
        //          }
        //        }
      }
    });
  }

  private void saveFile ()
  {
    FileChooser fileChooser = new FileChooser ();
    fileChooser.setInitialFileName (currentFileNode.datasetName);

    //Set extension filter
    //    FileChooser.ExtensionFilter extFilter =
    //        new FileChooser.ExtensionFilter ("TXT files (*.txt)", "*.txt");
    //    fileChooser.getExtensionFilters ().add (extFilter);

    File file = fileChooser.showSaveDialog (null);

    if (file != null && file.isFile () && !file.isHidden ())
      try
      {
        ReportData reportData = currentFileNode.getReportData ();
        //        Files.write (file.toPath (), currentFileNode.getBuffer ());
        Files.write (file.toPath (), reportData.getBuffer ());
      }
      catch (IOException e)
      {
        e.printStackTrace ();
      }
  }

  @Override
  public void paginationChanged (Pagination pagination)
  {
    pagination.setPrefWidth (18000);// need this to make it expand
    borderPane.setCenter (pagination);
  }

  private void fireNodeSelected (FileNode fileNode)
  {
    for (NodeSelectionListener listener : nodeSelectionListeners)
      listener.nodeSelected (fileNode);
  }

  public void addNodeSelectionListener (NodeSelectionListener listener)
  {
    nodeSelectionListeners.add (listener);
  }

  public void removeFileSelectionListener (NodeSelectionListener listener)
  {
    nodeSelectionListeners.remove (listener);
  }
}