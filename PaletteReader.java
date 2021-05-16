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
public class PaletteReader {

  /** The plugin used to read the archive **/
  static PalettePlugin usedPlugin;


/**
**********************************************************************************************

**********************************************************************************************
**/
  public PaletteReader(){
    }


/**
**********************************************************************************************
Gets the plugins that could read this file, sorted by rating.
**********************************************************************************************
**/
  public static RatedPlugin[] getRatedPlugins(File path){
    RatedPlugin[] plugins = PluginFinder.findPlugins(path,PalettePlugin.class);
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
  public static PalettePlugin getUsedPlugin(){
    return usedPlugin;
    }


/**
**********************************************************************************************
Read trying all plugins.
**********************************************************************************************
**/
  public static Palette read(File path, boolean showErrors){
    // error checking
    if (path == null || !path.exists()){
      if (showErrors){
        WSPopup.showError("ReadArchive_FileDoesNotExist",true);
        }
      return null;
      }

    if (path.isDirectory()){
      if (showErrors){
        WSPopup.showError("ReadPalette_FileNotAPalette",true);
        }
      return null;
      }

    // get plugins
    RatedPlugin[] plugins = getRatedPlugins(path);
    if (plugins == null || plugins.length == 0){
      if (showErrors){
        WSPopup.showError("ReadPalette_NoPluginsFound",true);
        }
      return null;
      }

    // read the file
    Palette palette = null;

    for (int i=0;i<plugins.length;i++){
      palette = readWithPlugin(path,(PalettePlugin)plugins[i].getPlugin());
      if (palette != null){
        break;
        }
      }

    if (showErrors){
      if (palette == null){
        WSPopup.showError("ReadPalette_ReadFailed",true);
        }
      else {
        WSPopup.showError("ReadPalette_ReadSucceeded",true);
        }
      }

    return palette;
    }



/**
**********************************************************************************************
Read using a specific plugin.
**********************************************************************************************
**/
  public static Palette read(File path, PalettePlugin plugin, boolean showErrors){
    // error checking
    if (path == null || !path.exists()){
      if (showErrors){
        WSPopup.showError("ReadArchive_FileDoesNotExist",true);
        }
      return null;
      }

    if (path.isDirectory()){
      if (showErrors){
        WSPopup.showError("ReadPalette_FileNotAnPalette",true);
        }
      return null;
      }

    if (plugin == null){
      return null;
      }

    // read the file
    Palette palette = readWithPlugin(path,plugin);

    if (showErrors){
      if (palette == null){
        WSPopup.showError("ReadPalette_ReadWithPluginFailed",true);
        }
      else {
        WSPopup.showMessage("ReadPalette_ReadWithPluginSucceeded",true);
        }
      }

    return palette;
    }



/**
**********************************************************************************************
[PRIVATE] Read using a specific plugin. Internal method that just does the reading.
**********************************************************************************************
**/
  private static Palette readWithPlugin(File path, PalettePlugin plugin){
    try {
      Palette palette = plugin.readPalette(path);
      if (palette != null){
        usedPlugin = plugin;
        //palette.setReadPlugin(plugin);
        return palette;
        }
      }
    catch (Throwable t){
      ErrorLogger.log(t);
      }
    return null;
    }


  }

