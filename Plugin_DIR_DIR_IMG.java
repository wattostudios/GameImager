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
public class Plugin_DIR_DIR_IMG extends ArchivePlugin {


/**
**********************************************************************************************

**********************************************************************************************
**/
  public Plugin_DIR_DIR_IMG() {
    super("DIR_DIR_IMG","Worms IMG Image Viewer");

    //            read write
    setProperties(true,true);

    setGames("Worms 2",
             "Worms Armageddon");
    setExtensions("img");
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
      if (fm.readString(4).equals("IMG" + (byte)26)){
        rating += 50;
        }
      else {
        rating = 0;
        }

      // 4 - File Size
      if (fm.readIntL() == fm.getLength()){
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

      // 4 - Header (IMG + (byte)26)
      // 4 - File Size
      fm.skip(8);

      // X - Filename
      // 1 - null Filename Terminator
      fm.readNullString();


      int[] palette = new int[81];
      for (int i=0;i<81;i++){
        // 1 - Red
        int r = fm.readByteU();

        // 1 - Blue
        int b = fm.readByteU();

        // 1 - Green
        int g = fm.readByteU();

        palette[i] = ((b << 16) | (g << 8) | (r) | 0xFF000000);
        }

      fm.skip(1);


      // 2 - Image Width/Height
      int width = fm.readShortL();
      check.width(width);

      // 2 - Image Width/Height
      int height = fm.readShortL();
      check.width(height);


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


      String filename = path.getName();
      int width = resource.getWidth();
      int height = resource.getHeight();
      int fileSize = 256 + filename.length() + 1 + (width*height);



      // 4 - Header (IMG + (byte)26)
      fm.writeString("IMG");
      fm.writeByte((byte)26);

      // 4 - File Size
      fm.writeIntL(fileSize);

      // X - Filename
      // 1 - null Filename Terminator
      fm.writeNullString(filename);



      // Reduce to 81 colors, if necessary
      ColorConverter.changeNumColors(resource,81);



      // X - Palette
      int numColors = 81;
      int[] palette = resource.getPalette();

      for (int i=0;i<numColors;i++){
        int color = palette[i];

        // 1 - Red
        // 1 - Blue
        // 1 - Green
        fm.writeByte((byte)color>>16);
        fm.writeByte((byte)color>>8);
        fm.writeByte((byte)color);
        }

      // 1 - Unknown
      fm.writeByte(0);

      // 2 - Image Width/Height
      fm.writeShortL((short)width);

      // 2 - Image Width/Height
      fm.writeShortL((short)height);


      // X - Pixels
      int[] pixels = resource.getPixels();
      int numPixels = pixels.length;

      for (int p=0;p<numPixels;p++){
        // 1 - Color Palette Index
        fm.writeByte((byte)pixels[p]);
        }


      fm.close();

      }
    catch (Throwable t){
      logError(t);
      }
    }


  }