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
public class Plugin_TEX_TEX extends ArchivePlugin {


/**
**********************************************************************************************

**********************************************************************************************
**/
  public Plugin_TEX_TEX() {
    super("TEX_TEX","TEX_TEX");

    //            read write
    setProperties(true,true);
    setIsCollection(false);

    setGames("Mall Tycoon");
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
      if (fm.readString(4).equals("_TEX")){
        rating += 50;
        }
      else {
        rating = 0;
        }


      fm.skip(8);


      // 2 - Pixel Format (768/1024)
      short pixelFormat = fm.readShortL();
      if (pixelFormat == 768 || pixelFormat == 1024){
        rating += 5;
        }
      else {
        return 0; // don't want to support it if it doesn't have a valid pixelFormat value,
                  // cause we don't know what format the pixels would be in!
        }


      // 2 - Image Width/Height
      if (check.width(fm.readShortL())){
        rating += 5;
        }

      // 2 - Image Width/Height
      if (check.height(fm.readShortL())){
        rating += 5;
        }

      fm.skip(1);

      // 4 - Image Data Length
      if (check.length(fm.readIntL(),fm.length())){
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


      // 4 - Header (_TEX)
      // 4 - Version (1)
      // 4 - Unknown (3329)
      fm.skip(12);

      // 2 - Pixel Format (768/1024)
      short pixelFormat = fm.readShortL();

      // 2 - Image Width/Height
      short width = fm.readShortL();
      check.width(width);

      // 2 - Image Width/Height
      short height = fm.readShortL();
      check.height(height);

      // 1 - Number Of Mipmaps
      int numMipmaps = fm.readByteU();

      // 4 - Image Data Length
      fm.skip(4);


      int numPixels = width*height;
      int[] pixels = new int[numPixels];

      if (pixelFormat == 768){ // 8-8-8 FORMAT
        // X - Pixels
        for (int p=0;p<numPixels;p++){
          // 1 - Red
          // 1 - Green
          // 1 - Blue
          int r = fm.readByteU();
          int g = fm.readByteU();
          int b = fm.readByteU();

          pixels[p] = ((255 << 24) | (b << 16) | (g << 8) | (r));
          }
        }

      else if (pixelFormat == 1024){ // 8-8-8-8 FORMAT
        // X - Pixels
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
        }


      fm.close();

      Resource resource = new Resource(path,pixels,width,height);
      ColorConverter.convertToPaletted(resource);

      resource.addProperty("DDSNumMipMaps",""+numMipmaps);

      if (pixelFormat == 768){
        resource.addProperty("PixelFormat","8-8-8");
        }
      else if (pixelFormat == 1024){
        resource.addProperty("PixelFormat","8-8-8-8");
        }

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



      int height = resource.getHeight();
      int width = resource.getWidth();


      int numMipMaps = 1;
      String mipMapCount = resource.getProperty("DDSNumMipMaps");
      try {
        numMipMaps = Integer.parseInt(mipMapCount);
        }
      catch (Throwable t){
        }

      if (numMipMaps > 255){
        numMipMaps = 255;
        }


      String pixelFormatString = resource.getProperty("PixelFormat");
      int pixelFormat = 1024;
      if (pixelFormatString.equals("8-8-8")){
        pixelFormat = 768;
        }



      // CALCULATIONS
      int imageDataLength = 0;

      int mipWidth = width;
      int mipHeight = height;

      for (int i=0;i<numMipMaps;i++){
        imageDataLength += (mipWidth*mipHeight);
        mipWidth /= 2;
        mipHeight /= 2;
        }

      if (pixelFormat == 768){
        imageDataLength *= 3; // 3 bytes per pixel
        }
      else {
        imageDataLength *= 4; // 4 bytes per pixel
        }





      WSProgressDialog.setMessage(Language.get("Progress_WritingFiles"));

      // 4 - Header (_TEX)
      fm.writeString("_TEX");

      // 4 - Version (1)
      fm.writeIntL(1);

      // 4 - Unknown (3329)
      fm.writeIntL(3329);

      // 2 - Pixel Format (768/1024)
      fm.writeShortL((short)pixelFormat);

      // 2 - Image Width/Height
      fm.writeShortL((short)width);

      // 2 - Image Width/Height
      fm.writeShortL((short)height);

      // 1 - Number Of Mipmaps
      fm.writeByte(numMipMaps);

      // 4 - Image Data Length
      fm.writeIntL(imageDataLength);



      // X - Pixels
      int[] pixels = resource.getImagePixels();
      int numPixels = pixels.length;

      if (pixelFormat == 768){ // 8-8-8 FORMAT
        writeRGBPixels(fm,pixels,width,height,numMipMaps);
        }
      else {
        writeRGBAPixels(fm,pixels,width,height,numMipMaps);
        }


      fm.close();

      }
    catch (Throwable t){
      logError(t);
      }
    }




/**
**********************************************************************************************
Writes all mipmaps in RGB format
**********************************************************************************************
**/
  public static void writeRGBPixels(FileManipulator fm, int[] pixels, int width, int height, int numMipmaps) throws Exception {

    int numPixels = pixels.length;

    // X - Pixels
    for (int p=0;p<numPixels;p++){
      int pixel = pixels[p];

      // 1 - Red
      // 1 - Green
      // 1 - Blue
      fm.writeByte(pixel);
      fm.writeByte(pixel>>8);
      fm.writeByte(pixel>>16);
      }


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

      numPixels = pixels.length;


      // X - Pixels
      for (int p=0;p<numPixels;p++){
        int pixel = pixels[p];

        // 1 - Red
        // 1 - Green
        // 1 - Blue
        fm.writeByte(pixel);
        fm.writeByte(pixel>>8);
        fm.writeByte(pixel>>16);
        }
      }
    }



/**
**********************************************************************************************
Writes all mipmaps in RGBA format
**********************************************************************************************
**/
  public static void writeRGBAPixels(FileManipulator fm, int[] pixels, int width, int height, int numMipmaps) throws Exception {

    int numPixels = pixels.length;

    // X - Pixels
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

      numPixels = pixels.length;


      // X - Pixels
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
      }
    }



  }