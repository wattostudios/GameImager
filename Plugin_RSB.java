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
public class Plugin_RSB extends ArchivePlugin {


/**
**********************************************************************************************

**********************************************************************************************
**/
  public Plugin_RSB() {
    super("RSB","RSB");

    //            read write
    setProperties(true,false);
    setIsCollection(false);

    setGames("Ghost Recon");
    setExtensions("rsb"); // MUST BE LOWER CASE
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


      fm.skip(4);

      // 4 - Image Width
      if (check.width(fm.readIntL())){
        rating += 5;
        }

      // 4 - Image Height
      if (check.height(fm.readIntL())){
        rating += 5;
        }

      // 4 - Number Of Bits Used For Red
      int red = fm.readIntL();
      if (red >= 4 && red <= 8){
        rating += 5;
        }

      // 4 - Number Of Bits Used For Green
      int green = fm.readIntL();
      if (green >= 4 && green <= 8){
        rating += 5;
        }

      // 4 - Number Of Bits Used For Blue
      int blue = fm.readIntL();
      if (blue >= 4 && blue <= 8){
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

      // 4 - Unknown (2/4/5/6)
      fm.skip(4);

      // 4 - Width
      int width = fm.readIntL();
      check.width(width);

      // 4 - Height
      int height = fm.readIntL();
      check.height(height);

      // 4 - Number Of Bits Used For Red
      int numRed = fm.readIntL();
      check.length(numRed,10);

      // 4 - Number Of Bits Used For Green
      int numGreen = fm.readIntL();
      check.length(numGreen,10);

      // 4 - Number Of Bits Used For Blue
      int numBlue = fm.readIntL();
      check.length(numBlue,10);

      // 4 - Number Of Bits Used For Alpha
      int numAlpha = fm.readIntL();
      check.length(numAlpha,10);


      // X - Pixels
      int numPixels = width*height;
      int[] pixels = new int[numPixels];

      if (numRed == 8 && numGreen == 8 && numBlue == 8 && numAlpha == 8){
        // 8-8-8-8
        for (int p=0;p<numPixels;p++){
          // 1 - Red
          // 1 - Green
          // 1 - Blue
          // 1 - Alpha
          int r = fm.readByteU();
          int g = fm.readByteU();
          int b = fm.readByteU();
          int a = fm.readByteU();
          pixels[p] = ((a << 24) | (b << 16) | (g << 8) | (r));
          }
        }
      else if (numRed == 4 && numGreen == 4 && numBlue == 4 && numAlpha == 4){
        // 4-4-4-4
        for (int p=0;p<numPixels;p++){
          // 4 bits - Red
          // 4 bits - Green
          // 4 bits - Blue
          // 4 bits - Alpha
          int byte1 = fm.readByteU();
          int byte2 = fm.readByteU();

          int r = ((byte1&240)>>4)*16;
          int g = (byte1&15)*16;
          int b = ((byte2&240)>>4)*16;
          int a = (byte2&15)*16;
          pixels[p] = ((a << 24) | (b << 16) | (g << 8) | (r));
          }
        }
      else if (numRed == 5 && numGreen == 6 && numBlue == 5 && numAlpha == 0){
        // 5-6-5
        for (int p=0;p<numPixels;p++){
          // 5 bits - Red
          // 6 bits - Green
          // 5 bits - Blue
          int byte2 = fm.readByteU();
          int byte1 = fm.readByteU();

          int b = ((byte1&248)>>3)*8;
          int g = (((byte1&7)<<3)|((byte2&224)>>5))*4;
          int r = (byte2&31)*8;
          int a = 255;
          pixels[p] = ((a << 24) | (b << 16) | (g << 8) | (r));
          }
        }
      else {
        System.out.println("Unknown format: " + numRed + "," + numGreen + "," + numBlue + "," + numAlpha);
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


/**
**********************************************************************************************
Writes an [archive] File with the contents of the Resources
**********************************************************************************************
**/
  public void write(Resource[] resources, File path) {
    }


  }