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
import org.watto.component.WSPluginException;
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
public class Plugin_TGA extends ArchivePlugin {


/**
**********************************************************************************************

**********************************************************************************************
**/
  public Plugin_TGA() {
    super("TGA","TGA Image");

    //            read write
    setProperties(true,true);
    setIsCollection(false);

    setGames("");
    setExtensions("tga"); // MUST BE LOWER CASE
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


      // 1 - Header 2 Size
      if (fm.readByteU() == 0){
        rating += 5;
        }

      // 1 - Palette Flag
      fm.skip(1);

      // 1 - Image Type
      int imageType = fm.readByteU();
      if (imageType >= 0 && imageType <= 11){
        rating += 5;
        }

      // 2 - First Color Map
      // 2 - Number Of Colors
      // 1 - Number Of Bits per Color
      // 2 - X Position
      // 2 - Y Position
      fm.skip(9);

      // 2 - Image Width
      if (check.width(fm.readShortL())){
        rating += 5;
        }

      // 2 - Image Height
      if (check.height(fm.readShortL())){
        rating += 5;
        }

      // 1 - Color Depth
      int colorDepth = fm.readByteU();
      if (colorDepth == 8 || colorDepth == 16 || colorDepth == 24 || colorDepth == 32){
        rating += 4;
        }

      // 1 - Descriptor Flag


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

      // 1 - Header 2 Size
      int headerSize = fm.readByteU();

      // 1 - Palette Flag
      int paletteFlag = fm.readByteU();
      if (paletteFlag != 0){
        throw new WSPluginException("Paletted TGA images not supported");
        }

      // 1 - Image Type
      int imageType = fm.readByteU();
      boolean compressed = false;
      if (imageType >= 8){
        compressed = true;
        imageType -= 8;
        }
      if (imageType != 2){
        throw new WSPluginException("Only RGB TGA images are supported");
        }

      // 2 - First Color Map
      // 2 - Number Of Colors
      // 1 - Number Of Bits per Color
      // 2 - X Position
      // 2 - Y Position
      fm.skip(9);

      // 2 - Width
      short width = fm.readShortL();
      check.width(width);

      // 2 - Height
      short height = fm.readShortL();
      check.height(height);

      // 1 - Color Depth
      int colorDepth = fm.readByteU();

      // 1 - Descriptor Flag
      fm.skip(1);


      fm.skip(headerSize);

      int numPixels = width*height;
      int[] pixels = new int[numPixels];

      if (compressed){
        if (colorDepth == 32){
          pixels = readRLE32(fm,pixels,numPixels,width,height);
          }
        else if (colorDepth == 24){
          pixels = readRLE24(fm,pixels,numPixels,width,height);
          }
        else if (colorDepth == 16){
          pixels = readRLE16(fm,pixels,numPixels,width,height);
          }
        }
      else {
        pixels = readRGB(fm,pixels,width,height,colorDepth);
        }


      fm.close();

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

**********************************************************************************************
**/
  public int[] readRLE32(FileManipulator fm, int[] pixels, int numPixels, int width, int height) {
    try {

      int numRead = 0;
      while (numRead < numPixels){
        // 1 - Header
        int header = fm.readByteU();


        boolean compressed = ((header&128) == 128);
        int count = (header&127);
        if (count < 0){
          count = 256 + count;
          }

        count++;

        if (count == 0){
          return null;
          }


        if (compressed){
          // 1 - Blue
          // 1 - Green
          // 1 - Red
          // 1 - Alpha
          int b = fm.readByteU();
          int g = fm.readByteU();
          int r = fm.readByteU();
          int a = fm.readByteU();

          for (int i=0;i<count;i++){
            pixels[numRead+i] = ((a << 24) | (r << 16) | (g << 8) | (b));
            }
          }
        else {
          for (int i=0;i<count;i++){
            // 1 - Blue
            // 1 - Green
            // 1 - Red
            // 1 - Alpha
            int b = fm.readByteU();
            int g = fm.readByteU();
            int r = fm.readByteU();
            int a = fm.readByteU();

            pixels[numRead+i] = ((a << 24) | (r << 16) | (g << 8) | (b));
            }
          }

        numRead += count;
        }


      // flip the image
      int[] tempLine = new int[width];
      for (int h=0,j=height-1;h<j;h++,j--){
        System.arraycopy(pixels,h*width,tempLine,0,width);
        System.arraycopy(pixels,j*width,pixels,h*width,width);
        System.arraycopy(tempLine,0,pixels,j*width,width);
        }

      return pixels;

      }
    catch (Throwable t){
      logError(t);
      return null;
      }
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public int[] readRLE24(FileManipulator fm, int[] pixels, int numPixels, int width, int height) {
    try {

      int numRead = 0;
      while (numRead < numPixels){
        // 1 - Header
        int header = fm.readByteU();


        boolean compressed = ((header&128) == 128);
        int count = (header&127);
        if (count < 0){
          count = 256 + count;
          }

        count++;

        if (count == 0){
          return null;
          }


        if (compressed){
          // 1 - Blue
          // 1 - Green
          // 1 - Red
          int b = fm.readByteU();
          int g = fm.readByteU();
          int r = fm.readByteU();

          for (int i=0;i<count;i++){
            pixels[numRead+i] = ((255 << 24) | (r << 16) | (g << 8) | (b));
            }
          }
        else {
          for (int i=0;i<count;i++){
            // 1 - Blue
            // 1 - Green
            // 1 - Red
            int b = fm.readByteU();
            int g = fm.readByteU();
            int r = fm.readByteU();

            pixels[numRead+i] = ((255 << 24) | (r << 16) | (g << 8) | (b));
            }
          }

        numRead += count;
        }


      // flip the image
      int[] tempLine = new int[width];
      for (int h=0,j=height-1;h<j;h++,j--){
        System.arraycopy(pixels,h*width,tempLine,0,width);
        System.arraycopy(pixels,j*width,pixels,h*width,width);
        System.arraycopy(tempLine,0,pixels,j*width,width);
        }

      return pixels;

      }
    catch (Throwable t){
      logError(t);
      return null;
      }
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public int[] readRLE16(FileManipulator fm, int[] pixels, int numPixels, int width, int height) {
    try {

      int numRead = 0;
      while (numRead < numPixels){
        // 1 - Header
        int header = fm.readByteU();


        boolean compressed = ((header&128) == 128);
        int count = (header&127);
        if (count < 0){
          count = 256 + count;
          }

        count++;

        if (count == 0){
          return null;
          }


        if (compressed){
          // 1 bit  - Alpha
          // 5 bits - Blue
          // 5 bits - Green
          // 5 bits - Red
          int byte1 = fm.readByteU();
          int byte2 = fm.readByteU();

          int b = (byte1&31)*8;
          int g = (((byte1&224)>>5)|((byte2&3)<<3))*8;
          int r = ((byte2&124)>>2)*8;

          for (int i=0;i<count;i++){
            pixels[numRead+i] = ((255 << 24) | (r << 16) | (g << 8) | (b));
            }
          }
        else {
          for (int i=0;i<count;i++){
            // 1 bit  - Alpha
            // 5 bits - Blue
            // 5 bits - Green
            // 5 bits - Red
            int byte1 = fm.readByteU();
            int byte2 = fm.readByteU();

            int b = (byte1&31)*8;
            int g = (((byte1&224)>>5)|((byte2&3)<<3))*8;
            int r = ((byte2&124)>>2)*8;

            pixels[numRead+i] = ((255 << 24) | (r << 16) | (g << 8) | (b));
            }
          }

        numRead += count;
        }


      // flip the image
      int[] tempLine = new int[width];
      for (int h=0,j=height-1;h<j;h++,j--){
        System.arraycopy(pixels,h*width,tempLine,0,width);
        System.arraycopy(pixels,j*width,pixels,h*width,width);
        System.arraycopy(tempLine,0,pixels,j*width,width);
        }

      return pixels;

      }
    catch (Throwable t){
      logError(t);
      return null;
      }
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public int[] readRGB(FileManipulator fm, int[] pixels, int width, int height, int colorDepth) {
    try {

      if (colorDepth == 32){
        for (int h=height-1;h>=0;h--){
          for (int w=0;w<width;w++){
            // 1 - Blue
            // 1 - Green
            // 1 - Red
            // 1 - Alpha
            int b = fm.readByteU();
            int g = fm.readByteU();
            int r = fm.readByteU();
            int a = fm.readByteU();

            pixels[h*width+w] = ((a << 24) | (r << 16) | (g << 8) | (b));
            }
          }
        }

      else if (colorDepth == 24){
        for (int h=height-1;h>=0;h--){
          for (int w=0;w<width;w++){
            // 1 - Blue
            // 1 - Green
            // 1 - Red
            int b = fm.readByteU();
            int g = fm.readByteU();
            int r = fm.readByteU();

            pixels[h*width+w] = ((255 << 24) | (r << 16) | (g << 8) | (b));
            }
          }
        }

      else if (colorDepth == 16){
        for (int h=height-1;h>=0;h--){
          for (int w=0;w<width;w++){
            // 1 bit  - Alpha
            // 5 bits - Blue
            // 5 bits - Green
            // 5 bits - Red
            int byte1 = fm.readByteU();
            int byte2 = fm.readByteU();

            int b = (byte1&31)*8;
            int g = (((byte1&224)>>5)|((byte2&3)<<3))*8;
            int r = ((byte2&124)>>2)*8;

            pixels[h*width+w] = ((255 << 24) | (r << 16) | (g << 8) | (b));
            }
          }
        }

      return pixels;

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



      int height = resource.getHeight();
      int width = resource.getWidth();



      // 1 - Header 2 Size
      fm.writeByte(0);

      // 1 - Palette Flag
      fm.writeByte(0);

      // 1 - Image Type
      fm.writeByte(2);

      // 2 - First Color Map
      fm.writeShort((short)0);

      // 2 - Number Of Colors
      fm.writeShort((short)0);

      // 1 - Number Of Bits per Color
      fm.writeByte(0);

      // 2 - X Position
      fm.writeShort((short)0);

      // 2 - Y Position
      fm.writeShort((short)0);

      // 2 - Width
      fm.writeShortL((short)width);

      // 2 - Height
      fm.writeShortL((short)height);

      // 1 - Color Depth
      fm.writeByte(32);

      // 1 - Descriptor Flag
      fm.writeByte(0);


      // X - Pixels
      int[] pixels = resource.getImagePixels();
      int numPixels = pixels.length;

      for (int h=height-1;h>=0;h--){
        for (int w=0;w<width;w++){
          int pixel = pixels[h*width+w];

          // 1 - Red
          fm.writeByte(pixel);

          // 1 - Green
          fm.writeByte(pixel>>8);

          // 1 - Blue
          fm.writeByte(pixel>>16);

          // 1 - Alpha
          fm.writeByte(pixel>>24);
          }
        }

      fm.close();

      }
    catch (Throwable t){
      logError(t);
      }
    }


  }