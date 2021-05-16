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
public class Plugin_EDI extends ArchivePlugin {


/**
**********************************************************************************************

**********************************************************************************************
**/
  public Plugin_EDI() {
    super("EDI","EDI");

    //            read write
    setProperties(true,false);
    setIsCollection(false);

    setGames("Project Eden");
    setExtensions("edi"); // MUST BE LOWER CASE
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

      fm.skip(8);

      // 4 - Image Width
      if (check.width(fm.readIntL())){
        rating += 5;
        }

      // 4 - Bytes Per Pixel (2)
      if (fm.readIntL() == 2){
        rating += 5;
        }

      fm.skip(4);

      // 4 - Pixel Data Length
      if (check.length(fm.readIntL(),fm.length())){
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


      // 4 - Unknown (1)
      // 4 - Unknown
      fm.skip(8);

      // 4 - Image Dimensions (image is a square, so width=height=dimension)
      int width = fm.readIntL();
      check.width(width);
      int height = width;

      // 4 - Bytes Per Pixel (2)
      // 4 - null
      // 4 - Pixel Data Length
      // 4 - Unknown
      // 4 - Unknown (65)
      // 4 - Unknown (16)
      // 4 - Red Color Mask
      // 4 - Green Color Mask
      // 4 - Blue Color Mask
      // 4 - Alpha Color Mask
      // 4 - null
      fm.skip(44);



      // X - Pixels (GGGBBBBB ARRRRRGG)
      int numPixels = width*height;
      int[] pixels = new int[numPixels];

      for (int y=height-1;y>=0;y--){
        for (int x=0;x<width;x++){
          // 5 bits - Red
          // 5 bits - Green
          // 5 bits - Blue
          // 1 bit  - Alpha
          int byte1 = fm.readByteU();
          int byte2 = fm.readByteU();

          int r = ((byte2&124)>>3)*8;
          int g = (((byte2&3)<<3)|((byte1&224)>>5))*8;
          int b = (byte1&63)*8;
          int a = (byte2&128)*255;
          pixels[y*width+x] = ((a << 24) | (r << 16) | (g << 8) | (b));

          // 4 - null
          fm.skip(4);
          }
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