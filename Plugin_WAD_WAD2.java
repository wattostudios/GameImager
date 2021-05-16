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
public class Plugin_WAD_WAD2 extends ArchivePlugin {


/**
**********************************************************************************************

**********************************************************************************************
**/
  public Plugin_WAD_WAD2() {
    super("WAD_WAD2","WAD_WAD2");

    //            read write
    setProperties(true,false);
    setIsCollection(true);

    setGames("Eternal War: Shadows Of Light");
    setExtensions("wad"); // MUST BE LOWER CASE
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
      if (fm.readString(4).equals("WAD2")){
        rating += 50;
        }
      else {
        rating = 0;
        }

      long arcSize = fm.length();

      // 4 - Number Of Files
      if (check.numFiles(fm.readIntL())){
        rating += 5;
        }

      // 4 - Directory Offset
      if (check.offset(fm.readIntL(),arcSize)){
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

      long arcSize = fm.length();

      // 4 - Header (WAD2)
      fm.skip(4);

      // 4 - Number Of Files
      int numFiles = fm.readIntL();
      check.numFiles(numFiles);

      // 4 - Directory Offset
      int dirOffset = fm.readIntL();
      check.offset(dirOffset,arcSize);

      fm.seek(dirOffset);


      int realNumFiles = 0;
      int[] offsets = new int[numFiles];
      for (int i=0;i<numFiles;i++){
        // 4 - Offset
        int offset = fm.readIntL();
        check.offset(offset,arcSize);

        // 4 - Compressed File Size
        // 4 - Uncompressed File Size
        fm.skip(8);

        // 1 - File Type (66=Paletted Image (*.lmp file), 68=Unknown)
        int fileType = fm.read();

        // 1 - Compression Type
        // 2 - Padding
        // 16 - Filename (null)
        fm.skip(19);

        if (fileType == 66){
          offsets[realNumFiles] = offset;
          realNumFiles++;
          }
        }


      numFiles = realNumFiles;

      Resource[] resources = new Resource[numFiles];

      Palette palette = PaletteGenerator.getGrayscalePalette();

      for (int i=0;i<numFiles;i++){
        fm.seek(offsets[i]);

        // 4 - Width
        int width = fm.readIntL();
        check.width(width);

        // 4 - Height
        int height = fm.readIntL();
        check.height(height);

        // X - Pixels
        int numPixels = width*height;
        int[] pixels = new int[numPixels];
        for (int p=0;p<numPixels;p++){
          // 1 - Color Palette Index
          pixels[p] = fm.readByteU();
          }

        resources[i] = new Resource(path,pixels,width,height,palette);
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