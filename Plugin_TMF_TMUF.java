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
public class Plugin_TMF_TMUF extends ArchivePlugin {


/**
**********************************************************************************************

**********************************************************************************************
**/
  public Plugin_TMF_TMUF() {
    super("TMF_TMUF","Test Drive Off-Road Image Collection");

    //            read write
    setProperties(true,true);
    setIsCollection(true);

    setGames("Test Drive Off-Road");
    setExtensions("tmf");
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
      if (fm.readString(4).equals("TMUF")){
        rating += 50;
        }
      else {
        rating = 0;
        }

      // 4 - Palette Header
      if (fm.readString(4).equals("PAL ")){
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

      // 4 - Header (TMUF)
      // 4 - Palette Header (PAL )
      fm.skip(8);


      int numColors = 256;

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

      Palette paletteObject = new Palette(palette);




      Resource[] resources = new Resource[Archive.getMaxFiles()];

      long arcSize = fm.getLength();

      int realNumFiles = 0;
      while (fm.getFilePointer() < arcSize){
        // 4 - Size Header (SIZE)
        fm.skip(4);

        // 2 - Image Width
        short width = fm.readShortL();
        check.width(width);

        // 2 - Image Height
        short height = fm.readShortL();
        check.height(height);

        // 4 - Data Header (DATA)
        fm.skip(4);

        // X - Pixels
        int numPixels = width*height;
        int[] pixels = new int[numPixels];
        for (int p=0;p<numPixels;p++){
          // 1 - Color Palette Index
          pixels[p] = fm.readByteU();
          }

        resources[realNumFiles] = new Resource(path,pixels,width,height,paletteObject);
        realNumFiles++;
        }

      resources = resizeResources(resources,realNumFiles);


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


      // First, need to generate a 256-color palette for all the images to use.
      // Will just use the first image to generate the palette, then force all others to use it.
      Resource resourcePal = resources[0];
      ColorConverter.changeNumColors(resourcePal,256);
      int[] palette = resourcePal.getPalette();


      WSProgressDialog.setMessage(Language.get("Progress_WritingFiles"));


      // 4 - Header (TMUF)
      fm.writeString("TMUF");

      // 4 - Palette Header (PAL )
      fm.writeString("PAL ");

      // X - Palette
      int numColors = 256;
      for (int i=0;i<numColors;i++){
        int color = palette[i];

        // 1 - Red
        // 1 - Green
        // 1 - Blue
        // 1 - Alpha
        fm.writeByte((byte)color);
        fm.writeByte((byte)color>>8);
        fm.writeByte((byte)color>>16);
        fm.writeByte((byte)color>>24);
        }


      int numFiles = resources.length;



      for (int i=0;i<numFiles;i++){
        Resource resource = resources[i];

        // 4 - Size Header (SIZE)
        fm.writeString("SIZE");

        // 2 - Image Width
        fm.writeShortL((short)resource.getWidth());

        // 2 - Image Height
        fm.writeShortL((short)resource.getHeight());

        // 4 - Data Header (DATA)
        fm.writeString("DATA");


        // Set the palette to the generic one for all images
        ColorConverter.changePaletteMatch(resource,palette);


        // X - Pixels
        int[] pixels = resource.getPixels();
        int numPixels = pixels.length;

        for (int p=0;p<numPixels;p++){
          // 1 - Color Palette Index
          fm.writeByte((byte)pixels[p]);
          }

        }


      fm.close();

      }
    catch (Throwable t){
      logError(t);
      }
    }


  }