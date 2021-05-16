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
import org.watto.component.WSPopup;
import org.watto.component.WSRepository;
import org.watto.component.WSProgressDialog;

import java.io.File;

/**
**********************************************************************************************

**********************************************************************************************
**/
public class Task_ReadArchive implements UndoTask {

  /** The direction to perform in the thread **/
  int direction = 1;

  File path;


/**
**********************************************************************************************

**********************************************************************************************
**/
  public Task_ReadArchive(File path){
    this.path = path;
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void redo(){
    if (!TaskManager.canDoTask()){
      return;
      }

    // ask to save the modified archive
    if (GameImager.getInstance().promptToSave()){
      return;
      }
    ChangeMonitor.set(false);


    // Progress dialog
    WSProgressDialog progress = WSProgressDialog.getInstance();
    progress.show(1,0,Language.get("Progress_ReadingArchive"));


    TaskManager.startTask();


      Resource[] resources = ResourceReader.read(path,true);
      if (resources == null || resources.length == 0){
        WSPopup.showError("ReadArchive_NoPluginsFound",true);
        }
      else {
        Settings.set("CurrentArchive",path.getAbsolutePath());
        Settings.addRecentFile(path);

        Archive.makeNewArchive();
        Archive.addResources(resources);
        Archive.setReadPlugin(ResourceReader.getUsedPlugin());

        FileListPanel fileList = (FileListPanel)((WSFileListPanelHolder)WSRepository.get("FileListPanelHolder")).getCurrentPanel();
        fileList.setCurrentImage(0);
        fileList.reload();

        WSSidePanelHolder sidePanelHolder = (WSSidePanelHolder)WSRepository.get("SidePanelHolder");
        if (! sidePanelHolder.getCurrentPanelCode().equals("SidePanel_DirectoryList")){
          sidePanelHolder.reloadPanel();
          }
        }


    TaskManager.stopTask();


    // clear out the undo/redo
    UndoManager.clear();

    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void run(){
    if (direction == -1){
      undo();
      }
    else if (direction == 1){
      redo();
      }
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void setDirection(int direction){
    this.direction = direction;
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public String toString(){
    Class cl = getClass();
    String name = cl.getName();
    Package pack = cl.getPackage();

    if (pack != null){
      name = name.substring(pack.getName().length()+1);
      }

    return Language.get(name);
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void undo(){
    if (!TaskManager.canDoTask()){
      return;
      }
    }


  }

