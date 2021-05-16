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
public class Plugin_BNK_KNB extends ArchivePlugin {


/**
**********************************************************************************************

**********************************************************************************************
**/
  public Plugin_BNK_KNB() {
    super("BNK_KNB","BNK_KNB");

    //            read write
    setProperties(true,false);
    setIsCollection(true);

    setGames("Nitro Stunt Racing");
    setExtensions("bnk"); // MUST BE LOWER CASE
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
      if (fm.readString(4).equals("KNB" + (char)0)){
        rating += 50;
        }
      else {
        rating = 0;
        }

      fm.skip(16);


      // 4 - Number Of Images
      if (check.numFiles(fm.readIntL())){
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

      // 4 - Header ("KNB" + null)
      // 4 - Unknown (590082)
      // 4 - Unknown
      // 4 - Unknown (-1)
      // 4 - Unknown (1)
      fm.skip(20);

      // 4 - Number Of Images In The Archive
      int numFiles = fm.readIntL();
      check.numFiles(numFiles);


      Resource[] resources = new Resource[numFiles];

      for (int i=0;i<numFiles;i++){

        // 4 - Unknown (128)
        // 4 - Unknown (2)
        fm.skip(8);

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


        // X - DDS-3 format
        int[] pixels = Plugin_DDS_DDS.readDXT3Pixels(fm,width,height);


        // determine the length of the remaining mipmaps
        int mipLength = 0;
        int mipWidth = width / 2;

        for (int j=1;j<numMipMaps;j++){
          mipLength += (mipWidth*mipWidth);
          mipWidth /= 2;
          }

        fm.skip(mipLength);

        // 4 - File Path Length (including null)
        int filenameLength = fm.readIntL();
        check.filenameLength(filenameLength);

        // X - File Path
        // 1 - null File Path Terminator
        fm.skip(filenameLength);


        resources[i] = new Resource(path,pixels,width,height);
        ColorConverter.convertToPaletted(resources[i]);
        }


      fm.close();

      return resources;
      }
    catch (Throwable t){
      logError(t);
      return null;
      }
    }



  }