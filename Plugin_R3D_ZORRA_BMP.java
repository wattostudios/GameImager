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
public class Plugin_R3D_ZORRA_BMP extends ArchivePlugin {


/**
**********************************************************************************************

**********************************************************************************************
**/
  public Plugin_R3D_ZORRA_BMP() {
    super("R3D_ZORRA_BMP","Rocko's Quest BMP Image");

    //            read write
    setProperties(true,true);

    setGames("Rocko's Quest");
    setExtensions("bmp");
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

      // 4 - Image Width
      if (check.width(fm.readIntL())){
        rating += 5;
        }

      // 4 - Image Height
      if (check.height(fm.readIntL())){
        rating += 5;
        }

      // 4 - Color Depth (32)
      if (fm.readIntL() == 32){
        rating += 5;
        }

      // 4 - null
      if (fm.readIntL() == 0){
        rating += 5;
        }

      // 4 - Header Size (20)
      if (fm.readIntL() == 20){
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

      // 4 - Color Depth (32)
      // 4 - null
      // 4 - Header Size (20)
      fm.skip(12);


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

        pixels[p] = ((255 << 24) | (b << 16) | (g << 8) | (r));
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

      // 4 - Color Depth (32)
      fm.writeIntL(32);

      // 4 - null
      fm.writeIntL(0);

      // 4 - Header Size (20)
      fm.writeIntL(20);


      // X - Pixels
      int[] pixels = resource.getImagePixels();
      int numPixels = pixels.length;

      for (int p=0;p<numPixels;p++){
        int pixel = pixels[p];

        // 1 - Red
        fm.writeByte((byte)pixel);

        // 1 - Green
        fm.writeByte((byte)pixel>>8);

        // 1 - Blue
        fm.writeByte((byte)pixel>>16);

        // 1 - Alpha
        fm.writeByte((byte)pixel>>24);
        }

      fm.close();

      }
    catch (Throwable t){
      logError(t);
      }
    }


  }