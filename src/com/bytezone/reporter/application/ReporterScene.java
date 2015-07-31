package com.bytezone.reporter.application;

import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.prefs.Preferences;

import javax.swing.SwingUtilities;

import com.bytezone.reporter.application.TreePanel.FileNode;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Pagination;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

public class ReporterScene extends Scene
    implements PaginationChangeListener, FileSelectionListener
{
  private final static String OS = System.getProperty ("os.name");
  private final static boolean MAC_MENUBAR = OS != null && OS.startsWith ("Mac");

  private FormatBox formatBox;
  private final TreePanel treePanel;

  private final BorderPane borderPane;
  private final MenuBar menuBar = new MenuBar ();

  private final Preferences prefs;

  public ReporterScene (Preferences prefs, BorderPane root)
  {
    super (root, 800, 592);

    this.borderPane = root;
    this.prefs = prefs;

    String home = System.getProperty ("user.home") + "/Dropbox/testfiles";

    treePanel = new TreePanel (prefs);
    treePanel.addFileSelectionListener (this);
    StackPane stackPane = new StackPane ();
    stackPane.setPrefWidth (180);

    TreeView<FileNode> tree = treePanel.getTree (home);
    stackPane.getChildren ().add (tree);

    borderPane.setLeft (stackPane);
    borderPane.setTop (menuBar);

    menuBar.getMenus ().addAll (getFileMenu ());

    if (MAC_MENUBAR)
      menuBar.useSystemMenuBarProperty ().set (true);

    tree.requestFocus ();
  }

  public TreePanel getTreePanel ()
  {
    return treePanel;
  }

  @Override
  public void fileSelected (FileNode fileNode)
  {
    formatBox = fileNode.formatBox;
    borderPane.setRight (formatBox.getFormattingBox ());
    formatBox.setFileNode (fileNode, this);
  }

  private Menu getFileMenu ()
  {
    Menu menuFile = new Menu ("File");

    MenuItem menuItemOpen = getMenuItem ("Open...", e -> openFile (), KeyCode.O);
    MenuItem menuItemSave = getMenuItem ("Save...", e -> saveFile (), KeyCode.S);
    MenuItem menuItemPrint = getMenuItem ("Page setup", e -> pageSetup (), null);
    MenuItem menuItemPageSetup = getMenuItem ("Print", e -> printFile (), KeyCode.P);
    MenuItem menuItemClose = getMenuItem ("Close window", e -> closeWindow (), KeyCode.W);

    menuFile.getItems ().addAll (menuItemOpen, menuItemSave, menuItemPageSetup,
                                 menuItemPrint, menuItemClose);
    return menuFile;
  }

  private MenuItem getMenuItem (String text, EventHandler<ActionEvent> eventHandler,
      KeyCode keyCode)
  {
    MenuItem menuItem = new MenuItem (text);
    menuItem.setOnAction (eventHandler);
    if (keyCode != null)
      menuItem.setAccelerator (new KeyCodeCombination (keyCode,
          KeyCombination.SHORTCUT_DOWN));
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
        PrinterJob printerJob = PrinterJob.getPrinterJob ();// AWT

        if (printerJob.printDialog ())
        {
          try
          {
            printerJob.setPrintable (formatBox.getSelectedReportMaker ());
            printerJob.print ();
          }
          catch (PrinterException e)
          {
            e.printStackTrace ();
          }
        }
      }
    });
  }

  private void saveFile ()
  {
    System.out.println ("Save");
  }

  private void closeWindow ()
  {
    System.out.println ("Close");
  }

  @Override
  public void paginationChanged (Pagination pagination)
  {
    borderPane.setCenter (pagination);
  }
}