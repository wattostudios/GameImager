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
public class Task_ExportFiles implements UndoTask {

  /** The direction to perform in the thread **/
  int direction = 1;

  File path;
  ArchivePlugin plugin;
  Resource[] resources;

  int imageNumber = 0;


/**
**********************************************************************************************

**********************************************************************************************
**/
  public Task_ExportFiles(Resource[] resources, File path, ArchivePlugin plugin){
    this.path = path;
    this.plugin = plugin;
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

    if (resources == null || resources.length <= 0){
      return;
      }


    // Progress dialog
    WSProgressDialog progress = WSProgressDialog.getInstance();
    progress.show(1,0,Language.get("Progress_WritingArchive"));
    progress.setIndeterminate(true);

    TaskManager.startTask();


      String extension = plugin.getExtension(0);
      if (extension == null || extension.length() <= 0){
        extension = "unk";
        }

      String parentDirectory = path.getAbsolutePath();
      imageNumber = 0; // reset the first image number to 0

      if (plugin != null){
        // write from scratch
        for (int i=0;i<resources.length;i++){
          //File filePath = getImageFilePath(parentDirectory,extension);
          File filePath = changeExtension(parentDirectory,resources[i].getSource(),extension);
          plugin.write(new Resource[]{resources[i]},filePath);
          }
        }

      Settings.addRecentFile(path);

      ((WSDirectoryListHolder)WSRepository.get("SidePanel_DirectoryList_DirectoryListHolder")).reload();


    TaskManager.stopTask();


    ChangeMonitor.set(false);
    WSPopup.showMessage("ExportFiles_ArchiveSaved",true);

    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public File changeExtension(String parent, File sourcePath, String extension){
    String name = sourcePath.getName();
    int dotPos = name.lastIndexOf(".");
    if (dotPos > 0){
      name = name.substring(0,dotPos+1) + extension;
      }
    else {
      name = name + "." + extension;
      }

    return new File(parent + File.separator + name);
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public File getImageFilePath(String parent, String extension){

    String name;
    boolean found = false;

    while (!found){
      imageNumber++;

      if (imageNumber < 10){
        name = "Image 00000" + imageNumber;
        }
      else if (imageNumber < 100){
        name = "Image 0000" + imageNumber;
        }
      else if (imageNumber < 1000){
        name = "Image 000" + imageNumber;
        }
      else if (imageNumber < 10000){
        name = "Image 00" + imageNumber;
        }
      else if (imageNumber < 100000){
        name = "Image 0" + imageNumber;
        }
      else {
        name = "Image " + imageNumber;
        }

      File filePath = new File(parent + File.separator + name + "." + extension);
      if (!filePath.exists()){
        return filePath;
        }
      }

    return path;
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

