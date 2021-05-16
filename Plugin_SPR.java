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
public class Plugin_SPR extends ArchivePlugin {


/**
**********************************************************************************************

**********************************************************************************************
**/
  public Plugin_SPR() {
    super("SPR","SPR");

    //            read write
    setProperties(true,false);
    setIsCollection(true);

    setGames("Killer Tank");
    setExtensions("spr"); // MUST BE LOWER CASE
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

      // 1 - numImages
      if (fm.readByte() < 20){
        rating += 5;
        }

      if (fm.readByte() == 0){
        fm.skip(1);
        }

      fm.skip(67);

      // 2 - Image Width
      if (check.width(fm.readShortL())){
        rating += 5;
        }

      // 2 - Image Height
      if (check.height(fm.readShortL())){
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


      // 1 OR 2 - Number Of Images (1)
      int numFiles = fm.readByteU();

      if (fm.readByteU() > 10){
        fm.seek(0);
        fm.skip(1);
        }
      else {
        //fm.seek(0);
        //numFiles = fm.readShortL();
        }


      Resource[] resources = new Resource[numFiles];
      for (int i=0;i<numFiles;i++){

        // 64 - Filename of Original Image (null terminated)
        // 4 - Unknown
        fm.skip(68);

        // 2 - Image Width/Height
        short width = fm.readShortL();
        check.width(width);

        // 2 - Image Width/Height
        short height = fm.readShortL();
        check.height(height);

        // 4 - Color Format? (0/1/3)
        int colorFormat = fm.readIntL();

        // 28 - null
        fm.skip(28);


        // X - Pixels
        int numPixels = width*height;
        int[] pixels = new int[numPixels];

        if (colorFormat == 1){
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

          resources[i] = new Resource(path,pixels,width,height);
          ColorConverter.convertToPaletted(resources[i]);

          //Palette palette = PaletteGenerator.getGrayscalePalette();
          //resources[i] = new Resource(path,pixels,width,height,palette);

          }
        else if (colorFormat == 0){
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

          resources[i] = new Resource(path,pixels,width,height);
          ColorConverter.convertToPaletted(resources[i]);
          }
        else if (colorFormat == 2){
          for (int y=height-1;y>=0;y--){
            for (int x=0;x<width;x++){
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
              pixels[y*width+x] = ((a << 24) | (r << 16) | (g << 8) | (b));
              }
            }

          resources[i] = new Resource(path,pixels,width,height);
          ColorConverter.convertToPaletted(resources[i]);
          }
        else {
          for (int y=height-1;y>=0;y--){
            for (int x=0;x<width;x++){
              // 1 - Color
              // 1 - Alpha
              int byte1 = fm.readByteU();
              int byte2 = fm.readByteU();

              pixels[y*width+x] = ((byte2 << 24) | (byte1 << 16) | (byte1 << 8) | (byte1));
              }
            }

          resources[i] = new Resource(path,pixels,width,height);
          ColorConverter.convertToPaletted(resources[i]);
          }

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
    }


  }