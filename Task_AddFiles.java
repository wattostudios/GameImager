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
public class Task_AddFiles implements UndoTask {

  /** The direction to perform in the thread **/
  int direction = 1;

  File[] files;


/**
**********************************************************************************************

**********************************************************************************************
**/
  public Task_AddFiles(File[] files){
    this.files = files;
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void redo(){
    if (!TaskManager.canDoTask()){
      return;
      }

    if (files == null || files.length <= 0){
      WSPopup.showError("AddFiles_NoFilesToAdd",true);
      return;
      }


    // Progress dialog
    WSProgressDialog progress = WSProgressDialog.getInstance();
    progress.show(1,0,Language.get("Progress_AddingFiles"));
    progress.setIndeterminate(true);


    TaskManager.startTask();


      int numFiles = Archive.getNumFiles();

      for (int i=0;i<files.length;i++){
        Resource[] resources = ResourceReader.read(files[i],false);
        if (resources == null || resources.length == 0){
          }
        else {
          Archive.addResources(resources);
          Archive.setReadPlugin(ResourceReader.getUsedPlugin());

          FileListPanel fileList = (FileListPanel)((WSFileListPanelHolder)WSRepository.get("FileListPanelHolder")).getCurrentPanel();
          fileList.setCurrentImage(numFiles);
          fileList.reload();
          fileList.selectResource(numFiles);

          WSSidePanelHolder sidePanelHolder = (WSSidePanelHolder)WSRepository.get("SidePanelHolder");
          if (! sidePanelHolder.getCurrentPanelCode().equals("SidePanel_DirectoryList")){
            sidePanelHolder.reloadPanel();
            }

          }
        }


    TaskManager.stopTask();


    ChangeMonitor.set(true);
    WSPopup.showMessage("AddFiles_FilesAdded",true);

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

    return Language.get(name).replace("&number&",""+files.length);
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

