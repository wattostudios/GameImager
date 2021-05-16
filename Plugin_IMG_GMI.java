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
public class Plugin_IMG_GMI extends ArchivePlugin {


/**
**********************************************************************************************

**********************************************************************************************
**/
  public Plugin_IMG_GMI() {
    super("IMG_GMI","IMG_GMI");

    //            read write
    setProperties(true,true);
    setIsCollection(false);

    setGames("Jetboat Superchamps",
             "Jetboat Superchamps 2");
    setExtensions("img"); // MUST BE LOWER CASE
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
      if (fm.readString(4).equals(" GMI")){
        rating += 50;
        }
      else {
        rating = 0;
        }

      // 4 - Image Width
      if (check.width(fm.readIntL())){
        rating += 5;
        }

      // 4 - Image Height
      if (check.height(fm.readIntL())){
        rating += 5;
        }

      fm.skip(4);

      // 4 - Bits Per Pixel
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

      // 4 - Header ( GMI)
      fm.skip(4);

      // 4 - Width
      int width = fm.readIntL();
      check.width(width);

      // 4 - Height
      int height = fm.readIntL();
      check.height(height);

      // 4 - Unknown (1)
      // 4 - Bits Per Pixel (16)
      fm.skip(8);

      // 4 - Alpha Bit Mask (5-6-5 = 0,     4-4-4-4 = 61440)
      int alphaMask = fm.readIntL();

      // 4 - Blue Bit Mask  (5-6-5 = 63488, 4-4-4-4 = 3840)
      // 4 - Green Bit Mask (5-6-5 = 2016,  4-4-4-4 = 240)
      // 4 - Red Bit Mask   (5-6-5 = 31,    4-4-4-4 = 15)
      fm.skip(12);


      // X - Pixels
      int numPixels = width*height;
      int[] pixels = new int[numPixels];

      if (alphaMask == 0){
        // 5-6-5

        for (int p=0;p<numPixels;p++){
          // 5bits - Blue
          // 6bits - Green
          // 5bits - Red
          int byte1 = fm.readByteU();
          int byte2 = fm.readByteU();

          int r = ((byte2&248)>>3)*8;
          int g = (((byte2&7)<<3)|((byte1&224)>>5))*4;
          int b = (byte1&31)*8;
          int a = 255;
          pixels[p] = ((a << 24) | (r << 16) | (g << 8) | (b));
          }
        }
      else {
        // 4-4-4-4

        for (int p=0;p<numPixels;p++){
          // 4bits - Alpha
          // 4bits - Blue
          // 4bits - Green
          // 4bits - Red
          int byte1 = fm.readByteU();
          int byte2 = fm.readByteU();

          int g = ((byte1&240)>>4)*16;
          int b = (byte1&15)*16;
          int a = ((byte2&240)>>4)*16;
          int r = (byte2&15)*16;
          pixels[p] = ((a << 24) | (r << 16) | (g << 8) | (b));
          }
        }

      fm.close();

      Resource resource = new Resource(path,pixels,width,height);
      ColorConverter.convertToPaletted(resource);

      if (alphaMask == 0){
        resource.addProperty("ColorFormat","565");
        }
      else {
        resource.addProperty("ColorFormat","4444");
        }

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
    try {


      FileManipulator fm = new FileManipulator(path,"rw");

      Resource resource = resources[0];

      WSProgressDialog.setMessage(Language.get("Progress_WritingFiles"));


      String type = resource.getProperty("ColorFormat");


      // 4 - Header ( GMI)
      fm.writeString(" GMI");

      // 4 - Width
      fm.writeIntL(resource.getWidth());

      // 4 - Height
      fm.writeIntL(resource.getHeight());

      // 4 - Unknown (1)
      fm.writeIntL(1);

      // 4 - Bits Per Pixel (16)
      fm.writeIntL(16);

      int[] pixels = resource.getImagePixels();
      int numPixels = pixels.length;

      if (type.equals("565")){
        // 4 - Alpha Bit Mask (5-6-5 = 0,     4-4-4-4 = 61440)
        // 4 - Blue Bit Mask  (5-6-5 = 63488, 4-4-4-4 = 3840)
        // 4 - Green Bit Mask (5-6-5 = 2016,  4-4-4-4 = 240)
        // 4 - Red Bit Mask   (5-6-5 = 31,    4-4-4-4 = 15)
        fm.writeIntL(0);
        fm.writeIntL(63488);
        fm.writeIntL(2016);
        fm.writeIntL(31);

        // X - Pixels
        for (int p=0;p<numPixels;p++){
          int pixel = pixels[p];

          // 5bits - Blue
          // 6bits - Green
          // 5bits - Red
          int r = (((((pixel>>16)&255)/8)<<3)&248);
          int g1 = (((((pixel>>8)&255)/4)>>3)&7);
          int byte2 = g1|r;

          int g2 = (((((pixel>>8)&255)/4)<<5)&224);
          int b = ((((pixel)&255)/8)&31);
          int byte1 = g2|b;


          fm.writeByte(byte1);
          fm.writeByte(byte2);
          }
        }
      else {
        // 4 - Alpha Bit Mask (5-6-5 = 0,     4-4-4-4 = 61440)
        // 4 - Blue Bit Mask  (5-6-5 = 63488, 4-4-4-4 = 3840)
        // 4 - Green Bit Mask (5-6-5 = 2016,  4-4-4-4 = 240)
        // 4 - Red Bit Mask   (5-6-5 = 31,    4-4-4-4 = 15)
        fm.writeIntL(61440);
        fm.writeIntL(3840);
        fm.writeIntL(240);
        fm.writeIntL(15);

        // X - Pixels
        for (int p=0;p<numPixels;p++){
          int pixel = pixels[p];

          // 4bits - Alpha
          // 4bits - Blue
          // 4bits - Green
          // 4bits - Red
          int g = (((((pixel>>8)&255)/16)<<4)&240);
          int b = ((((pixel)&255)/16)&15);
          int byte1 = g|b;

          int a = (((((pixel>>24)&255)/16)<<4)&240);
          int r = ((((pixel>>16)&255)/16)&15);
          int byte2 = a|r;


          fm.writeByte(byte1);
          fm.writeByte(byte2);
          }
        }


      fm.close();


      }
    catch (Throwable t){
      logError(t);
      }
    }


  }