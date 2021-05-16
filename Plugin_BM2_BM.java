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
public class Plugin_BM2_BM extends ArchivePlugin {


/**
**********************************************************************************************

**********************************************************************************************
**/
  public Plugin_BM2_BM() {
    super("BM2_BM","BM2_BM");

    //            read write
    setProperties(true,false);
    setIsCollection(false);

    setGames("Austerlitz");
    setExtensions("bm2"); // MUST BE LOWER CASE
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

      // 2 - Header
      if (fm.readString(2).equals("BM")){
        rating += 25; // So that it doesn't get priority over normal BMP plugins
        }
      else {
        rating = 0;
        }

      fm.skip(8);

      // 4 - Image Header Length (54)
      if (fm.readIntL() == 54){
        rating += 5;
        }

      fm.skip(4);

      // 4 - Image Height
      if (check.height(fm.readIntL())){
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


      // 2 - Header (BM)
      // 4 - Unknown
      // 4 - null
      // 4 - Image Header Length (54)
      // 4 - Unknown (40)
      fm.skip(18);

      // 4 - Image Width/Height?
      int width = fm.readIntL();
      check.width(width);
      int height = width;

      // 4 - Unknown
      // 2 - Unknown (1)
      // 2 - Unknown (24)
      // 8 - null
      // 4 - Unknown (2834)
      // 4 - Unknown (2834)
      // 8 - null
      fm.skip(32);



      ///// FOR USING A 24BIT OR 32BIT RGBA FORMAT /////

      // X - Pixels
      int numPixels = width*height;
      int[] pixels = new int[numPixels];
      for (int p=0;p<numPixels;p++){
        // 1 - Red
        // 1 - Green
        // 1 - Blue
        int r = fm.readByteU();
        int g = fm.readByteU();
        int b = fm.readByteU();

        pixels[p] = ((255 << 24) | (b << 16) | (g << 8) | (r));
        }



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