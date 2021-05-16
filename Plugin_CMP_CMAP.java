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
import java.util.zip.*;


/**
**********************************************************************************************

**********************************************************************************************
**/
public class Plugin_CMP_CMAP extends ArchivePlugin {


/**
**********************************************************************************************

**********************************************************************************************
**/
  public Plugin_CMP_CMAP() {
    super("CMP_CMAP","CMP_CMAP");

    //            read write
    setProperties(true,true);
    setIsCollection(false);

    setGames("Baram");
    setExtensions("cmp"); // MUST BE LOWER CASE
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
      if (fm.readString(4).equals("CMAP")){
        rating += 50;
        }
      else {
        rating = 0;
        }

      // 2 - Image Width
      if (check.width(fm.readShortL())){
        rating += 5;
        }

      // 2 - Image Height
      if (check.height(fm.readShortL())){
        rating += 5;
        }

      // 1 - ZLib Compression Header
      if (fm.readString(1).equals("x")){
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

      // 4 - Header (CMAP)
      fm.skip(4);


      // 2 - Image Width/Height
      short width = fm.readShortL();
      check.width(width);

      // 2 - Image Height/Width
      short height = fm.readShortL();
      check.height(height);


      // ZLib Compression
      InflaterInputStream readSource = new InflaterInputStream(new FileManipulatorUnclosableInputStream(fm));



      // X - Pixels
      int numPixels = width*height;
      int[] pixels = new int[numPixels];

      for (int p=0;p<numPixels;p++){
        // 1 - Green
        // 1 - Blue
        // 2 - Unknown
        // 1 - Alpha
        // 1 - Red

        int g = DataConverter.unsign((byte)readSource.read());
        int b = DataConverter.unsign((byte)readSource.read());
        readSource.skip(2);
        int a = DataConverter.unsign((byte)readSource.read());
        int r = DataConverter.unsign((byte)readSource.read());

        //int a = 255;

        pixels[p] = ((a << 24) | (b << 16) | (g << 8) | (r));
        }



      // close ZLib
      readSource.close();

      // close file
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


      // 4 - Header (CMAP)
      fm.writeString("CMAP");


      // 2 - Image Width/Height
      fm.writeShortL((short)resource.getWidth());

      // 2 - Image Height/Width
      fm.writeShortL((short)resource.getHeight());


      // ZLib Compression
      DeflaterOutputStream outputStream = new DeflaterOutputStream(new FileManipulatorUnclosableOutputStream(fm));


      // X - Pixels
      int[] pixels = resource.getImagePixels();
      int numPixels = pixels.length;

      for (int p=0;p<numPixels;p++){
        int pixel = pixels[p];

        // 1 - Green
        // 1 - Blue
        // 2 - Unknown
        // 1 - Alpha
        // 1 - Red
        outputStream.write((byte)(pixel>>8));
        outputStream.write((byte)(pixel>>16));
        outputStream.write((byte)0);
        outputStream.write((byte)0);
        outputStream.write((byte)(pixel>>24));
        outputStream.write((byte)(pixel));
        }


      // close ZLib
      outputStream.close();

      // close file
      fm.close();

      }
    catch (Throwable t){
      logError(t);
      }
    }


  }