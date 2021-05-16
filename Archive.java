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
import org.watto.manipulator.FileBuffer;

import java.io.*;
import javax.swing.*;
import javax.swing.filechooser.*;

/**
**********************************************************************************************
The Archive class manages details about the loaded archive, such as the <i>resources</i>, the
<i>columns</i> to be shown in the current FileListPanel, and the plugin used to read the
archive. It also contains methods for the management and manipulation of Resources, such as
adding and removing resources.
<br><br>
This class is entirely static. You will need to call the constructor once in order to set up a
few globals and such, but from this point onwards you would simply call the methods directly
such as by "Archive.runMethod()".
**********************************************************************************************
**/
public class Archive{

  /** The resources stored in this archive **/
  static Resource[] resources = new Resource[0];

  /** The plugin used to read the <i>basePath</i> archive **/
  static ArchivePlugin readPlugin = new AllFilesPlugin();


/**
**********************************************************************************************
Constructor. Should only be called once.
**********************************************************************************************
**/
  public Archive() {
    }


/**
**********************************************************************************************
Adds all files in the <i>directory</i> to the archive. If there are any sub-directories, they
are also analysed and added.
@param directory the directory that contains the files to add
@param directoryName the prefix name to use for the files in the archive, rather than using the
                     absolute directory path.
**********************************************************************************************
**/
  public static void addDirectory(File directory) {
    File[] files = directory.listFiles();
    addFiles(files);
    }


/**
**********************************************************************************************
Adds the <i>files</i> to the archive. If any of the files are a directory, the contents of the
directory are added via addDirectory(File,String).
@param files the files to add to the archive.
**********************************************************************************************
**/
  public static void addFiles(File[] files) {
    try {

      int numFiles = resources.length;
      int newNumFiles = numFiles + files.length;
      resizeResources(newNumFiles);
      for (int i=0;i<files.length;i++){
        if (files[i].isDirectory()){
          addDirectory(files[i]);
          }
        else {
          Resource[] newResources = ResourceReader.read(files[i],false);
          if (newResources == null || newResources.length == 0){
            continue;
            }
          else if (newResources.length == 1){
            numFiles++;
            resources[numFiles] = newResources[0];
            }
          else {
            newNumFiles += newResources.length;
            resizeResources(newNumFiles);

            System.arraycopy(newResources,0,resources,numFiles,newResources.length);
            numFiles += newResources.length;
            }
          }
        }


      // when a directory is added, it doesn't appear as a file in the list,
      // rather the contents of the directory are added instead. This call
      // will ensure that any nulls created by adding a directory will be
      // removed from the array.
      removeNullResources();

      }
    catch (Throwable t){
      logError(t);
      }
    }


/**
**********************************************************************************************
Adds a resource to the archive
@param file the resource to add
**********************************************************************************************
**/
  public static void addResource(Resource file) {
    int numResources = resources.length;
    resizeResources(numResources + 1);
    resources[numResources] = file;
    }


/**
**********************************************************************************************
Adds a number of files to the archive
@param files the files to add
**********************************************************************************************
**/
  public static void addResources(Resource[] files) {

    int numFiles = resources.length;
    int newNumFiles = numFiles + files.length;
    resizeResources(newNumFiles);

    for (int i=numFiles,j=0;i<newNumFiles && j<files.length;i++,j++){
      resources[i] = files[j];
      }

    }


/**
**********************************************************************************************
Gets the name of the archive
@return the name of the opened archive, or "newArchive" if the archive was started from scratch
**********************************************************************************************
**/
  public static String getArchiveName() {
    return "newArchive";
    }


/**
**********************************************************************************************
Gets the maximum number of files, from setting "MaxNumberOfFiles4"
@return the maximum number of files
**********************************************************************************************
**/
  public static int getMaxFiles() {
    return getMaxFiles(4);
    }


/**
**********************************************************************************************
Gets the maximum number of files for a field with <i>size</i> number of bytes
@param size the number of bytes assigned to the NumberOfFiles field of an archive
@return the maximum number of files
**********************************************************************************************
**/
  public static int getMaxFiles(int size) {
    if (size == 2){
      return Settings.getInt("MaxNumberOfFiles2");
      }
    else {
      return Settings.getInt("MaxNumberOfFiles4");
      }
    }


/**
**********************************************************************************************
Gets the number of resources in the archive
@return the number of files
**********************************************************************************************
**/
  public static int getNumFiles() {
    if (resources == null){
      return 0;
      }
    return resources.length;
    }


/**
**********************************************************************************************
Gets the plugin used to read the archive
@return the plugin
**********************************************************************************************
**/
  public static ArchivePlugin getReadPlugin() {
    return readPlugin;
    }


/**
**********************************************************************************************
Gets the resource from index <i>num</i> of the array
@param num the resource number
@return the resource.
**********************************************************************************************
**/
  public static Resource getResource(int num) {
    if (num < 0 || num >= resources.length){
      return null;
      }
    return resources[num];
    }


/**
**********************************************************************************************
Gets all the resources in the archive
@return the resources
**********************************************************************************************
**/
  public static Resource[] getResources() {
    return resources;
    }


/**
**********************************************************************************************
Gets <i>numOfResources</i>, starting from the <i>startResource</i>
@return the resources
**********************************************************************************************
**/
  public static Resource[] getResources(int startResource, int numOfResources) {
    Resource[] range = new Resource[numOfResources];
    System.arraycopy(resources,startResource,range,0,numOfResources);
    return range;
    }


/**
**********************************************************************************************
Makes a new archive. Effectively resets the globals to their initial values. If there is an
archive already opened, and if the archive has been modified, it will ask the user to save first.
**********************************************************************************************
**/
  public static void makeNewArchive() {
    resources = new Resource[0];
    readPlugin = new AllFilesPlugin();

    try {
      new FullVersionVerifier();
      GameImager.getInstance().setTitle(Language.get("ProgramName") + " " + Settings.get("Version") + " - http://www.watto.org");
      }
    catch (Throwable t){
      GameImager.getInstance().setTitle(Language.get("ProgramName_Free") + " " + Settings.get("Version") + " - http://www.watto.org");
      }
    }


/**
**********************************************************************************************
Removes all resources from the archive
**********************************************************************************************
**/
  public static void removeAllResources() {
    resources = new Resource[0];
    }


/**
**********************************************************************************************
Removes all null resources from the array, which may be caused when removing files in batch, or
when adding directories of files.
**********************************************************************************************
**/
  public static void removeNullResources() {
    try {

      // find the 2 pointers
      int nullPos = -1;
      int nextFile = -1;
      for (int i=0;i<resources.length;i++){
        if (nullPos == -1 && resources[i] == null){
          nullPos = i;
          }
        if (nullPos > -1 && resources[i] != null){
          nextFile = i;
          i = resources.length;
          }
        }

      // re-shuffle the resource array to the top
      if (nullPos > -1 && nextFile > -1){
        for (int i=nextFile;i<resources.length;i++){
          if (resources[i] != null){
            resources[nullPos] = resources[i];
            nullPos ++;
            }
          }
        }


      // resize the resources array
      if (nullPos > -1){
        resizeResources(nullPos);
        }

      }
    catch (Throwable t){
      logError(t);
      }
    }


/**
**********************************************************************************************
Removes the resource <i>num</i> from the archive
@param num the resource to remove
**********************************************************************************************
**/
  public static void removeResource(int num) {
    removeResource(resources[num]);
    }


/**
**********************************************************************************************
Removes the <i>file</i> from the archive
@param file the resource to remove
**********************************************************************************************
**/
  public static void removeResource(Resource file) {
    removeResources(new Resource[]{file});
    }


/**
**********************************************************************************************
Removes the <i>files</i> from the archive
@param files the resources to remove.
**********************************************************************************************
**/
  public static void removeResources(Resource[] files) {
    try {

      // null out the resources to remove
      int filesPos = 0;
      while (filesPos < files.length){
        for (int i=0;i<resources.length;i++){
          if (files[filesPos] == resources[i]){
            resources[i] = null;
            filesPos++;
            if (filesPos >= files.length){
              i = resources.length;
              }
            }
          }
        }

      removeNullResources();

      }
    catch (Throwable t){
      logError(t);
      }
    }


/**
**********************************************************************************************
Changes the size of the <i>resources</i> array
@param numResources the new size of the array
**********************************************************************************************
**/
  public static void resizeResources(int numResources) {
    Resource[] temp = resources;
    resources = new Resource[numResources];

    if (numResources < temp.length){
      System.arraycopy(temp,0,resources,0,numResources);
      }
    else {
      System.arraycopy(temp,0,resources,0,temp.length);
      }
    }


/**
**********************************************************************************************
Sets the number of resources in the archive. Used for undo() in Task_AddFiles();
@param numFiles the new number of files
**********************************************************************************************
**/
  public static void setNumFiles(int numFiles) {
    resizeResources(numFiles);
    }


/**
**********************************************************************************************
Sets the plugin used to read the archive
@param pluginNew the new plugin
**********************************************************************************************
**/
  public static void setReadPlugin(ArchivePlugin pluginNew) {
    readPlugin = pluginNew;
    }


/**
**********************************************************************************************
Sets the resources in the archive
@param resourcesNew the new resources
**********************************************************************************************
**/
  public static void setResources(Resource[] resourcesNew) {
    resources = resourcesNew;
    }


/**
**********************************************************************************************
Records an error to the log file
@param t the error that occurred.
**********************************************************************************************
**/
  public static void logError(Throwable t) {
    ErrorLogger.log(t);
    }


  }