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
public class Plugin_TEX_3TEX extends ArchivePlugin {


/**
**********************************************************************************************

**********************************************************************************************
**/
  public Plugin_TEX_3TEX() {
    super("TEX_3TEX","TEX_3TEX");

    //            read write
    setProperties(true,true);
    setIsCollection(true);

    setGames("High Heat Baseball 2000");
    setExtensions("tex"); // MUST BE LOWER CASE
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
      if (fm.readString(4).equals("3TEX")){
        rating += 50;
        }
      else {
        rating = 0;
        }

      // 4 - Number Of Files
      if (check.numFiles(fm.readIntL())){
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

      // 4 - Header (3TEX)
      fm.skip(4);

      // 4 - Number Of Files
      int numFiles = fm.readIntL();
      check.numFiles(numFiles);


      String[] names = new String[numFiles];
      for (int i=0;i<numFiles;i++){
        // 32 - Filename (null terminated)
        names[i] = fm.readNullString(32);
        }


      Resource[] resources = new Resource[numFiles];


      for (int i=0;i<numFiles;i++){
        // 4 - Width
        int width = fm.readIntL();
        check.width(width);

        // 4 - Height
        int height = fm.readIntL();
        check.height(height);

        // 4 - Unknown (1)
        fm.skip(4);

        // X - Pixels
        int numPixels = width*height;
        int[] pixels = new int[numPixels];
        for (int p=0;p<numPixels;p++){
          // 1 - Color Palette Index
          pixels[p] = fm.readByteU();
          }

        // X - Palette
        int numColors = 256;
        int[] palette = new int[numColors];
        for (int c=0;c<numColors;c++){
          // 1 - Blue
          // 1 - Green
          // 1 - Red
          // 1 - Alpha
          int b = fm.readByteU();
          int g = fm.readByteU();
          int r = fm.readByteU();
          int a = fm.readByteU();

          palette[c] = ((a << 24) | (b << 16) | (g << 8) | (r));
          }

        resources[i] = new Resource(path,pixels,width,height,palette);
        resources[i].setProperty("Filename",names[i]);
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
    try {

      FileManipulator fm = new FileManipulator(path,"rw");

      WSProgressDialog.setMessage(Language.get("Progress_WritingFiles"));

      int numFiles = resources.length;



      // 4 - Header (3TEX)
      fm.writeString("3TEX");

      // 4 - Number Of Files
      fm.writeIntL(numFiles);


      for (int i=0;i<numFiles;i++){
        Resource resource = resources[i];

        // 32 - Filename (null terminated)
        String filename = resource.getProperty("Filename");
        fm.writeNullString(filename,32);
        }


      for (int i=0;i<numFiles;i++){
        Resource resource = resources[i];

        // 4 - Width
        fm.writeIntL(resource.getWidth());

        // 4 - Height
        fm.writeIntL(resource.getHeight());

        // 4 - Unknown (1)
        fm.writeIntL(1);


        // Reduce to 256 colors, if necessary
        ColorConverter.changeNumColors(resource,256);


        // X - Pixels
        int[] pixels = resource.getPixels();
        int numPixels = pixels.length;

        for (int p=0;p<numPixels;p++){
          // 1 - Color Palette Index
          fm.writeByte((byte)pixels[p]);
          }


        // X - Palette
        int numColors = 256;
        int[] palette = resource.getPalette();

        for (int c=0;c<numColors;c++){
          int color = palette[c];

          // 1 - Blue
          // 1 - Green
          // 1 - Red
          // 1 - Alpha
          fm.writeByte(color>>16);
          fm.writeByte(color>>8);
          fm.writeByte(color);
          fm.writeByte(color>>24);
          }

        }


      fm.close();

      }
    catch (Throwable t){
      logError(t);
      }
    }


  }