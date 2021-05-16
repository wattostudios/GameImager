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

import java.io.File;

/**
**********************************************************************************************

**********************************************************************************************
**/
public class ResourceReader {

  /** The plugin used to read the archive **/
  static ArchivePlugin usedPlugin;


/**
**********************************************************************************************

**********************************************************************************************
**/
  public ResourceReader(){
    }


/**
**********************************************************************************************
Gets the plugins that could read this file, sorted by rating.
**********************************************************************************************
**/
  public static RatedPlugin[] getRatedPlugins(File path){
    RatedPlugin[] plugins = PluginFinder.findPlugins(path,ArchivePlugin.class);
    if (plugins == null || plugins.length == 0){
      return null;
      }
    java.util.Arrays.sort(plugins);
    return plugins;
    }


/**
**********************************************************************************************
Gets the plugin that successfully read the file
**********************************************************************************************
**/
  public static ArchivePlugin getUsedPlugin(){
    return usedPlugin;
    }


/**
**********************************************************************************************
Read trying all plugins.
**********************************************************************************************
**/
  public static Resource[] read(File path, boolean showErrors){
    // error checking
    if (path == null || !path.exists()){
      if (showErrors){
        WSPopup.showError("ReadArchive_FileDoesNotExist",true);
        }
      return null;
      }

    if (path.isDirectory()){
      if (showErrors){
        WSPopup.showError("ReadArchive_FileNotAnArchive",true);
        }
      return null;
      }

    // get plugins
    RatedPlugin[] plugins = getRatedPlugins(path);
    if (plugins == null || plugins.length == 0){
      WSPopup.showError("ReadArchive_NoPluginsFound",true);
      return null;
      }

    // read the file
    Resource[] resources = null;

    for (int i=0;i<plugins.length;i++){
      resources = readWithPlugin(path,(ArchivePlugin)plugins[i].getPlugin());
      if (resources != null && resources.length != 0){
        break;
        }
      }

    if (showErrors){
      if (resources == null){
        WSPopup.showError("ReadArchive_ReadFailed",true);
        }
      else {
        WSPopup.showError("ReadArchive_ReadSucceeded",true);
        }
      }

    return resources;
    }



/**
**********************************************************************************************
Read using a specific plugin.
**********************************************************************************************
**/
  public static Resource[] read(File path, ArchivePlugin plugin, boolean showErrors){
    // error checking
    if (path == null || !path.exists()){
      if (showErrors){
        WSPopup.showError("ReadArchive_FileDoesNotExist",true);
        }
      return null;
      }

    if (path.isDirectory()){
      if (showErrors){
        WSPopup.showError("ReadArchive_FileNotAnArchive",true);
        }
      return null;
      }

    if (plugin == null){
      return null;
      }

    // read the file
    Resource[] resources = readWithPlugin(path,plugin);

    if (showErrors){
      if (resources == null){
        WSPopup.showError("ReadArchive_ReadWithPluginFailed",true);
        }
      else {
        WSPopup.showError("ReadArchive_ReadWithPluginSucceeded",true);
        }
      }

    return resources;
    }



/**
**********************************************************************************************
[PRIVATE] Read using a specific plugin. Internal method that just does the reading.
**********************************************************************************************
**/
  private static Resource[] readWithPlugin(File path, ArchivePlugin plugin){
    try {
      Resource[] resources = plugin.read(path);
      if (resources != null){
        usedPlugin = plugin;

        for (int i=0;i<resources.length;i++){
          resources[i].setReadPlugin(plugin);
          }

        return resources;
        }
      }
    catch (Throwable t){
      ErrorLogger.log(t);
      }
    return null;
    }


  }

