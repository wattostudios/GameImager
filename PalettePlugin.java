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
The PalettePlugin is one of the most important classes in Game Extractor. A class that extends
from PalettePlugin is able to read, and possibly write, a particular format of archive.
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
public abstract class PalettePlugin extends ArchivePlugin {

/**
**********************************************************************************************
Constructor
**********************************************************************************************
**/
  public PalettePlugin() {
    setCode("PalettePlugin");
    setName("Palette Plugin");
    }


/**
**********************************************************************************************
Constructor
**********************************************************************************************
**/
  public PalettePlugin(String code) {
    setCode(code);
    setName(code);
    }


/**
**********************************************************************************************
Constructor
**********************************************************************************************
**/
  public PalettePlugin(String code, String name) {
    setCode(code);
    setName(name);
    }


/**
**********************************************************************************************
Reads the archive <i>source</i>
@param source the archive file
@return the resources in the archive
**********************************************************************************************
**/
  public abstract Palette readPalette(File source);


/**
**********************************************************************************************
Reads the archive <i>source</i>
@param source the archive file
@return the resources in the archive
**********************************************************************************************
**/
  public Resource[] read(File source){
    return null;
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
  public void writePalette(Palette palette, File destination) {
    }


  }