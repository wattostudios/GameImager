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
public class Plugin_BAG_TEX extends ArchivePlugin {


/**
**********************************************************************************************

**********************************************************************************************
**/
  public Plugin_BAG_TEX() {
    super("BAG_TEX","Brian Lara International Cricket 2005 TEX Image");

    //            read write
    setProperties(true,true);

    setGames("Brian Lara International Cricket 2005 TEX Image Viewer");
    setExtensions("tex");
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

      // 2 - Image Height
      if (check.height(fm.readShortL())){
        rating += 5;
        }

      // 2 - Image Width
      if (check.width(fm.readShortL())){
        rating += 5;
        }

      // 2 - Unknown (5)
      if (fm.readShortL() == 5){
        rating += 5;
        }

      // 2 - Number Of Mipmaps (4)
      if (check.numFiles(fm.readShortL())){
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

      // 2 - Image Height
      int height = fm.readShortL();
      check.height(height);

      // 2 - Image Width
      int width = fm.readShortL();
      check.width(width);

      // 2 - Unknown (5)
      fm.skip(2);

      // 2 - Number Of Mipmaps (4)
      int numMipmaps = fm.readShortL();
      check.numFiles(numMipmaps);


      int pixelLength = 8;
      int interWidth = width;
      int interHeight = height;
      for (int i=0;i<numMipmaps;i++){
        pixelLength += (interWidth*interHeight);
        interWidth /= 2;
        interHeight /= 2;
        }


      // Skip to the color palette
      fm.skip(pixelLength);
      check.offset(fm.getOffset());


      int numColors = 256;
      int[] palette = new int[numColors];

      // read the color palette
      for (int i=0;i<numColors;i++){
        // 1 - Red
        // 1 - Green
        // 1 - Blue
        // 1 - Alpha
        int r = fm.readByteU();
        int g = fm.readByteU();
        int b = fm.readByteU();
        fm.skip(1); //int a = fm.readByteU();

        palette[i] = ((255<<24) | (r << 16) | (g << 8) | (b));
        }



      // skip back to the first mipmap
      fm.seek(16);



      int[] pixels = new int[width*height];
      int numPixels = pixels.length;
      for (int i=0;i<numPixels;i++){
        // 1 - Palette Color Index
        pixels[i] = fm.readByteU();
        }



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



      // 2 - Image Height
      int height = resource.getHeight();
      fm.writeShortL((short)height);

      // 2 - Image Width
      int width = resource.getWidth();
      fm.writeShortL((short)width);

      // 2 - Unknown (5)
      fm.writeShortL((short)5);

      // 2 - Number Of Mipmaps (4)
      int numMipmaps = 0;
      int interWidth = width;
      int interHeight = height;
      while (interWidth > 1 && interHeight > 1){
        numMipmaps++;
        interWidth /= 2;
        interHeight /= 2;

        if (interWidth%2 == 1){
          interWidth--;
          }
        if (interHeight%2 == 1){
          interHeight--;
          }
        }
      fm.writeShortL((short)numMipmaps);



      // Reduce to 256 colors, if necessary
      ColorConverter.changeNumColors(resource,256);



      // write each mipmap
      int[] pixels = resource.getPixels();
      int numPixels = pixels.length;

      for (int m=0;m<numMipmaps;m++){

        for (int p=0;p<numPixels;p++){
          // 1 - Color Palette Index
          fm.writeByte((byte)pixels[p]);
          }

        // now generate the smaller mipmap
        int newWidth = width/2;
        int newHeight = height/2;

        boolean widthOdd = false;
        boolean heightOdd = false;

        if (newWidth%2 == 1){
          newWidth--;
          widthOdd = true;
          }
        if (newHeight%2 == 1){
          newHeight--;
          heightOdd = true;
          }

        int newNumPixels = newWidth*newHeight;

        int readPixelNum = 0;
        int writePixelNum = 0;
        for (int i=0;i<newHeight;i++){
          for (int j=0;j<newWidth;j++){
            pixels[writePixelNum] = pixels[readPixelNum];
            readPixelNum += 2;
            writePixelNum++;
            }
          if (widthOdd){
            readPixelNum+=2;
            }
          }

        width = newWidth;
        height = newHeight;
        numPixels = width*height;
        }



      // write the color palette
      int numColors = 256;
      int[] palette = resource.getPalette();

      for (int i=0;i<numColors;i++){
        int color = palette[i];

        // 1 - Red
        // 1 - Green
        // 1 - Blue
        // 1 - Alpha
        fm.writeByte((byte)color>>16);
        fm.writeByte((byte)color>>8);
        fm.writeByte((byte)color);
        fm.writeByte((byte)color>>24);
        }


      fm.close();

      }
    catch (Throwable t){
      logError(t);
      }
    }


  }