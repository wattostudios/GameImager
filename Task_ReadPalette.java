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
public class Task_ReadPalette implements UndoTask {

  /** The direction to perform in the thread **/
  int direction = 1;

  File path;
  Resource[] resources;


/**
**********************************************************************************************

**********************************************************************************************
**/
  public Task_ReadPalette(File path, Resource[] resources){
    this.path = path;
    this.resources = resources;
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void redo(){
    if (!TaskManager.canDoTask()){
      return;
      }


    // Progress dialog
    WSProgressDialog progress = WSProgressDialog.getInstance();
    progress.show(1,0,Language.get("Progress_ReadingPalette"));


    TaskManager.startTask();


      boolean showErrors = !Settings.getBoolean("OpenArchiveOnDoubleClick");

      Palette palette = PaletteReader.read(path,showErrors);
      if (palette == null){
        //WSPopup.showError("ReadPalette_NoPluginsFound",true);
        // commented out, because this overwrites the selected file instead of adding to the collection
        //TaskManager.stopTask();
        //Task_ReadArchive task = new Task_ReadArchive(path);
        //task.redo();

        // Try to read the file as an image instead
        if (Settings.getBoolean("OpenArchiveOnDoubleClick")){
          if (Settings.getBoolean("DoubleClickAddsFiles")){
            Task_AddFiles task = new Task_AddFiles(new File[]{path});
            task.setDirection(UndoTask.DIRECTION_REDO);
            new Thread(task).start();
            }
          else {
            Task_ReadArchive task = new Task_ReadArchive(path);
            task.setDirection(UndoTask.DIRECTION_REDO);
            new Thread(task).start();
            }
          }

        }
      else {
        Settings.set("CurrentPalette",path.getAbsolutePath());

        if (Settings.getBoolean("LoadPaletteReplacesCurrent") && resources.length > 0){
          for (int i=0;i<resources.length;i++){
            resources[i].setPalette(palette);
            }

          FileListPanel fileList = (FileListPanel)((WSFileListPanelHolder)WSRepository.get("FileListPanelHolder")).getCurrentPanel();
          fileList.reload();
          }
        else {
          PaletteManager.addPalette(palette);
          }

        if (!showErrors){
          WSPopup.showMessage("ReadPalette_ReadSucceeded",true);
          }

        /*
        WSSidePanelHolder sidePanelHolder = (WSSidePanelHolder)WSRepository.get("SidePanelHolder");
        if (! sidePanelHolder.getCurrentPanelCode().equals("SidePanel_DirectoryList")){
          sidePanelHolder.reloadPanel();
          }
        */
        }


    TaskManager.stopTask();

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

