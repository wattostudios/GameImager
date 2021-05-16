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
import org.watto.manipulator.*;

import java.io.*;
import java.util.Hashtable;
import javax.swing.*;
import javax.swing.filechooser.*;

/**
**********************************************************************************************
The ArchivePlugin is one of the most important classes in Game Extractor. A class that extends
from ArchivePlugin is able to read, and possibly write, a particular format of archive.
<br><br>
This class contains many methods and globals that make it easy to write an extending class,
such as methods for reading and writing using different inputs, methods to control the data
displayed by the FileTablePanels and by the FileTableSorter, and globals for verification of
fields when reading an archive.
<br><br>
It also contains methods to allow automatic replacing support with only slight alteration to
the code in your plugin. Methods to assist Game Extractor in the automatic detection of a
compatable read plugin are also supplied.
**********************************************************************************************
**/
public abstract class ArchivePlugin extends WSObjectPlugin {

  /** Can this plugin read an archive? **/
  boolean canRead = true;
  /** Can this plugin write an archive? **/
  boolean canWrite = false;
  /** Does this plugin allow multiple images (ie is it a collection)? **/
  boolean isCollection = false;

  /** The games that use this archive format **/
  String[] games = new String[]{""};
  /** The default extension of archives complying to this format **/
  String[] extensions = new String[]{""};
  /** The platforms that this archive exists on (such as "PC", "XBox", or "PS2") **/
  String[] platforms = new String[]{""};


  /** quick access to the field validator **/
  static FieldValidator check = new FieldValidator();


/**
**********************************************************************************************
Constructor
**********************************************************************************************
**/
  public ArchivePlugin() {
    setCode("ArchivePlugin");
    setName("Archive Plugin");
    }


/**
**********************************************************************************************
Constructor
**********************************************************************************************
**/
  public ArchivePlugin(String code) {
    setCode(code);
    setName(code);
    }


/**
**********************************************************************************************
Constructor
**********************************************************************************************
**/
  public ArchivePlugin(String code, String name) {
    setCode(code);
    setName(name);
    }


/**
**********************************************************************************************
Can this plugin read an archive?
@return true if the plugin can read, false if it cannot.
**********************************************************************************************
**/
  public boolean canRead() {
    return canRead;
    }


/**
**********************************************************************************************
Can this plugin write archives?
@return true is the plugin can write archives, false if it cannot.
**********************************************************************************************
**/
  public boolean canWrite() {
    return canWrite;
    }


/**
**********************************************************************************************
Gets a list of the allowed functions
@return the list
**********************************************************************************************
**/
  public String getAllowedFunctionsList() {
    String list = "";

    if (canRead){
      list += Language.get("Description_ReadOperation");
      }
    if (canWrite){
      if (list.length() > 0){
        list += ", ";
        }
      list += Language.get("Description_WriteOperation");
      }

    return list;
    }


/**
**********************************************************************************************
Gets the description of the plugin, such as the games and platforms that are supported, and the
functions that can be performed.
@return the description of this plugin
**********************************************************************************************
**/
  public String getDescription(){

    String description = toString() + "\n\n" + Language.get("Description_ArchivePlugin");


    if (games.length <= 0){
      description += "\n\n" + Language.get("Description_NoDefaultGames");
      }
    else {
      description += "\n\n" + Language.get("Description_DefaultGames");

      for (int i=0;i<games.length;i++){
        description += "\n -" + games[i];
        }

      }


    if (platforms.length <= 0){
      description += "\n\n" + Language.get("Description_NoDefaultPlatforms");
      }
    else {
      description += "\n\n" + Language.get("Description_DefaultPlatforms");

      for (int i=0;i<platforms.length;i++){
        description += "\n -" + platforms[i];
        }

      }


    if (extensions.length <= 0 || extensions[0].length() == 0){
      description += "\n\n" + Language.get("Description_NoDefaultExtensions");
      }
    else {
      description += "\n\n" + Language.get("Description_DefaultExtensions") + "\n";

      for (int i=0;i<extensions.length;i++){
        if (i > 0){
          description += " *." + extensions[i];
          }
        else {
          description += "*." + extensions[i];
          }
        }

      }


    description += "\n\n" + Language.get("Description_SupportedOperations");
    if (canRead){
      description += "\n - " + Language.get("Description_ReadOperation");
      }
    if (canWrite){
      description += "\n - " + Language.get("Description_WriteOperation");
      }


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
Gets a file with the same name, but different extension, to the <i>source</i>
@param source the source file to use as the name and diretory base
@param extension the new extension
@return the file with the same name, different extension
@throws WSPluginException if the file does not exist.
**********************************************************************************************
**/
  public static File getDirectoryFile(File source, String extension) throws WSPluginException {
    return getDirectoryFile(source,extension,true);
    }


/**
**********************************************************************************************
Gets a file with the same name, but different extension, to the <i>source</i>
@param source the source file to use as the name and diretory base
@param extension the new extension
@return the file with the same name, different extension
@throws WSPluginException if the file does not exist.
**********************************************************************************************
**/
  public static File getDirectoryFile(File source, String extension, boolean checkExists) throws WSPluginException {
    String pathName = source.getPath();
    int dotPos = pathName.lastIndexOf(".");
    if (dotPos < 0){
      throw new WSPluginException("Missing Directory File");
      }

    File path = new File(pathName.substring(0,dotPos) + "." + extension);
    if (checkExists && ! path.exists()){
      throw new WSPluginException("Missing Directory File");
      }

    return path;
    }


/**
**********************************************************************************************
Gets the extension at position <i>num</i> of the array
@param num the extension number
@return the extension
**********************************************************************************************
**/
  public String getExtension(int num) {
    if (num < extensions.length){
      return extensions[num];
      }
    else {
      return "unk";
      }
    }


/**
**********************************************************************************************
Gets all the extensions
@return the extensions
**********************************************************************************************
**/
  public String[] getExtensions() {
    return extensions;
    }


/**
**********************************************************************************************
Gets a list of the extensions
@return the list
**********************************************************************************************
**/
  public String getExtensionsList() {
    String list = "";

    for (int i=0;i<extensions.length;i++){
      if (i > 0){
        list += ", ";
        }
      list += "*." + extensions[i];
      }

    return list;
    }


/**
**********************************************************************************************
Gets all the games
@return the games
**********************************************************************************************
**/
  public String[] getGames() {
    return games;
    }


/**
**********************************************************************************************
Gets a list of the games
@return the list
**********************************************************************************************
**/
  public String getGamesList() {
    String list = "";

    for (int i=0;i<games.length;i++){
      if (i > 0){
        list += ", ";
        }
      list += games[i];
      }

    return list;
    }


/**
**********************************************************************************************
Gets the percentage chance that this plugin can read the <i>file</i>
@param file the file to analyse
@return the percentage (0-100) chance
**********************************************************************************************
**/
  public int getMatchRating(File file){
    try {
      FileManipulator fm = new FileManipulator(file,"r");
      int rating = getMatchRating(fm);
      fm.close();
      return rating;
      }
    catch (Throwable t){
      return 0;
      }
    }


/**
**********************************************************************************************
Gets the percentage chance that this plugin can read the file <i>fm</i>
@param fm the file to analyse
@return the percentage (0-100) chance
**********************************************************************************************
**/
  public abstract int getMatchRating(FileManipulator fm);


/**
**********************************************************************************************

**********************************************************************************************
**/
  public String[] getPlatforms() {
    return platforms;
    }


/**
**********************************************************************************************
Gets a list of the platforms
@return the list
**********************************************************************************************
**/
  public String getPlatformsList() {
    String list = "";

    for (int i=0;i<platforms.length;i++){
      if (i > 0){
        list += ", ";
        }
      list += platforms[i];
      }

    return list;
    }


/**
**********************************************************************************************
Does this plugin support multiple images (ie is it a collection)
@return true is the plugin is a collection
**********************************************************************************************
**/
  public boolean isCollection() {
    return isCollection;
    }


/**
**********************************************************************************************
Reads the archive <i>source</i>
@param source the archive file
@return the resources in the archive
**********************************************************************************************
**/
  public abstract Resource[] read(File source);


/**
**********************************************************************************************
Resizes the <i>resources</i> array to the new size, where <i>numResources</i> MUST be smaller
than the current array length.
@param resources the array to resize
@param numResources the new size of the array
@return the resized array
**********************************************************************************************
**/
  public static Resource[] resizeResources(Resource[] resources, int numResources) {
    Resource[] temp = resources;
    resources = new Resource[numResources];
    System.arraycopy(temp,0,resources,0,numResources);
    return resources;
    }


/**
**********************************************************************************************
Sets whether this plugin can read archives or not
@param canRead is reading allowed?
**********************************************************************************************
**/
  public void setCanRead(boolean canRead) {
    this.canRead = canRead;
    }


/**
**********************************************************************************************
Sets whether this plugin can write archives
@param canWrite is writing allowed?
**********************************************************************************************
**/
  public void setCanWrite(boolean canWrite) {
    this.canWrite = canWrite;
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void setExtensions(String ... extensions) {
    this.extensions = extensions;
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void setGames(String ... games) {
    this.games = games;
    }


/**
**********************************************************************************************
Sets whether this plugin supports multiple images (ie is it a collection?)
@param isCollection is this a collection?
**********************************************************************************************
**/
  public void setIsCollection(boolean isCollection) {
    this.isCollection = isCollection;
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void setPlatforms(String ... platforms) {
    this.platforms = platforms;
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void setProperties(boolean canRead, boolean canWrite) {
    this.canRead = canRead;
    this.canWrite = canWrite;
    }


/**
**********************************************************************************************
Writes the <i>resources</i> to the archive <i>destination</i>, where the archive was constructed
from scratch (as opposed to replace() which writes an archive that was already opened). If
<i>allowImplicitReplacing</i> is enabled, it will write the archive without the need for
overwriting this method.
@param resources the files to write
@param destination the place to store the archive
**********************************************************************************************
**/
  public void write(Resource[] resources, File destination) {
    }


/**
**********************************************************************************************
Records the error/exception stack trace in the log file. If debug is enabled, it will also
write the error to the <i>System.out</i> command prompt
@param t the <i>Throwable</i> error/exception
**********************************************************************************************
**/
  public static void logError(Throwable t){
    ErrorLogger.log(t);
    }


  }