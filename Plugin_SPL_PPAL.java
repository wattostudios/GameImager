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
public class Plugin_SPL_PPAL extends ArchivePlugin {


/**
**********************************************************************************************

**********************************************************************************************
**/
  public Plugin_SPL_PPAL() {
    super("SPL_PPAL","SPL_PPAL");

    //            read write
    setProperties(true,false);
    setIsCollection(false);

    setGames("Red Baron 3D");
    setExtensions("spl"); // MUST BE LOWER CASE
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
      if (fm.readString(4).equals("PPAL")){
        rating += 50;
        }
      else {
        rating = 0;
        }

      fm.skip(4);

      // 4 - Header
      if (fm.readString(4).equals("head")){
        rating += 5;
        }

      // 4 - Header Length (4)
      if (fm.readIntL() == 4){
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

      // 4 - Header (PPAL)
      // 4 - Image Length [+44]
      // 4 - Header (head)
      // 4 - Header Length (4)
      // 4 - Unknown (4)
      // 4 - Header (data)
      fm.skip(24);

      // 4 - Color Palette Length (1024)
      int numColors = fm.readIntL() / 4;
      check.numColors(numColors);


      // X - Palette
      int[] palette = new int[numColors];
      for (int i=0;i<numColors;i++){
        // 1 - Red
        // 1 - Green
        // 1 - Blue
        // 1 - Alpha
        int r = fm.readByteU();
        int g = fm.readByteU();
        int b = fm.readByteU();
        int a = 255 - fm.readByteU();

        palette[i] = ((a << 24) | (b << 16) | (g << 8) | (r));
        }


      // 4 - Header (pspl)
      // 4 - Image Data Length (including next field)
      // 2 - Unknown (1029)
      fm.skip(10);

      int width = 256;
      int height = 128;


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