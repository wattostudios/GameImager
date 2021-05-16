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

import org.watto.Language;
import org.watto.component.WSProgressDialog;
import org.watto.manipulator.*;

import java.io.File;

import javax.swing.ImageIcon;
import java.awt.*;
import java.awt.image.*;
import java.io.*;


/**
**********************************************************************************************

**********************************************************************************************
**/
public class Plugin_TEX_XET extends ArchivePlugin {


/**
**********************************************************************************************

**********************************************************************************************
**/
  public Plugin_TEX_XET() {
    super("TEX_XET","TEX_XET");

    //            read write
    setProperties(true,false);
    setIsCollection(false);

    setGames("Nitro Stunt Racing");
    setExtensions("tex"); // MUST BE LOWER CASE
    setPlatforms("PC");
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public int getMatchRating(FileManipulator fm) {
    try {

      int rating = 0;

      if (fm.getExtension().equals(extensions[0])){
        rating += 25;
        }

      // 4 - Header
      if (fm.readString(4).equals("XET" + (char)0)){
        rating += 50;
        }
      else {
        rating = 0;
        }

      fm.skip(16);

      // 4 - Number Of MipMaps
      if (check.numColors(fm.readIntL())){
        rating += 5;
        }


      return rating;

      }
    catch (Throwable t){
      return 0;
      }
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public Resource[] read(File path) {
    try {

      FileManipulator fm = new FileManipulator(path,"r");


      // 4 - Header ("XET" + null)
      // 4 - Unknown (590081)
      // 4 - File Path Offset [-4]
      // 4 - Unknown (128)
      // 4 - Unknown (2)
      fm.skip(20);

      // 4 - Number Of Mipmaps
      int numMipMaps = fm.readIntL();
      check.numColors(numMipMaps);

      // 4 - null
      // 4 - Unknown (22)
      // 4 - Timestamp? (1065353216)
      // 4 - Unknown (6/8)
      // 12 - null
      fm.skip(28);


      int width = (int)Math.pow(2,numMipMaps);
      check.width(width);
      int height = width;


      int[] pixels = Plugin_DDS_DDS.readDXT3Pixels(fm,width,height);


      fm.close();

      Resource resource = new Resource(path,pixels,width,height);
      ColorConverter.convertToPaletted(resource);

      return new Resource[]{resource};
      }
    catch (Throwable t){
      logError(t);
      return null;
      }
    }



  }