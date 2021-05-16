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

import org.watto.*;
import org.watto.component.*;
import org.watto.event.*;
import org.watto.manipulator.FileManipulator;
import org.watto.manipulator.FileExtensionFilter;
import org.watto.xml.*;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileFilter;
import javax.swing.*;

/**
**********************************************************************************************
A PanelPlugin
**********************************************************************************************
**/
public class SidePanel_DirectoryList extends WSPanelPlugin implements WSSelectableInterface,
                                                                      WSEnterableInterface {

  /** The current controls **/
  WSPanel currentControl;

  // Panels and buttons for the control subgroups at the bottom of the sidepanel
  /** The controls for reading archives **/
  WSPanel readControls;
  /** The controls for reading palettes **/
  WSPanel paletteControls;
  /** The controls for modifying archives **/
  WSPanel modifyControls;
  /** The controls for extracting files from archives **/
  WSPanel exportControls;
  /** The controls for writing archives **/
  WSPanel writeControls;


  /** Holder for the controls **/
  WSPanel controlHolder;
  /** Holder for the directory list **/
  WSDirectoryListHolder dirHolder;


/**
**********************************************************************************************
Constructor for extended classes only
**********************************************************************************************
**/
  public SidePanel_DirectoryList(){
    super(new XMLNode());
    }


/**
**********************************************************************************************
Constructor to construct the component from an XMLNode <i>tree</i>
@param node the XMLNode describing this component
@param caller the object that contains this component, created this component, or more formally,
              the object that receives events from this component.
**********************************************************************************************
**/
  public SidePanel_DirectoryList(XMLNode node){
    super(node);
    }



///////////////
//
// Configurable
//
///////////////


/**
**********************************************************************************************
Build this object from the <i>node</i>
@param node the XML node that indicates how to build this object
**********************************************************************************************
**/
  public void buildObject(XMLNode node){
    super.buildObject(node);

    setLayout(new BorderLayout());

    // Build an XMLNode tree containing all the elements on the screen
    XMLNode srcNode = XMLReader.read(new File("settings" + File.separator + "interface_SidePanel_DirectoryList.wsd"));

    // Build the components from the XMLNode tree
    Component component = WSHelper.buildComponent(srcNode);
    add(component,BorderLayout.CENTER);

    // setting up this object in the repository (overwrite the base panel with this object)
    setCode(((WSComponent)component).getCode());
    WSRepository.add(this);

    loadControlPanels();

    controlHolder = (WSPanel)WSRepository.get("SidePanel_DirectoryList_ControlsHolder");
    dirHolder = (WSDirectoryListHolder)WSRepository.get("SidePanel_DirectoryList_DirectoryListHolder");

    //changeControls("ReadPanel",false);

    //loadDirList();
    }


/**
**********************************************************************************************
Builds an XMLNode that describes this object
@return an XML node with the details of this object
**********************************************************************************************
**/
  public XMLNode buildXML(){
    return super.buildXML();
    }


/**
**********************************************************************************************
Registers the events that this component generates
**********************************************************************************************
**/
  public void registerEvents(){
    super.registerEvents();
    }



///////////////
//
// Class-Specific Methods
//
///////////////

/**
**********************************************************************************************

**********************************************************************************************
**/
  public void addFiles(){
    File[] selectedFiles = dirHolder.getAllSelectedFiles();
    addFiles(selectedFiles);
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void addFiles(File selectedFile){
    addFiles(new File[]{selectedFile});
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void addFiles(File[] selectedFiles){
    /*
    ArchivePlugin plugin = Archive.getReadPlugin();
    if (plugin != null){
      if (!plugin.canWrite()){
        WSPopup.showMessage("ModifyArchive_NotWritable",true);
        return;
        }
      }
    */

    Task_AddFiles task = new Task_AddFiles(selectedFiles);
    task.setDirection(UndoTask.DIRECTION_REDO);
    new Thread(task).start();
    UndoManager.add(task);

    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void addFilesDoubleClick(File selectedFile){
    /*
    ArchivePlugin plugin = Archive.getReadPlugin();
    if (plugin != null){
      if (!plugin.canWrite()){
        WSPopup.showMessage("ModifyArchive_NotWritable",true);
        return;
        }
      }
    */


    WSComboBox readPlugins = (WSComboBox)WSRepository.get("SidePanel_DirectoryList_ReadPluginList");
    PluginList selectedItem = (PluginList)readPlugins.getSelectedItem();

    WSPlugin plugin = null;
    if (selectedItem != null){
      plugin = selectedItem.getPlugin();
      }
    else {
      plugin = new AllFilesPlugin();
      }

    if (plugin instanceof AllFilesPlugin){
      // auto-detect a plugin
      Task_AddFiles task = new Task_AddFiles(new File[]{selectedFile});
      task.setDirection(UndoTask.DIRECTION_REDO);
      new Thread(task).start();
      UndoManager.add(task);
      }
    else {
      // open with the chosen plugin
      Task_AddFilesWithPlugin task = new Task_AddFilesWithPlugin(selectedFile,(ArchivePlugin)plugin);
      task.setDirection(UndoTask.DIRECTION_REDO);
      new Thread(task).start();
      UndoManager.add(task);
      }

    }


/**
**********************************************************************************************
Changes the control bar to a different panel
@param controlName the name of the control panel to load
@param fullVersionOnly true if the panel to change to is only a full version feature
**********************************************************************************************
**/
  public void changeControls(String controlName, boolean fullVersionOnly) {

    boolean fullVersion = true;
    if (fullVersionOnly){
      if (!checkFullVersion(false)){
        //return;
        fullVersion = false;
        }
      }

    WSPanel newControl = currentControl;

    // reset the filter on the directoryList
    setFileFilter(null);


    if (controlName.equals("ReadPanel")){
      newControl = readControls;
      loadReadPlugins();
      setPluginFilter("SidePanel_DirectoryList_ReadPluginList","CurrentSelectedReadPlugin");
      }
    else if (controlName.equals("PalettePanel")){
      newControl = paletteControls;
      loadPalettePlugins();
      setPluginFilter("SidePanel_DirectoryList_PalettePluginList","CurrentSelectedPalettePlugin");
      }
    else if (controlName.equals("ModifyPanel")){
      newControl = modifyControls;
      }
    else if (controlName.equals("ExportPanel")){
      newControl = exportControls;
      if (fullVersion){
        loadExportPlugins();
        setPluginFilter("SidePanel_DirectoryList_ExportPluginList","CurrentSelectedExportPlugin",false);
        setExportFilename(dirHolder.getSelectedFile());
        }
      }
    else if (controlName.equals("WritePanel")){
      newControl = writeControls;
      if (fullVersion){
        loadWritePlugins();
        setPluginFilter("SidePanel_DirectoryList_WritePluginList","CurrentSelectedWritePlugin",false);
        setWriteFilename(dirHolder.getSelectedFile());
        }
      }


    controlHolder.removeAll();


    currentControl = newControl;
    controlHolder.add(currentControl,BorderLayout.NORTH);


    controlHolder.revalidate();
    controlHolder.repaint();

    Settings.set("SidePanel_DirectoryList_CurrentControl",controlName);
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public boolean checkFullVersion() {
    return checkFullVersion(true);
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public boolean checkFullVersion(boolean showPopup) {
    try {
      new FullVersionVerifier();
      return true;
      }
    catch (Throwable t){
      // basic version
      if (showPopup){
        WSPopup.showError("FullVersionOnly",true);
        }
      return false;
      }
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void exportAllFiles(){
    String path = ((WSTextField)WSRepository.get("SidePanel_DirectoryList_ExportFilenameField")).getText();

    int dotPos = path.lastIndexOf(".");
    int slashPos = path.lastIndexOf("/");
    if (slashPos == -1){
      slashPos = path.lastIndexOf("\\");
      }

    File directory = new File(path);
    if (dotPos > slashPos){
      // is a file - want a directory name instead
      directory = directory.getParentFile();
      }

    exportFiles(Archive.getResources(),directory);
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void exportSelectedFiles(){
    String path = ((WSTextField)WSRepository.get("SidePanel_DirectoryList_ExportFilenameField")).getText();

    int dotPos = path.lastIndexOf(".");
    int slashPos = path.lastIndexOf("/");
    if (slashPos == -1){
      slashPos = path.lastIndexOf("\\");
      }

    File directory = new File(path);
    if (dotPos > slashPos){
      // is a file - want a directory name instead
      directory = directory.getParentFile();
      }

    Resource[] resources = ((WSFileListPanelHolder)WSRepository.get("FileListPanelHolder")).getSelected();
    exportFiles(resources,directory);
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void exportFiles(Resource[] files, File directory){
    WSComboBox convertPlugins = (WSComboBox)WSRepository.get("SidePanel_DirectoryList_ExportPluginList");
    Object pluginObj = convertPlugins.getSelectedItem();
    if (pluginObj == null || !(pluginObj instanceof PluginList)){
      return;
      }
    ArchivePlugin plugin = ((PluginList)pluginObj).getPlugin();

    exportFiles(files,directory,plugin);
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void exportFiles(Resource[] files, File directory, ArchivePlugin plugin){
    Task_ExportFiles task = new Task_ExportFiles(files,directory,plugin);
    task.setDirection(UndoTask.DIRECTION_REDO);
    new Thread(task).start();
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void loadControlPanels() {
    readControls = (WSPanel)WSHelper.buildComponent(XMLReader.read(new File("settings" + File.separator + "interface_SidePanel_DirectoryList_ReadPanel.wsd")));
    paletteControls = (WSPanel)WSHelper.buildComponent(XMLReader.read(new File("settings" + File.separator + "interface_SidePanel_DirectoryList_PalettePanel.wsd")));

    if (checkFullVersion(false)){
      // full version panels
      modifyControls = (WSPanel)WSHelper.buildComponent(XMLReader.read(new File("settings" + File.separator + "interface_SidePanel_DirectoryList_ModifyPanel.wsd")));
      writeControls = (WSPanel)WSHelper.buildComponent(XMLReader.read(new File("settings" + File.separator + "interface_SidePanel_DirectoryList_WritePanel.wsd")));
      exportControls = (WSPanel)WSHelper.buildComponent(XMLReader.read(new File("settings" + File.separator + "interface_SidePanel_DirectoryList_ExtractPanel.wsd")));
      }
    else {
      // basic panels
      modifyControls = (WSPanel)WSHelper.buildComponent(XMLReader.readString("<WSPanel code=\"SidePanel_DirectoryList_ModifyPanel_Main\" showBorder=\"true\" layout=\"BorderLayout\"><WSLabel code=\"SidePanel_DirectoryList_ModifyPanel_Basic\" wrap=\"true\" vertical-alignment=\"true\" height=\"80\" position=\"CENTER\" /></WSPanel>"));
      writeControls = (WSPanel)WSHelper.buildComponent(XMLReader.readString("<WSPanel code=\"SidePanel_DirectoryList_WritePanel_Main\" showBorder=\"true\" layout=\"BorderLayout\"><WSLabel code=\"SidePanel_DirectoryList_WritePanel_Basic\" wrap=\"true\" vertical-alignment=\"true\" height=\"80\" position=\"CENTER\" /></WSPanel>"));
      exportControls = (WSPanel)WSHelper.buildComponent(XMLReader.readString("<WSPanel code=\"SidePanel_DirectoryList_ExtractPanel_Main\" showBorder=\"true\" layout=\"BorderLayout\"><WSLabel code=\"SidePanel_DirectoryList_ExtractPanel_Basic\" wrap=\"true\" vertical-alignment=\"true\" height=\"80\" position=\"CENTER\" /></WSPanel>"));
      }

    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void loadDirList() {
    //dirHolder.loadPanel(Settings.get("DirectoryListView"));
    //dirHolder.setMultipleSelection(false);

    //dirHolder.revalidate();
    //dirHolder.repaint();
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void loadExportPlugins() {
    WSComboBox pluginList = (WSComboBox)WSRepository.get("SidePanel_DirectoryList_ExportPluginList");
    pluginList.setModel(new DefaultComboBoxModel(PluginListBuilder.getExportPluginList()));

    int selectedItem = TemporarySettings.getInt("CurrentSelectedExportPlugin");
    if (selectedItem != -1){
      pluginList.setSelectedIndex(selectedItem);
      }
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void loadPalettePlugins() {
    WSComboBox pluginList = (WSComboBox)WSRepository.get("SidePanel_DirectoryList_PalettePluginList");
    pluginList.setModel(new DefaultComboBoxModel(PluginListBuilder.getPalettePluginList()));
    pluginList.addItem(new PluginList(Language.get("AllFiles"),new AllFilesPlugin()));

    int selectedItem = TemporarySettings.getInt("CurrentSelectedPalettePlugin");
    if (selectedItem != -1){
      pluginList.setSelectedIndex(selectedItem);
      }
    else {
      pluginList.setSelectedIndex(pluginList.getItemCount()-1);
      }
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void loadReadPlugins() {
    WSComboBox pluginList = (WSComboBox)WSRepository.get("SidePanel_DirectoryList_ReadPluginList");
    pluginList.setModel(new DefaultComboBoxModel(PluginListBuilder.getPluginList()));
    pluginList.addItem(new PluginList(Language.get("AllFiles"),new AllFilesPlugin()));

    int selectedItem = TemporarySettings.getInt("CurrentSelectedReadPlugin");
    if (selectedItem != -1){
      pluginList.setSelectedIndex(selectedItem);
      }
    else {
      pluginList.setSelectedIndex(pluginList.getItemCount()-1);
      }
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void loadWritePlugins() {
    WSComboBox pluginList = (WSComboBox)WSRepository.get("SidePanel_DirectoryList_WritePluginList");
    pluginList.setModel(new DefaultComboBoxModel(PluginListBuilder.getWritePluginList()));

    int selectedItem = TemporarySettings.getInt("CurrentSelectedWritePlugin");
    if (selectedItem != -1){
      pluginList.setSelectedIndex(selectedItem);
      }
    }


/**
**********************************************************************************************
The event that is triggered from a WSClickableListener when a click occurs
@param c the component that triggered the event
@param e the event that occurred
**********************************************************************************************
**/
  public boolean onClick(JComponent c, MouseEvent e){
    if (c instanceof WSButton){
      String code = ((WSButton)c).getCode();

      if (code == null){
        return false;
        }

      // Control Panels
      else if (code.equals("SidePanel_DirectoryList_ReadPanelButton")){
        changeControls("ReadPanel",false);
        }
      else if (code.equals("SidePanel_DirectoryList_PalettePanelButton")){
        changeControls("PalettePanel",false);
        }
      else if (code.equals("SidePanel_DirectoryList_ModifyPanelButton")){
        changeControls("ModifyPanel",true);
        }
      else if (code.equals("SidePanel_DirectoryList_WritePanelButton")){
        changeControls("WritePanel",true);
        }
      else if (code.equals("SidePanel_DirectoryList_ExportPanelButton")){
        changeControls("ExportPanel",true);
        }

      // Buttons on the Control Panels
      else if (code.equals("SidePanel_DirectoryList_ReadArchiveButton")){
        readArchive();
        }
      else if (code.equals("SidePanel_DirectoryList_ReadPaletteButton")){
        readPalette();
        }
      else if (code.equals("SidePanel_DirectoryList_AddFileButton")){
        if (checkFullVersion()){
          addFiles();
          }
        }
      else if (code.equals("SidePanel_DirectoryList_RemoveFileButton")){
        if (checkFullVersion()){
          removeFiles();
          }
        }
      else if (code.equals("SidePanel_DirectoryList_WriteArchiveButton")){
        if (checkFullVersion()){
          writeArchive();
          }
        }
      else if (code.equals("SidePanel_DirectoryList_ExtractResourcesButton")){
        if (checkFullVersion()){
          exportSelectedFiles();
          }
        }
      else if (code.equals("SidePanel_DirectoryList_ExtractAllResourcesButton")){
        if (checkFullVersion()){
          exportAllFiles();
          }
        }

      else {
        return false;
        }

      // returns true even if not the full version,
      // because the click was still handled by this class.
      return true;

      }

    else if (c instanceof WSComponent){
      String code = ((WSComponent)c).getCode();

      if (code == null){
        return false;
        }

      if (code.equals("DirectoryList")){
        // directory list
        if (currentControl == writeControls){
          setWriteFilename(dirHolder.getSelectedFile());
          }
        else if (currentControl == exportControls){
          setExportFilename(dirHolder.getCurrentDirectory());
          }
        else {
          return false;
          }
        return true;
        }

      }

    return false;
    }


/**
**********************************************************************************************
Performs any functionality that needs to happen when the panel is to be closed. This method
does nothing by default, but can be overwritten to do anything else needed before the panel is
closed, such as garbage collecting and closing pointers to temporary objects.
**********************************************************************************************
**/
  public void onCloseRequest(){
    }


/**
**********************************************************************************************
The event that is triggered from a WSSelectableListener when an item is deselected
@param c the component that triggered the event
@param e the event that occurred
**********************************************************************************************
**/
  public boolean onDeselect(JComponent c, Object e){
    return false;
    }


/**
**********************************************************************************************
The event that is triggered from a WSDoubleClickableListener when a double click occurs
@param c the component that triggered the event
@param e the event that occurred
**********************************************************************************************
**/
  public boolean onDoubleClick(JComponent c, MouseEvent e){
    if (c instanceof WSComponent){
      String code = ((WSComponent)c).getCode();

      if (code == null){
        return false;
        }


      if (code.equals("DirectoryList")){
        // perform double click on the directory list

        File selected = dirHolder.getSelectedFile();

        // read
        if (currentControl == readControls){
          if (! selected.isDirectory()){
            if (Settings.getBoolean("DoubleClickAddsFiles")){
              addFilesDoubleClick(selected);
              }
            else {
              readArchive(selected);
              }
            }
          return true;
          }

        // read Palette
        else if (currentControl == paletteControls){
          if (! selected.isDirectory()){
            readPalette(selected);
            }
          return true;
          }

        // double click reads file by default (if setting is enabled)
        // it is here so that it is checked AFTER the script, thus allowing
        // double-clicks to be handled by the script panel if it is open
        else if (Settings.getBoolean("OpenArchiveOnDoubleClick")){
          if (! selected.isDirectory()){
            if (Settings.getBoolean("DoubleClickAddsFiles")){
              addFilesDoubleClick(selected);
              }
            else {
              readArchive(selected);
              }
            }
          return true;
          }

        // modify (add)
        else if (currentControl == modifyControls){
          if (checkFullVersion(false)){
            addFiles();
            }
          return true;
          }

        }

      }
    return false;
    }


/**
**********************************************************************************************
The event that is triggered from a WSEnterableListener when a key is pressed
@param c the component that triggered the event
@param e the event that occurred
**********************************************************************************************
**/
  public boolean onEnter(JComponent c, java.awt.event.KeyEvent e){
    if (c instanceof WSComponent){
      String code = ((WSComponent)c).getCode();


      if (code.equals("SidePanel_DirectoryList_WriteFilenameField")){
        if (checkFullVersion()){
          writeArchive();
          }
        }
      return true;
      }
    return false;
    }


/**
**********************************************************************************************
The event that is triggered from a WSKeyableListener when a key press occurs
@param c the component that triggered the event
@param e the event that occurred
**********************************************************************************************
**/
  public boolean onKeyPress(JComponent c, KeyEvent e){
    return false;
    }


/**
**********************************************************************************************
Performs any functionality that needs to happen when the panel is to be opened. By default,
it just calls checkLoaded(), but can be overwritten to do anything else needed before the
panel is displayed, such as resetting or refreshing values.
**********************************************************************************************
**/
  public void onOpenRequest(){
    String controlName = Settings.getString("SidePanel_DirectoryList_CurrentControl");
    boolean fullVersionOnly = true;
    if (controlName.equals("ReadPanel") || controlName.equals("PalettePanel")){
      fullVersionOnly = false;
      }
    changeControls(controlName,fullVersionOnly);

    dirHolder.checkFiles();
    dirHolder.scrollToSelected();
    }


/**
**********************************************************************************************
The event that is triggered from a WSSelectableListener when an item is selected
@param c the component that triggered the event
@param e the event that occurred
**********************************************************************************************
**/
  public boolean onSelect(JComponent c, Object e){
    if (c instanceof WSComboBox){
      String code = ((WSComboBox)c).getCode();

      if (code.equals("SidePanel_DirectoryList_ReadPluginList")){
        setPluginFilter(code,"CurrentSelectedReadPlugin");
        }
      else if (code.equals("SidePanel_DirectoryList_PalettePluginList")){
        setPluginFilter(code,"CurrentSelectedPalettePlugin");
        }
      else if (code.equals("SidePanel_DirectoryList_WritePluginList")){
        setPluginFilter(code,"CurrentSelectedWritePlugin",false);
        setWriteFilename();
        setFileFilter(null);
        }
      else if (code.equals("SidePanel_DirectoryList_ExportPluginList")){
        setPluginFilter(code,"CurrentSelectedExportPlugin",false);
        setExportFilename();
        setFileFilter(null);
        }

      }
    return false;
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void readArchive(){
    File selectedFile = dirHolder.getSelectedFile();
    readArchive(selectedFile);
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void readArchive(File selectedFile){
    WSComboBox readPlugins = (WSComboBox)WSRepository.get("SidePanel_DirectoryList_ReadPluginList");
    PluginList selectedItem = (PluginList)readPlugins.getSelectedItem();

    WSPlugin plugin = null;
    if (selectedItem != null){
      plugin = selectedItem.getPlugin();
      }
    else {
      plugin = new AllFilesPlugin();
      }

    if (plugin instanceof AllFilesPlugin){
      // auto-detect a plugin
      Task_ReadArchive task = new Task_ReadArchive(selectedFile);
      task.setDirection(UndoTask.DIRECTION_REDO);
      new Thread(task).start();
      }
    else {
      // open with the chosen plugin
      Task_ReadArchiveWithPlugin task = new Task_ReadArchiveWithPlugin(selectedFile,(ArchivePlugin)plugin);
      task.setDirection(UndoTask.DIRECTION_REDO);
      new Thread(task).start();
      }
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void readPalette(){
    File selectedFile = dirHolder.getSelectedFile();
    readPalette(selectedFile);
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void readPalette(File selectedFile){
    Resource[] resources = ((WSFileListPanelHolder)WSRepository.get("FileListPanelHolder")).getAllSelectedFiles();
    readPalette(selectedFile,resources);
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void readPalette(File selectedFile, Resource[] resources){
    WSComboBox readPlugins = (WSComboBox)WSRepository.get("SidePanel_DirectoryList_PalettePluginList");
    PluginList selectedItem = (PluginList)readPlugins.getSelectedItem();

    WSPlugin plugin = null;
    if (selectedItem != null){
      plugin = selectedItem.getPlugin();
      }
    else {
      plugin = new AllFilesPlugin();
      }

    if (plugin instanceof AllFilesPlugin){
      // auto-detect a plugin
      Task_ReadPalette task = new Task_ReadPalette(selectedFile, resources);
      task.setDirection(UndoTask.DIRECTION_REDO);
      new Thread(task).start();
      }
    else {
      // open with the chosen plugin
      Task_ReadPaletteWithPlugin task = new Task_ReadPaletteWithPlugin(selectedFile,(PalettePlugin)plugin, resources);
      task.setDirection(UndoTask.DIRECTION_REDO);
      new Thread(task).start();
      }
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void reloadDirectoryList() {
    dirHolder.reload();
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void removeFiles(){
    Resource[] selectedFiles = ((WSFileListPanelHolder)WSRepository.get("FileListPanelHolder")).getAllSelectedFiles();
    removeFiles(selectedFiles);
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void removeFiles(Resource[] selectedFiles){
    /*
    ArchivePlugin plugin = Archive.getReadPlugin();
    if (plugin != null){
      if (!plugin.canWrite()){
        WSPopup.showMessage("ModifyArchive_NotWritable",true);
        return;
        }
      }
    */

    Task_RemoveFiles task = new Task_RemoveFiles(selectedFiles);
    task.setDirection(UndoTask.DIRECTION_REDO);
    new Thread(task).start();
    UndoManager.add(task);
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void requestFocus(){
    dirHolder.requestFocus();
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void setExportFilename(){
    WSTextField field = (WSTextField)WSRepository.get("SidePanel_DirectoryList_ExportFilenameField");
    String filename = field.getText();

    if (filename == null || filename.length() <= 0){
      setExportFilename(dirHolder.getSelectedFile());
      }
    else {
      setExportFilename(filename);
      }
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void setExportFilename(File file){
    if (file == null){
      return;
      }
    setExportFilename(file.getAbsolutePath());
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void setExportFilename(String filename){
    WSTextField field = (WSTextField)WSRepository.get("SidePanel_DirectoryList_ExportFilenameField");

    if (field.getText().equals(filename)){
      return; // already the same
      }

    field.setText(filename);
    field.setCaretPosition(filename.length());
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void setFileFilter(FileFilter filter) {
    dirHolder.setMatchFilter(filter);
    dirHolder.reload();
    dirHolder.scrollToSelected();
    }


/**
**********************************************************************************************
sets the filter to the filter obtained from the WSComboBox with the given code name
**********************************************************************************************
**/
  public void setPluginFilter(String comboBoxCode, String settingCode) {
    setPluginFilter(comboBoxCode,settingCode,true);
    }


/**
**********************************************************************************************
sets the filter to the filter obtained from the WSComboBox with the given code name
**********************************************************************************************
**/
  public void setPluginFilter(String comboBoxCode, String settingCode, boolean setFilter) {

    WSComboBox combo = (WSComboBox)WSRepository.get(comboBoxCode);

    if (setFilter){
      PluginList list = (PluginList)combo.getSelectedItem();
      if (list == null || ! combo.isEnabled()){
        setFileFilter(null);
        }
      else {
        setFileFilter(new PluginFinderMatchFileFilter(list.getPlugin()));
        }
      }

    TemporarySettings.set(settingCode,combo.getSelectedIndex());

    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void setWriteFilename(){
    WSTextField field = (WSTextField)WSRepository.get("SidePanel_DirectoryList_WriteFilenameField");
    String filename = field.getText();

    if (filename == null || filename.length() <= 0){
      setWriteFilename(dirHolder.getSelectedFile());
      }
    else {
      setWriteFilename(filename);
      }
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void setWriteFilename(File file){
    if (file == null){
      return;
      }
    setWriteFilename(file.getAbsolutePath());
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void setWriteFilename(String filename){
    WSTextField field = (WSTextField)WSRepository.get("SidePanel_DirectoryList_WriteFilenameField");

    int dotPos = filename.lastIndexOf(".");
    int slashPos = filename.lastIndexOf("\\");
    if (slashPos < 0){
      slashPos = filename.lastIndexOf("/");
      }

    if (dotPos > 0 && dotPos > slashPos){
      filename = filename.substring(0,dotPos);
      }
    else {
      filename = "newArchive";
      }


    String extension = "unk";


    WSComboBox convertPlugins = (WSComboBox)WSRepository.get("SidePanel_DirectoryList_WritePluginList");
    Object pluginObj = convertPlugins.getSelectedItem();
    if (pluginObj == null || !(pluginObj instanceof PluginList)){
      return;
      }
    ArchivePlugin plugin = ((PluginList)pluginObj).getPlugin();


    if (plugin != null){
      extension = plugin.getExtension(0);
      if (extension == null || extension.length() == 0 || extension.equals("*")){
        extension = "unk";
        }
      }

    filename += "." + extension;

    if (field.getText().equals(filename)){
      return; // already the same
      }

    field.setText(filename);
    field.setCaretPosition(filename.length());
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void writeArchive(){
    WSComboBox convertPlugins = (WSComboBox)WSRepository.get("SidePanel_DirectoryList_WritePluginList");
    Object pluginObj = convertPlugins.getSelectedItem();
    if (pluginObj == null || !(pluginObj instanceof PluginList)){
      return;
      }
    ArchivePlugin plugin = ((PluginList)pluginObj).getPlugin();


    String dirName = dirHolder.getCurrentDirectory().getAbsolutePath();
    String filename = ((WSTextField)WSRepository.get("SidePanel_DirectoryList_WriteFilenameField")).getText();

    if (filename == null || filename.equals("")){
      WSPopup.showError("WriteArchive_FilenameMissing",true);
      return;
      }

    // append the default extension, if no extension exists
    if (FileManipulator.getExtension(filename).equals("")){
      filename += "." + plugin.getExtension(0);
      }

    if (filename.indexOf(dirName) >= 0){
      }
    else {
      filename = dirName + File.separator + filename;
      }

    File outputPath = new File(filename);

    writeArchive(outputPath,plugin);
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void writeArchive(File outputPath, ArchivePlugin plugin){
    Task_WriteArchive task = new Task_WriteArchive(outputPath,plugin);
    task.setDirection(UndoTask.DIRECTION_REDO);
    new Thread(task).start();
    }



///////////////
//
// Default Implementations
//
///////////////


/**
**********************************************************************************************
Gets the plugin description
**********************************************************************************************
**/
  public String getDescription(){
    String description = toString() + "\n\n" + Language.get("Description_SidePanel");

    if (! isEnabled()){
      description += "\n\n" + Language.get("Description_PluginDisabled");
      }
    else {
      description += "\n\n" + Language.get("Description_PluginEnabled");
      }

    return description;
    }


/**
**********************************************************************************************
Gets the plugin name
**********************************************************************************************
**/
  public String getText(){
    return super.getText();
    }


/**
**********************************************************************************************
The event that is triggered from a WSHoverableListener when the mouse moves over an object
@param c the component that triggered the event
@param e the event that occurred
**********************************************************************************************
**/
  public boolean onHover(JComponent c, MouseEvent e){
    return super.onHover(c,e);
    }


/**
**********************************************************************************************
The event that is triggered from a WSHoverableListener when the mouse moves out of an object
@param c the component that triggered the event
@param e the event that occurred
**********************************************************************************************
**/
  public boolean onHoverOut(JComponent c, MouseEvent e){
    return super.onHoverOut(c,e);
    }


/**
**********************************************************************************************
Sets the description of the plugin
@param description the description
**********************************************************************************************
**/
  public void setDescription(String description){
    super.setDescription(description);
    }



  }