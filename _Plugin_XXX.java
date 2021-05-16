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
public class Plugin_XXX extends ArchivePlugin {


/**
**********************************************************************************************

**********************************************************************************************
**/
  public Plugin_XXX() {
    super("XXX","XXX");

    //            read write
    setProperties(true,false);
    setIsCollection(false);

    setGames("");
    setExtensions(""); // MUST BE LOWER CASE
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
      if (fm.readString(4).equals("PICT")){
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

      // 4 - Number Of Colors
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

      // 4 - Width
      int width = fm.readIntL();
      check.width(width);

      // 4 - Height
      int height = fm.readIntL();
      check.height(height);

      // 4 - Number Of Colors
      int numColors = fm.readIntL();
      check.numColors(numColors);


      ///// FOR USING A COLOR PALETTE /////

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
        int a = fm.readByteU();

        palette[i] = ((a << 24) | (b << 16) | (g << 8) | (r));
        }


      // X - Pixels
      int numPixels = width*height;
      int[] pixels = new int[numPixels];
      for (int p=0;p<numPixels;p++){
        // 1 - Color Palette Index
        pixels[p] = fm.readByteU();
        }


      ///// FOR USING A 24BIT OR 32BIT RGBA FORMAT /////

      // X - Pixels
      int numPixels = width*height;
      int[] pixels = new int[numPixels];

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


      ///// FOR USING A 16BIT 5551 FORMAT /////

      // X - Pixels
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

          int r = ((byte2&248)>>3)*8;
          int g = (((byte2&7)<<3)|((byte1&192)>>6))*8;
          int b = (byte1&63)*8;
          int a = 255;
          pixels[y*width+x] = ((a << 24) | (r << 16) | (g << 8) | (b));
          }
        }


      ///// FOR USING A 16BIT 565 FORMAT /////

      // X - Pixels
      int numPixels = width*height;
      int[] pixels = new int[numPixels];

      for (int y=height-1;y>=0;y--){
        for (int x=0;x<width;x++){
          // 5 bits - Red
          // 6 bits - Green
          // 5 bits - Blue
          int byte1 = fm.readByteU();
          int byte2 = fm.readByteU();

          int r = ((byte2&248)>>3)*8;
          int g = (((byte2&7)<<3)|((byte1&224)>>5))*4;
          int b = (byte1&31)*8;
          int a = 255;
          pixels[y*width+x] = ((a << 24) | (r << 16) | (g << 8) | (b));
          }
        }


      ///// FOR USING A 16BIT 4444 FORMAT /////

      // X - Pixels
      int numPixels = width*height;
      int[] pixels = new int[numPixels];

      for (int y=height-1;y>=0;y--){
        for (int x=0;x<width;x++){
          // 4 bits - Alpha
          // 4 bits - Blue
          // 4 bits - Green
          // 4 bits - Red
          int byte1 = fm.readByteU();
          int byte2 = fm.readByteU();

          int g = ((byte1&240)>>4)*16;
          int b = (byte1&15)*16;
          int a = ((byte2&240)>>4)*16;
          int r = (byte2&15)*16;
          pixels[y*width+x] = ((a << 24) | (r << 16) | (g << 8) | (b));
          }
        }


      fm.close();

      return new Resource[]{new Resource(path,pixels,width,height,palette)};

      Resource resource = new Resource(path,pixels,width,height);
      ColorConverter.convertToPaletted(resource);
      ColorConverter.removeAlpha(resource);

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

      // 4 - Width
      fm.writeIntL(resource.getWidth());

      // 4 - Height
      fm.writeIntL(resource.getHeight());


      // Reduce to 256 colors, if necessary
      ColorConverter.changeNumColors(resource,256);



      // X - Palette
      int numColors = 256;
      int[] palette = resource.getPalette();

      for (int i=0;i<numColors;i++){
        int color = palette[i];

        // 1 - Red
        // 1 - Green
        // 1 - Blue
        // 1 - Alpha
        fm.writeByte(color);
        fm.writeByte(color>>8);
        fm.writeByte(color>>16);
        fm.writeByte(color>>24);
        }


      // X - Pixels
      int[] pixels = resource.getPixels();
      int numPixels = pixels.length;

      for (int p=0;p<numPixels;p++){
        // 1 - Color Palette Index
        fm.writeByte((byte)pixels[p]);
        }



      // X - Pixels
      int[] pixels = resource.getImagePixels();
      int numPixels = pixels.length;

      for (int p=0;p<numPixels;p++){
        int pixel = pixels[p];

        // 1 - Red
        // 1 - Green
        // 1 - Blue
        // 1 - Alpha
        fm.writeByte(pixel);
        fm.writeByte(pixel>>8);
        fm.writeByte(pixel>>16);
        fm.writeByte(pixel>>24);
        }

      fm.close();

      }
    catch (Throwable t){
      logError(t);
      }
    }


  }