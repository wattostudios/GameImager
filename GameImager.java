////////////////////////////////////////////////////////////////////////////////////////////////
//                                                                                            //
//                                        GAME IMAGER                                         //
//                              Game Image Manipulation Utility                               //
//                                http://www.watto.org/imager                                 //
//                                                                                            //
//                           Copyright (C) 2006-2009  WATTO Studios                           //
//                                                                                            //
// This program is free software; you can redistribute it and/or modify it under the terms of //
// the GNU General Public License published by the Free Software Foundation; either version 2 //
// of the License, or (at your option) any later versions. This program is distributed in the //
// hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranties //
// of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License //
// at http://www.gnu.org for more details. For updates and information about this program, go //
// to the WATTO Studios website at http://www.watto.org or email watto@watto.org . Thanks! :) //
//                                                                                            //
////////////////////////////////////////////////////////////////////////////////////////////////

/***************************************** TODO LIST *****************************************\

////////////////////////////////////////////////////////////////////////////////////////////////
//                                  VERSION RELEASE FEATURES                                  //
////////////////////////////////////////////////////////////////////////////////////////////////

***************           ***************
*************** IMPORTANT ***************
***************           ***************

NEED TO BUILD UP A CLASS DIAGRAM TO CLEARLY DETERMINE WHAT THE GOALS OF THIS PROGRAM ARE.
EG. SINGLE OR MULTIPLE IMAGES, UNIQUE COLOR PALETTES OR COLOR PALETTES FOR EACH IMAGE, ETC.
ONCE WE HAVE DEFINED THIS, CAN MOVE FORWARD AND RE-DEVELOP.
- 1 Default collection, where images are read into it. If an image file contains multiple images, they are all added in
- Saving will save the collection as a single file (if the plugin allows multiple images) or all as individual files otherwise
  - Saved as either Collection###.ext or Image###.ext
- Thumbnails and color palettes listed down the left side, and can be shown as either images or names (like TreeTable in GE)
  - Also can be shown as a tree, with the palette under each image
- Palettes exist for each image individually - if multi images share a palette they are all stored separately
- All true images are converted to a single paletted image with X colors
  - Removes need for color conversions - kinda - but needs ColorConvPalette for reducing numColors still
  - Allow changing numColors
- Images and palettes shown in the main window
- When showing a palette, it should load SidePanel_Palette with shows a separate dirList, import/export palettes, and GradientColorChooser
- Panel of pre-defined color palettes (monochrome, grayscale), and allow users to store color palettes like favorites, with a name


// TESTING
- Basic Version features
- Right-click menus (including for basic version only)

// BUGS
- When changing the colors of a palette, if the palette is used in more than 1 image, all thumbnails for those images should be updated
- If SidePanel_Information is open and then we select multiple files, it does not reload the SidePanel information
  - Don't want to fix by onOpenRequest() because it'll slowdown other SidePanels like SidePanel_Palette (as it would reload it too)

// REQUIRED
- Draw a 1-pixel black box around the shown image - the same as Photoshop does, or change the white background color to something
  different, like a green checkerbox (make this an option!)
- CHANGE THE SAVE PANEL...
  - Has a "Save Type" dropdown, or buttons, that allow user to choose to save as collection, images, or color palette
  - Removes the need for the Export sidepanel
  - Also do similar for Open so that we remove the open palette sidepanel?
- MAKE SURE THAT THE UNREAL TEXTURE VIEWERS HAVE ALL THE GAMES LISTED IN THEM THAT THEY SHOULD
- THINGS TO ADD FOR THE NEW COLOR PALETTE FUNCTIONALITY
  - Help entries for the new functions
  - Redo help screenshots of the overall program
  - Fix screenshots on the website
  - Add the Change Color Palettes functionallity into the website description
  - Palette plugins should show in the PluginList SidePanel
- Implement SPR2 (The Sims) and MIP (Half-Life)
- Sign up at SourceForge

// PREFERRED
- Plugin_DDS_DDS.write() should remember previous choice when saving multiple files at once, but then forget the choice when finished
  saving all the files in the selection. (at the moment, shows popup for each image saved)
- Add -Xmx128m into the classpath for this program, as the program uses a lot of memory (particularly for color conversion).
  - Ask on the java forums whether there is a better way to do it
- SidePanel_Palette
  - Add scrollbar for transparency 0% --> 100% (like the scrollbar for color)
  - Display transparencies in the small squares for each color in the WSPalettePanel
    - Painted over checkerboard

// OPTIONAL
- Pressing the "Delete" key on a thumbnail should remove the image from the collection?
- Allow moving the files around in the archive (changing the order)
- Make Sidepanels for...
  - Importing and exporting palettes
    - Export to CSV (R,G,B,A)
    - Import from CSV (R,G,B,A)
      - Replace directly (index[0] in old palette replaced by index[0] in new palette)
      - Replace matching (compare the old palette to the new palette, and change the indexes so they are a closest match)
  - Resizing
  - Painting
  - Other Photoshop-like things
- Allow pasting of image data from the clipboard

\*********************************************************************************************/

import org.watto.*;
import org.watto.component.*;
import org.watto.event.*;
import org.watto.manipulator.FileExtensionFilter;
import org.watto.xml.*;

import javax.swing.*;
import java.awt.*;
import java.io.*;

/**
**********************************************************************************************
The Game Imager program. This class contains the main interface, loads major components
such as the <code>PluginManager</code>s, and handles toolbar/menubar events.
**********************************************************************************************
**/
public class GameImager extends WSProgram implements WSClickableInterface,
                                                     WSResizableInterface,
                                                     WSWindowFocusableInterface {


  /** A singleton holder for the GameImager program, so other classes can directly access
      the same instance **/
  static GameImager instance = new GameImager();

  WSSidePanelHolder sidePanelHolder;
  WSFileListPanelHolder fileListPanelHolder;


/**
**********************************************************************************************
Not to be used - use "GameImager.getInstance()" instead of "new GameImager()"
**********************************************************************************************
**/
  public GameImager(){
    // DONT PUT THIS LINE HERE, CAUSE IT IS DONE AUTOMATICALLY BY super()
    // EVEN THOUGH super() ISNT CALLED, IT IS RUN BECAUSE THIS CONSTRUCTOR EXTENDS WSProgram
    // AND THUS MUST RUN super() BEFORE THIS CLASS CAN BE BUILT.
    // HAVING THIS LINE CAUSES THE PROCESSES TO BE RUN TWICE, ENDING UP WITH 2 OF
    // EACH PLUGIN, AND STUPID THINGS LIKE THAT.
    //buildProgram(this);


    // add the window focus listener, so it wil reload the dirpanel when focus has regained
    addWindowFocusListener(new WSWindowFocusableListener(this));

    setIconImage(new ImageIcon(getClass().getResource("images/WSFrame/icon.png")).getImage());

    splash.setMessage("ColorPalettes");

    //new Resource_Grayscale().generatePalette();
    //new Resource_Monochrome().generatePalette();

    ((WSStatusBar)WSRepository.get("StatusBar")).setText(Language.get("Welcome"));

    // close the splash screen
    splash.dispose();


    pack();
    setExtendedState(JFrame.MAXIMIZED_BOTH);

    fileListPanelHolder.setMinimumSize(new Dimension(0,0));
    sidePanelHolder.setMinimumSize(new Dimension(0,0));

    WSSplitPane mainSplit = (WSSplitPane)WSRepository.get("MainSplit");
    mainSplit.setDividerSize(5);

    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);


    setVisible(true);

    }


/**
**********************************************************************************************
Returns the singleton instance of the program. This allows other classes to all address the
same instance of the interface, rather than separate instances.
@return the singleton <i>instance</i> of GameImager
**********************************************************************************************
**/
  public static GameImager getInstance(){
    return instance;
    }


/**
**********************************************************************************************
Builds the interface of the program. Can be overwritten if you want to do additional things
when the interface is being constructed, or if you dont want to load the interface from an
XML file.
**********************************************************************************************
**/
  public void constructInterface(){
    super.constructInterface();
    sidePanelHolder = (WSSidePanelHolder)WSRepository.get("SidePanelHolder");
    sidePanelHolder.loadPanel(Settings.get("CurrentSidePanel"));

    fileListPanelHolder = (WSFileListPanelHolder)WSRepository.get("FileListPanelHolder");
    fileListPanelHolder.loadPanel(Settings.get("FileListView"));
    }


/**
**********************************************************************************************
Deletes all the temporary files from the <i>directory</i>.
@param directory the directory that contains the temporary files.
**********************************************************************************************
**/
  public void deleteTempFiles(File directory) {
    try {

      File[] tempFiles = directory.listFiles();

      if (tempFiles == null){
        return;
        }

      for (int i=0;i<tempFiles.length;i++){
        if (tempFiles[i].isDirectory()){
          deleteTempFiles(tempFiles[i]);
          }
        tempFiles[i].delete();
        }

      }
    catch (Throwable t){
      logError(t);
      }
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public static boolean isFullVersion(){
    try {
      new FullVersionVerifier();
      return true;
      }
    catch (Throwable t){
      return false;
      }
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void makeNewArchive(){
    Task_NewArchive task = new Task_NewArchive();
    task.setDirection(UndoTask.DIRECTION_REDO);
    task.redo();
    WSPopup.showMessage("ReadArchive_MakeNewArchive",true);
    //new Thread(task).start();
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void openSidePanelOnStartup(){
    WSPanel panel = sidePanelHolder.getCurrentPanel();
    if (panel instanceof WSPanelPlugin){
      ((WSPanelPlugin)panel).onOpenRequest();
      }
    }


/**
**********************************************************************************************
The event that is triggered from a WSButtonableListener when a button click occurs
@param c the component that triggered the event
@param e the event that occurred
**********************************************************************************************
**/
  public boolean onClick(JComponent c, java.awt.event.MouseEvent e){
    if (! (c instanceof WSComponent)){
      return false;
      }

    String code = ((WSComponent)c).getCode();

    System.out.println("GI: " + code);

    if (c instanceof WSRecentFileMenuItem){
      // opening a recent file - the 'code' is the filename to open
      File recentFile = new File(code);
      if (recentFile.exists()){
        if (Settings.getBoolean("DoubleClickAddsFiles")){
          Task_AddFiles task = new Task_AddFiles(new File[]{recentFile});
          task.setDirection(UndoTask.DIRECTION_REDO);
          new Thread(task).start();
          }
        else {
          Task_ReadArchive task = new Task_ReadArchive(recentFile);
          task.setDirection(UndoTask.DIRECTION_REDO);
          new Thread(task).start();
          }
        }
      }
    else if (c instanceof WSUndoMenuItem){
      // undo a task
      UndoTask task = ((WSUndoMenuItem)c).getTask();
      UndoManager.undo(task);
      }
    else if (c instanceof WSRedoMenuItem){
      // redo a task
      UndoTask task = ((WSRedoMenuItem)c).getTask();
      UndoManager.redo(task);
      }
    else if (c instanceof WSMenuItem || c instanceof WSButton){
      if (code.equals("ReadArchive_Normal")){
        setSidePanel("DirectoryList");
        ((SidePanel_DirectoryList)sidePanelHolder.getCurrentPanel()).changeControls("ReadPanel",false);
        }
      else if (code.equals("ReadArchive_OpenWith")){
        setSidePanel("DirectoryList");
        ((SidePanel_DirectoryList)sidePanelHolder.getCurrentPanel()).changeControls("ReadPanel",false);
        }
      else if (code.equals("ReadPalette")){
        setSidePanel("DirectoryList");
        ((SidePanel_DirectoryList)sidePanelHolder.getCurrentPanel()).changeControls("PalettePanel",false);
        }
      else if (code.equals("WriteArchive")){
        setSidePanel("DirectoryList");
        ((SidePanel_DirectoryList)sidePanelHolder.getCurrentPanel()).changeControls("WritePanel",true);
        }
      else if (code.equals("ExtractSelectedResources")){
        setSidePanel("DirectoryList");
        ((SidePanel_DirectoryList)sidePanelHolder.getCurrentPanel()).changeControls("ExportPanel",true);
        }
      else if (code.equals("ExtractAllResources")){
        setSidePanel("DirectoryList");
        ((SidePanel_DirectoryList)sidePanelHolder.getCurrentPanel()).changeControls("ExportPanel",true);
        }
      else if (code.equals("AddResources")){
        setSidePanel("DirectoryList");
        ((SidePanel_DirectoryList)sidePanelHolder.getCurrentPanel()).changeControls("ModifyPanel",true);
        }
      else if (code.equals("RemoveResources")){
        setSidePanel("DirectoryList");
        ((SidePanel_DirectoryList)sidePanelHolder.getCurrentPanel()).changeControls("ModifyPanel",true);
        }
      else if (code.equals("Palette")){
        setSidePanel("Palette");
        }
      else if (code.equals("PaletteList")){
        setSidePanel("PaletteList");
        }
      else if (code.equals("SelectResources_All")){
        fileListPanelHolder.selectAll();
        WSPopup.showMessage("SelectResources_All",true);
        }
      else if (code.equals("SelectResources_None")){
        fileListPanelHolder.selectNone();
        WSPopup.showMessage("SelectResources_None",true);
        }
      else if (code.equals("SelectResources_Inverse")){
        fileListPanelHolder.selectInverse();
        WSPopup.showMessage("SelectResources_Inverse",true);
        }
      else if (code.equals("Options")){
        setSidePanel("Options");
        }
      else if (code.equals("PluginList")){
        setSidePanel("PluginList");
        }
      else if (code.equals("Information")){
        setSidePanel("Information");
        }
      else if (code.equals("Help")){
        setSidePanel("Help");
        }
      else if (code.equals("About")){
        setSidePanel("About");
        }
      else if (code.equals("NewArchive")){
        makeNewArchive();
        }
      else if (code.equals("CloseProgram")){
        onClose();
        }
      else if (code.length() > 4 && code.substring(0,4).equals("Zoom")){
        setZoom(Integer.parseInt(code.substring(5)));
        }
      else {
        return false;
        }
      return true;
      }

    return false;
    }


/**
**********************************************************************************************
The event that is triggered from a WSClosableListener when a component is closed
**********************************************************************************************
**/
  public boolean onClose(){

    // ask to save the modified archive
    if (GameImager.getInstance().promptToSave()){
      return false;
      }
    ChangeMonitor.set(false);



    // so that the PreviewPanel loads on next startup
    Settings.set("AutoChangedToHexPreview","false");

    deleteTempFiles(new File("temp"));


    // do onClose() on FileListPanel and SidePanel
    sidePanelHolder.onCloseRequest();
    fileListPanelHolder.onCloseRequest();

    // Remember the location of the main split divider
    WSSplitPane mainSplit = (WSSplitPane)WSRepository.get("MainSplit");
    double splitLocationOld = Settings.getDouble("DividerLocation");
    double splitLocationNew = (double)(mainSplit.getDividerLocation()) / (double)(mainSplit.getWidth());
    double diff = splitLocationOld - splitLocationNew;
    if (diff > 0.01 || diff < -0.01){
      // only set if the change is large.
      // this gets around the problem with the split slowly moving left over each load
      Settings.set("DividerLocation",splitLocationNew);
      }

    // Save settings files
    Settings.saveSettings();

    // Saves the interface to XML, in case there were changes made by the program, such as
    // the adding/removal of buttons, or repositioning of elements
    saveInterface();


    ErrorLogger.closeLog();

    System.exit(0);

    return true;
    }


/**
**********************************************************************************************
The event that is triggered from a WSHoverableListener when a component is hovered over
@param c the component that triggered the event
@param e the event that occurred
**********************************************************************************************
**/
  public boolean onHover(JComponent c, java.awt.event.MouseEvent e){
    //statusbar.setText(((JComponent)c).getToolTipText());
    ((WSStatusBar)WSRepository.get("StatusBar")).setText(((JComponent)c).getToolTipText());
    return true;
    }


/**
**********************************************************************************************
The event that is triggered from a WSHoverableListener when a component is no longer hovered
over (ie loses its hover)
@param c the component that triggered the event
@param e the event that occurred
**********************************************************************************************
**/
  public boolean onHoverOut(JComponent c, java.awt.event.MouseEvent e){
    //statusbar.revertText();
    ((WSStatusBar)WSRepository.get("StatusBar")).revertText();
    return true;
    }


/**
**********************************************************************************************
The event that is triggered from a WSResizableListener when a component is resized
@param c the component that triggered the event
@param e the event that occurred
**********************************************************************************************
**/
  public boolean onResize(JComponent c, java.awt.event.ComponentEvent e){
    if (c instanceof WSComponent){
      String code = ((WSComponent)c).getCode();
      if (code.equals("MainSplit")){
        // reposition the splitpane divider when the splitpane changes sizes
        double splitPos = Settings.getDouble("DividerLocation");
        if (splitPos < 0 || splitPos > 1){
          splitPos = 0.7;
          }

        ((WSSplitPane)c).setDividerLocation(splitPos);
        }
      }
    return true;
    }


/**
**********************************************************************************************
The event that is triggered from a WSWindowFocusableListener when a component gains focus
@param c the component that triggered the event
@param e the event that occurred
**********************************************************************************************
**/
  public boolean onWindowFocus(java.awt.event.WindowEvent e){
    if (sidePanelHolder.getCurrentPanelCode().equals("SidePanel_DirectoryList")){
      // reload the directory list
      ((SidePanel_DirectoryList)sidePanelHolder.getCurrentPanel()).reloadDirectoryList();
      }
    return true;
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public boolean promptToSave(){
    if (ChangeMonitor.check()){
      if (isFullVersion()){
        if (ChangeMonitor.popup()){
          // save changes
          setSidePanel("DirectoryList");
          ((SidePanel_DirectoryList)sidePanelHolder.getCurrentPanel()).changeControls("WritePanel",true);
          return true;
          }
        }
      }
    return false;
    }


/**
**********************************************************************************************
  Does a soft reload, after options changes
**********************************************************************************************
**/
  public void reload(){
    fileListPanelHolder.reload();
    sidePanelHolder.reload();

    ((WSStatusBar)WSRepository.get("StatusBar")).setText(Language.get("Welcome"));

    validate();
    repaint();

    }


/**
**********************************************************************************************
  Does a hard reload (rebuilds the entire interface after language/font/interface change)
**********************************************************************************************
**/
  public void rebuild(){

    WSPlugin[] plugins = WSPluginManager.group("DirectoryList").getPlugins();
    for (int i=0;i<plugins.length;i++){
      ((DirectoryListPanel)plugins[i]).constructInterface(new File(Settings.get("CurrentDirectory")));
      }

    plugins = WSPluginManager.group("Options").getPlugins();
    for (int i=0;i<plugins.length;i++){
      ((WSPanelPlugin)plugins[i]).buildObject(new XMLNode());
      }

    constructInterface();
    sidePanelHolder.rebuild();
    fileListPanelHolder.rebuild();
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void setSidePanel(String name){
    Settings.set("AutoChangedToHexPreview","false");
    ((WSSidePanelHolder)WSRepository.get("SidePanelHolder")).loadPanel("SidePanel_" + name);
    WSPopup.showMessage("SidePanelChanged",true);
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void setFileListPanel(String name){
    Settings.set("FileListView",name);
    fileListPanelHolder.loadPanel(name);
    WSPopup.showMessage("FileListViewChanged",true);
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void setZoom(int zoom){
    fileListPanelHolder.setZoom(zoom);
    }


/**
**********************************************************************************************
The main method that starts the program.
@param args the arguments passed in from the commandline.
**********************************************************************************************
**/
  public static void main(String[] args){
    GameImager ge = GameImager.getInstance();
    Archive.makeNewArchive();
    ge.openSidePanelOnStartup();

    if (args.length > 0){
      File fileToOpen = new File(args[0]);
      if (fileToOpen.exists()){
        ((SidePanel_DirectoryList)WSRepository.get("SidePanel_DirectoryList")).readArchive(fileToOpen);
        //sidePanelHolder.reloadPanel();
        }
      }
    }

  }
