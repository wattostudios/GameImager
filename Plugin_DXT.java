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
public class Plugin_DXT extends ArchivePlugin {


/**
**********************************************************************************************

**********************************************************************************************
**/
  public Plugin_DXT() {
    super("DXT","Cars DXT");

    //            read write
    setProperties(true,true);

    setGames("Cars");
    setExtensions("dxt");
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


      // 4 - Unknown (2)
      if (fm.readIntL() == 2){
        rating += 4;
        }

      // 4 - Unknown (54)
      if (fm.readIntL() == 54){
        rating += 5;
        }

      // 4 - null
      if (fm.readIntL() == 0){
        rating += 5;
        }

      // 4 - Image Width
      if (check.width(fm.readIntL())){
        rating += 5;
        }

      // 4 - Image Height
      if (check.height(fm.readIntL())){
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

      // 4 - Unknown (2)
      // 4 - Unknown (54)
      // 4 - null
      fm.skip(12);

      // 4 - Number Of Mipmaps
      int numMipmaps = fm.readIntL();

      // 4 - Image Width
      // 4 - Image Height
      fm.skip(8);

      // 4 - Image Width
      int width = fm.readIntL();
      check.width(width);

      // 4 - Image Height
      int height = fm.readIntL();
      check.height(height);

      // 4 - Data Length
      int length = fm.readIntL();
      check.length(length,fm.length());


      // X - Image Data

      Resource resource;

      if ((width*height)==length){
        resource = Plugin_DDS_DDS.readDXT3(fm,width,height);
        resource.setProperty("DDSFormat","DXT3");
        }
      else{
        resource = Plugin_DDS_DDS.readDXT1(fm,width,height);
        resource.setProperty("DDSFormat","DXT1");
        }

      resource.addProperty("DDSNumMipMaps",""+numMipmaps);

      fm.close();

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
  public void write(Resource[] resources, File path) {
    try {


      FileManipulator fm = new FileManipulator(path,"rw");

      Resource resource = resources[0];

      WSProgressDialog.setMessage(Language.get("Progress_WritingFiles"));



      int height = resource.getHeight();
      int width = resource.getWidth();
      int[] pixels = resource.getImagePixels();


      int numMipMaps = 1;
      String mipMapCount = resource.getProperty("DDSNumMipMaps");
      try {
        numMipMaps = Integer.parseInt(mipMapCount);
        }
      catch (Throwable t){
        }



      String type = resource.getProperty("DDSFormat");
      if (type.equals("DXT1") || type.equals("DXT3")){
        // the format of the DDS image is found
        }
      else {
        // not a DDS image originally (ie doing a conversion), or not a valid format value, so set to DXT3
        type = "DXT3";
        }


      // 4 - Unknown (2)
      fm.writeIntL(2);

      // 4 - Unknown (54)
      fm.writeIntL(54);

      // 4 - null
      fm.writeIntL(0);

      // 4 - Number Of Mipmaps
      fm.writeIntL(numMipMaps);

      // 4 - Image Width
      fm.writeIntL(width);

      // 4 - Image Height
      fm.writeIntL(height);


      if (type.equals("DXT1")){
        writeDXT1Pixels(fm,pixels,width,height,numMipMaps);
        }
      else if (type.equals("DXT3")){
        writeDXT3Pixels(fm,pixels,width,height,numMipMaps);
        }

      fm.close();


      }
    catch (Throwable t){
      logError(t);
      }
    }



/**
**********************************************************************************************
Writes all mipmaps
**********************************************************************************************
**/
  public static void writeDXT1Pixels(FileManipulator fm, int[] pixels, int width, int height, int numMipmaps) throws Exception {

    int length = width*height/2;

    // 4 - Width
    fm.writeIntL(width);

    // 4 - Height
    fm.writeIntL(height);

    // 4 - Length
    fm.writeIntL(length);

    // X - Texture Data (DXT1=W*H/2, DXT3/5=W*H, RGBA=W*H*4)
    Plugin_DDS_DDS.writeDXT1Pixels(fm,pixels,width,height);


    for (int m=1;m<numMipmaps;m++){
      // resize the array
      int newWidth = width/2;
      int newHeight = height/2;
      int[] newPixels = new int[newWidth*newHeight];

      ColorSplitAlpha color1;
      ColorSplitAlpha color2;
      ColorSplitAlpha color3;
      ColorSplitAlpha color4;

      for (int i=0,j=0;j<newHeight;i+=2,j++){
        for (int x=0,y=0;y<newWidth;x+=2,y++){
          int rowStart = (i*width)+x;
          color1 = new ColorSplitAlpha(pixels[rowStart]);
          color2 = new ColorSplitAlpha(pixels[rowStart+1]);

          rowStart = ((i+1)*width)+x;
          color3 = new ColorSplitAlpha(pixels[rowStart]);
          color4 = new ColorSplitAlpha(pixels[rowStart+1]);

          int r = (color1.getRed() + color2.getRed() + color3.getRed() + color4.getRed())/4;
          int g = (color1.getGreen() + color2.getGreen() + color3.getGreen() + color4.getGreen())/4;
          int b = (color1.getBlue() + color2.getBlue() + color3.getBlue() + color4.getBlue())/4;
          int a = (color1.getAlpha() + color2.getAlpha() + color3.getAlpha() + color4.getAlpha())/4;

          newPixels[j*newWidth+y] = ((a << 24) | (r << 16) | (g << 8) | b);
          }
        }

      pixels = newPixels;
      width = newWidth;
      height = newHeight;

      // write the next mipmap

      length = width*height/2;

      // 4 - Width
      fm.writeIntL(width);

      // 4 - Height
      fm.writeIntL(height);

      // 4 - Length
      fm.writeIntL(length);

      // X - Texture Data (DXT1=W*H/2, DXT3/5=W*H, RGBA=W*H*4)
      Plugin_DDS_DDS.writeDXT1Pixels(fm,pixels,width,height);
      }
    }


/**
**********************************************************************************************
Writes all mipmaps
**********************************************************************************************
**/
  public static void writeDXT3Pixels(FileManipulator fm, int[] pixels, int width, int height, int numMipmaps) throws Exception {

    int length = width*height;

    // 4 - Width
    fm.writeIntL(width);

    // 4 - Height
    fm.writeIntL(height);

    // 4 - Length
    fm.writeIntL(length);

    // X - Texture Data (DXT1=W*H/2, DXT3/5=W*H, RGBA=W*H*4)
    Plugin_DDS_DDS.writeDXT3Pixels(fm,pixels,width,height);


    for (int m=1;m<numMipmaps;m++){
      // resize the array
      int newWidth = width/2;
      int newHeight = height/2;
      int[] newPixels = new int[newWidth*newHeight];

      ColorSplitAlpha color1;
      ColorSplitAlpha color2;
      ColorSplitAlpha color3;
      ColorSplitAlpha color4;

      for (int i=0,j=0;j<newHeight;i+=2,j++){
        for (int x=0,y=0;y<newWidth;x+=2,y++){
          int rowStart = (i*width)+x;
          color1 = new ColorSplitAlpha(pixels[rowStart]);
          color2 = new ColorSplitAlpha(pixels[rowStart+1]);

          rowStart = ((i+1)*width)+x;
          color3 = new ColorSplitAlpha(pixels[rowStart]);
          color4 = new ColorSplitAlpha(pixels[rowStart+1]);

          int r = (color1.getRed() + color2.getRed() + color3.getRed() + color4.getRed())/4;
          int g = (color1.getGreen() + color2.getGreen() + color3.getGreen() + color4.getGreen())/4;
          int b = (color1.getBlue() + color2.getBlue() + color3.getBlue() + color4.getBlue())/4;
          int a = (color1.getAlpha() + color2.getAlpha() + color3.getAlpha() + color4.getAlpha())/4;

          newPixels[j*newWidth+y] = ((a << 24) | (r << 16) | (g << 8) | b);
          }
        }

      pixels = newPixels;
      width = newWidth;
      height = newHeight;

      // write the next mipmap

      length = width*height;

      // 4 - Width
      fm.writeIntL(width);

      // 4 - Height
      fm.writeIntL(height);

      // 4 - Length
      fm.writeIntL(length);

      // X - Texture Data (DXT1=W*H/2, DXT3/5=W*H, RGBA=W*H*4)
      Plugin_DDS_DDS.writeDXT3Pixels(fm,pixels,width,height);
      }
    }



  }