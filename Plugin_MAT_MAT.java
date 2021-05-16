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
public class Plugin_MAT_MAT extends ArchivePlugin {


/**
**********************************************************************************************

**********************************************************************************************
**/
  public Plugin_MAT_MAT() {
    super("MAT_MAT","MAT_MAT");

    //            read write
    setProperties(true,false);
    setIsCollection(true);

    setGames("Jedi Knight: Mysteries Of The Sith");
    setExtensions("mat"); // MUST BE LOWER CASE
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
      if (fm.readString(4).equals("MAT ")){
        rating += 50;
        }
      else {
        rating = 0;
        }

      fm.skip(8);

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


      // 4 - Header (MAT )
      // 4 - Unknown (50)
      // 4 - Unknown (2)
      fm.skip(12);

      // 4 - Number Of Images
      int numFiles = fm.readIntL();
      check.numFiles(numFiles);

      // 4 - Number Of Images
      // 4 - null
      // 4 - Bits Per Pixel? (8)
      // 4 - Unknown
      // 4 - Unknown (64)
      // 4 - Unknown
      // 4 - Unknown
      // 4 - null
      // 4 - Unknown (64)
      // 4 - Unknown
      // 4 - Unknown (64)
      // 4 - Unknown
      // 4 - Unknown
      // 4 - Unknown (64)
      // 4 - Unknown
      fm.seek(76 + (numFiles*40));

      Resource[] resources = new Resource[numFiles];

      Palette palette = PaletteGenerator.getGrayscalePalette();

      for (int i=0;i<numFiles;i++){

        // 4 - Width
        int width = fm.readIntL();
        check.width(width);

        // 4 - Height
        int height = fm.readIntL();
        check.height(height);

        // 12 - null
        fm.skip(12);

        // 4 - Number Of Mipmaps
        int numMipMaps = fm.readIntL();

        int length = 0;
        int newWidth = width;
        int newHeight = height;
        for (int m=0;m<numMipMaps;m++){
          length += (newWidth*newHeight);
          newWidth /= 2;
          newHeight /= 2;
          }

        long nextOffset = fm.getFilePointer() + length;

        // X - Pixels
        int numPixels = width*height;
        int[] pixels = new int[numPixels];
        for (int p=0;p<numPixels;p++){
          // 1 - Color Palette Index
          pixels[p] = fm.readByteU();
          }

        resources[i] = new Resource(path,pixels,width,height,palette);

        fm.seek(nextOffset);

        }


      fm.close();

      return resources;

      }
    catch (Throwable t){
      logError(t);
      return null;
      }
    }


/**
**********************************************************************************************
Writes an [archive] File with the contents of the Resources
**********************************************************************************************
**/
  public void write(Resource[] resources, File path) {
    try {
      }
    catch (Throwable t){
      logError(t);
      }
    }


  }