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
public class Plugin_RAW_MHWANH extends ArchivePlugin {


/**
**********************************************************************************************

**********************************************************************************************
**/
  public Plugin_RAW_MHWANH() {
    super("RAW_MHWANH","RAW_MHWANH");

    //            read write
    setProperties(true,false);
    setIsCollection(false);

    setGames("Ecstatica 2");
    setExtensions("raw"); // MUST BE LOWER CASE
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
      if (fm.readString(6).equals("mhwanh")){
        rating += 50;
        }
      else {
        rating = 0;
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


      // 6 - Header (mhwanh)
      // 1 - null
      // 2 - Unknown
      fm.skip(9);

      // 2 - Image Height
      short width = fm.readShortL();
      check.width(width);

      // 2 - Unknown
      fm.skip(2);

      // 2 - Number Of Colors (256/-256)
      int numColors = fm.readShortL();
      if (numColors < 0){
        numColors = 0-numColors;
        }


      check.numColors(numColors);

      // 2 - Unknown
      // 2 - Unknown (44)
      // 13 - null
      fm.seek(32);


      // X - Palette
      int[] palette = new int[numColors];
      for (int i=0;i<numColors;i++){
        // 1 - Red
        // 1 - Green
        // 1 - Blue
        int r = fm.readByteU();
        int g = fm.readByteU();
        int b = fm.readByteU();

        palette[i] = ((255 << 24) | (b << 16) | (g << 8) | (r));
        }

      int height = (int)(fm.length() - fm.getFilePointer()) / width;

      if (width == 384){
        height = 480;
        width = 640;
        }


      // X - Pixels
      int numPixels = width*height;
      int[] pixels = new int[numPixels];
      for (int p=0;p<numPixels;p++){
        // 1 - Color Palette Index
        pixels[p] = fm.readByteU();
        }


      fm.close();

      return new Resource[]{new Resource(path,pixels,width,height,palette)};

      }
    catch (Throwable t){
      logError(t);
      return null;
      }
    }



  }