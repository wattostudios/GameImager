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
public class Plugin_DDS_DDS extends ArchivePlugin {


/**
**********************************************************************************************

**********************************************************************************************
**/
  public Plugin_DDS_DDS() {
    super("DDS_DDS","DirectX DDS Image");

    //            read write
    setProperties(true,true);

    setGames("");
    setExtensions("dds");
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
      if (fm.readString(4).equals("DDS ")){
        rating += 50;
        }
      else {
        rating = 0;
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

      // 4 - Header (DDS )
      // 4 - Header 1 Length (124)
      // 4 - Flags
      fm.skip(12);

      // 4 - Height
      int height = fm.readIntL();
      check.height(height);

      // 4 - Width
      int width = fm.readIntL();
      check.height(width);

      // 4 - Linear Size
      // 4 - Depth
      fm.skip(8);

      // 4 - Number Of MipMaps
      int mipMapCount = fm.readIntL();
      check.numFiles(mipMapCount);

      // 4 - Alpha Bit Depth
      // 40 - Unknown
      // 4 - Header 2 Length (32)
      // 4 - Flags 2
      fm.skip(52);

      // 4 - Format Code (DXT1 - DXT5)
      String fourCC = fm.readString(4);

      // 4 - Color Bit Count
      int colorBitCount = fm.readIntL();
      // 4 - Red Bit Mask
      // 4 - Green Bit Mask
      // 4 - Blue Bit Mask
      // 4 - Alpha Bit Mask
      // 16 - DDCAPS2
      // 4 - Texture Stage
      fm.seek(128);


      Resource resource;

      if (fourCC.equals("DXT1")) {
        resource = readDXT1(fm,width,height);
        }
      else if (fourCC.equals("DXT3")) {
        resource = readDXT3(fm,width,height);
        }
      else if (fourCC.equals("DXT5")) {
        resource = readDXT5(fm,width,height);
        }
      else {
        if (colorBitCount == 32){
          resource = readRGBA(fm,width,height);
          }
        else {
          resource = readDXT1(fm,width,height);
          }
        }

      fm.close();

      resource.addProperty("DDSFormat",fourCC);
      resource.addProperty("DDSNumMipMaps",""+mipMapCount);

      return new Resource[]{resource};


      }
    catch (Throwable t){
      logError(t);
      return null;
      }
    }


/**
**********************************************************************************************
Reads DXT1 Pixel Data as a Resource
**********************************************************************************************
**/
  public static Resource readDXT1(FileManipulator fm, int width, int height) throws Exception {
    int[] pixels = pixels = readDXT1Pixels(fm,width,height);
    Resource resource = new Resource(fm.getFile(),pixels,width,height);
    ColorConverter.convertToPaletted(resource);
    return resource;
    }


/**
**********************************************************************************************
Reads DXT1 Pixel Data
**********************************************************************************************
**/
  public static int[] readDXT1Pixels(FileManipulator fm, int width, int height) throws Exception {

    // X Bytes - Pixel Data
    int[] pixels = new int[width*height];

    for (int y=0;y<height;y+=4){
      // DXT encodes 4x4 blocks of pixels
      for (int x=0;x<width;x+=4){

        // two 16 bit encoded colors (red 5 bits, green 6 bits, blue 5 bits)
        int c1packed16 = fm.readByteU() | (fm.readByteU() << 8);
        int c2packed16 = fm.readByteU() | (fm.readByteU() << 8);

        // separate the R,G,B values
        int color1r = (c1packed16 >> 8) & 0xF8;
        int color1g = (c1packed16 >> 3) & 0xFC;
        int color1b = (c1packed16 << 3) & 0xF8;

        int color2r = (c2packed16 >> 8) & 0xF8;
        int color2g = (c2packed16 >> 3) & 0xFC;
        int color2b = (c2packed16 << 3) & 0xF8;

        int colors[] = new int[8]; // color table for all possible codes
        // colors 0 and 1 point to the two 16 bit colors we read in
        colors[0] = (color1r << 16) | (color1g << 8) | color1b | 0xFF000000;
        colors[1] = (color2r << 16) | (color2g << 8) | color2b | 0xFF000000;

        // 2/3 Color1, 1/3 color2
        int colorr = (((color1r<<1) + color2r) / 3);// & 0xFF;
        int colorg = (((color1g<<1) + color2g) / 3);// & 0xFF;
        int colorb = (((color1b<<1) + color2b) / 3);// & 0xFF;
        colors[2] = (colorr << 16) | (colorg << 8) | colorb  | 0xFF000000;

        // 2/3 Color2, 1/3 color1
        colorr = (((color2r<<1) + color1r) / 3);// & 0xFF;
        colorg = (((color2g<<1) + color1g) / 3);// & 0xFF;
        colorb = (((color2b<<1) + color1b) / 3);// & 0xFF;
        colors[3] = (colorr << 16) | (colorg << 8) | colorb  | 0xFF000000;

        // read in the color code bits, 16 values, each 2 bits long
        // then look up the color in the color table we built
        //int bits = fm.readByteU() + (fm.readByteU() << 8) + (fm.readByteU() << 16) + (fm.readByteU() << 24);
        int bits = fm.readByteU() | (fm.readByteU() << 8) | (fm.readByteU() << 16) | (fm.readByteU() << 24);

        for (int by=0;by<4;++by){
          for (int bx=0;bx<4;++bx){
            int code = (bits >> (((by<<2)+bx)<<1))&0x3;
            pixels[(y+by)*width+x+bx] = colors[code];
            }
          }
        }
      }

    return pixels;
    }


/**
**********************************************************************************************
Reads DXT3 Pixel Data as a Resource
**********************************************************************************************
**/
  public static Resource readDXT3(FileManipulator fm, int width, int height) throws Exception {
    int[] pixels = pixels = readDXT3Pixels(fm,width,height);
    Resource resource = new Resource(fm.getFile(),pixels,width,height);
    ColorConverter.convertToPaletted(resource);
    return resource;
    }


/**
**********************************************************************************************
Reads DXT3 Pixel Data
**********************************************************************************************
**/
  public static int[] readDXT3Pixels(FileManipulator fm, int width, int height) throws Exception {

    // X Bytes - Pixel Data
    int[] pixels = new int[width*height];

    for (int y=0;y<height;y+=4){
      // DXT encodes 4x4 blocks of pixels
      for (int x=0;x<width;x+=4){

        // skip the alpha data
        fm.skip(8);

        // two 16 bit encoded colors (red 5 bits, green 6 bits, blue 5 bits)
        int c1packed16 = fm.readByteU() | (fm.readByteU() << 8);
        int c2packed16 = fm.readByteU() | (fm.readByteU() << 8);

        // separate the R,G,B values
        int color1r = (c1packed16 >> 8) & 0xF8;
        int color1g = (c1packed16 >> 3) & 0xFC;
        int color1b = (c1packed16 << 3) & 0xF8;

        int color2r = (c2packed16 >> 8) & 0xF8;
        int color2g = (c2packed16 >> 3) & 0xFC;
        int color2b = (c2packed16 << 3) & 0xF8;

        int colors[] = new int[8]; // color table for all possible codes
        // colors 0 and 1 point to the two 16 bit colors we read in
        colors[0] = (color1r << 16) | (color1g << 8) | color1b | 0xFF000000;
        colors[1] = (color2r << 16) | (color2g << 8) | color2b | 0xFF000000;

        // 2/3 Color1, 1/3 color2
        int colorr = (((color1r<<1) + color2r) / 3);// & 0xFF;
        int colorg = (((color1g<<1) + color2g) / 3);// & 0xFF;
        int colorb = (((color1b<<1) + color2b) / 3);// & 0xFF;
        colors[2] = (colorr << 16) | (colorg << 8) | colorb  | 0xFF000000;

        // 2/3 Color2, 1/3 color1
        colorr = (((color2r<<1) + color1r) / 3);// & 0xFF;
        colorg = (((color2g<<1) + color1g) / 3);// & 0xFF;
        colorb = (((color2b<<1) + color1b) / 3);// & 0xFF;
        colors[3] = (colorr << 16) | (colorg << 8) | colorb  | 0xFF000000;

        // read in the color code bits, 16 values, each 2 bits long
        // then look up the color in the color table we built
        //int bits = fm.readByteU() + (fm.readByteU() << 8) + (fm.readByteU() << 16) + (fm.readByteU() << 24);
        int bits = fm.readByteU() | (fm.readByteU() << 8) | (fm.readByteU() << 16) | (fm.readByteU() << 24);

        for (int by=0;by<4;++by){
          for (int bx=0;bx<4;++bx){
            int code = (bits >> (((by<<2)+bx)<<1))&0x3;
            pixels[(y+by)*width+x+bx] = colors[code];
            }
          }
        }
      }

    return pixels;
    }


/**
**********************************************************************************************
Reads DXT5 Pixel Data as a Resource
**********************************************************************************************
**/
  public static Resource readDXT5(FileManipulator fm, int width, int height) throws Exception {
    int[] pixels = pixels = readDXT5Pixels(fm,width,height);
    Resource resource = new Resource(fm.getFile(),pixels,width,height);
    ColorConverter.convertToPaletted(resource);
    return resource;
    }


/**
**********************************************************************************************
Reads DXT5 Pixel Data
**********************************************************************************************
**/
  public static int[] readDXT5Pixels(FileManipulator fm, int width, int height) throws Exception {
    return readDXT3Pixels(fm,width,height);
    }


/**
**********************************************************************************************
Reads a DXT image (FULL - not used, just here for reference)
**********************************************************************************************
**/
    public Resource readDDSUNUSED(FileManipulator fm, int size, int format, int blocksize, int width, int height) throws Exception {

      // X Bytes - Pixel Data
      int[] pixels = new int[width*height];

      for (int y=0;y<height;y+=4){
        // DXT encodes 4x4 blocks of pixels
        for (int x=0;x<width;x+=4){
          // decode the alpha data
          if (format == 3){
            // DXT3 has 64 bits of alpha data, then 64 bits of DXT1 RGB data

            // DXT3 Alpha
            // 16 alpha values are here, one for each pixel, each is 4 bits long
            int abits1 = fm.read() | (fm.read() << 8) | (fm.read() << 16) | (fm.read() << 24);
            int abits2 = fm.read() | (fm.read() << 8) | (fm.read() << 16) | (fm.read() << 24);
            /*
            for (int by=0;by<4;++by){
              for (int bx=0;bx<4;++bx){
                int bits;
                if (by < 2){
                  bits = ((abits1 >> (((by<<2)+bx)<<2))&0xF)<<4;
                  }
                else {
                  bits = ((abits2 >> ((((by-2)<<2)+bx)<<2))&0xF)<<4;
                  }
                imageSourceAlpha[(y+by)*width+x+bx] = (int)(0xFF000000 | (bits << 16) | (bits << 8) | bits);
                }
              }
            */
            }

          else if (format == 5){
            // DXT5 test code Alpha  (ignore this)
            int alpha1 = fm.read();
            int alpha2 = fm.read();
            int abits = fm.read() + (fm.read() << 8) | (fm.read() << 16) | (fm.read() << 24) | (fm.read() << 32) | (fm.read() << 40);

            /*
            for (int by=0;by<4;++by){
              for (int bx=0;bx<4;++bx){
                int code = ((abits >> (((by<<2)+bx)*3))&0xF);
                int value=0;
                if (code == 0){
                  value = alpha1;
                  }
                else if (code == 1){
                  value = alpha2;
                  }
                else if (alpha1>alpha2){
                  if (code == 2){
                    value = (6*alpha1 +   alpha2)/7;
                    }
                  else if (code == 3){
                    value = (5*alpha1 + 2*alpha2)/7;
                    }
                  else if (code == 4){
                    value = (4*alpha1 + 3*alpha2)/7;
                    }
                  else if (code == 5){
                    value = (3*alpha1 + 4*alpha2)/7;
                    }
                  else if (code == 6){
                    value = (2*alpha1 + 5*alpha2)/7;
                    }
                  else if (code == 7){
                    value = (  alpha1 + 6*alpha2)/7;
                    }
                  }
                else {
                  if (code == 2){
                    value = (4*alpha1 + alpha2)/5;
                    }
                  else if (code == 3){
                    value = (3*alpha1 + 2*alpha2)/5;
                    }
                  else if (code == 4){
                    value = (2*alpha1 + 3*alpha2)/5;
                    }
                  else if (code == 5){
                    value = (  alpha1 + 4*alpha2)/5;
                    }
                  else if (code == 6){
                    value = 255;
                    }
                  else if (code == 7){
                    value = 0;
                    }
                  }
                imageSourceAlpha[(y+by)*width+x+bx] = (int)(0xFF000000 | (value << 16) | (value << 8) | value);
                }
              }
            */
            }

          // decode the DXT1/DXT3 RGB data

          // two 16 bit encoded colors (red 5 bits, green 6 bits, blue 5 bits)
          int c1packed16 = fm.readByteU() | (fm.readByteU() << 8);
          int c2packed16 = fm.readByteU() | (fm.readByteU() << 8);

          // separate the R,G,B values
          int color1r = (c1packed16 >> 8) & 0xF8;
          int color1g = (c1packed16 >> 3) & 0xFC;
          int color1b = (c1packed16 << 3) & 0xF8;

          int color2r = (c2packed16 >> 8) & 0xF8;
          int color2g = (c2packed16 >> 3) & 0xFC;
          int color2b = (c2packed16 << 3) & 0xF8;

          int colors[] = new int[8]; // color table for all possible codes
          // colors 0 and 1 point to the two 16 bit colors we read in
          colors[0] = (color1r << 16) | (color1g << 8) | color1b | 0xFF000000;
          colors[1] = (color2r << 16) | (color2g << 8) | color2b  | 0xFF000000;

          // 2/3 Color1, 1/3 color2
          int colorr = (((color1r<<1) + color2r) / 3);// & 0xFF;
          int colorg = (((color1g<<1) + color2g) / 3);// & 0xFF;
          int colorb = (((color1b<<1) + color2b) / 3);// & 0xFF;
          colors[2] = (colorr << 16) | (colorg << 8) | colorb  | 0xFF000000;

          // 2/3 Color2, 1/3 color1
          colorr = (((color2r<<1) + color1r) / 3);// & 0xFF;
          colorg = (((color2g<<1) + color1g) / 3);// & 0xFF;
          colorb = (((color2b<<1) + color1b) / 3);// & 0xFF;
          colors[3] = (colorr << 16) | (colorg << 8) | colorb  | 0xFF000000;

          // DXT1 Alpha test code (not used by DXT3 & DXT5) (Ignore this)
          /*
          colors[4] = colors[0];
          colors[5] = colors[1];

          colorr = ((color1r+color2r)>>1) & 0xFF;
          colorg = ((color1g+color2g)>>1) & 0xFF;
          colorb = ((color1b+color2b)>>1) & 0xFF;
          colors[6] = (colorr << 16) | (colorg << 8) | colorb  | 0xFF000000;
          colors[7] = 0xFF000000; // DXT1 full aplha
          */

          // read in the color code bits, 16 values, each 2 bits long
          // then look up the color in the color table we built
          int bits = fm.readByteU() + (fm.readByteU() << 8) + (fm.readByteU() << 16) + (fm.readByteU() << 24);

          for (int by=0;by<4;++by){
            for (int bx=0;bx<4;++bx){
              int code = (bits >> (((by<<2)+bx)<<1))&0x3;
              //if (colors[0] > colors[1])   // DXT1 Alpha stuff, ignore this
              pixels[(y+by)*width+x+bx] = colors[code];
              //else  // use DXT1 Alpha codes (ignore this)
              //    data[(y+j)*width+x+i] = colors[code+4];
              }
            }
          }
        }


      Resource resource = new Resource(fm.getFile(),pixels,width,height);
      ColorConverter.convertToPaletted(resource);

      return resource;

      }


/**
**********************************************************************************************
Reads RGBA Pixel Data as a Resource
**********************************************************************************************
**/
  public static Resource readRGBA(FileManipulator fm, int width, int height) throws Exception {
    int[] pixels = pixels = readRGBAPixels(fm,width,height);
    Resource resource = new Resource(fm.getFile(),pixels,width,height);
    ColorConverter.convertToPaletted(resource);
    return resource;
    }


/**
**********************************************************************************************
Reads RGBA Pixel Data
**********************************************************************************************
**/
  public static int[] readRGBAPixels(FileManipulator fm, int width, int height) throws Exception {

    int numPixels = width*height;

    // X Bytes - Pixel Data
    int[] pixels = new int[numPixels];

    for (int i=0;i<numPixels;i++){
      //pixels[i] = ((fm.readByte() << 16) | (fm.readByte() << 8) | fm.readByte() | (fm.readByte() << 24));
      pixels[i] = fm.readIntL();
      }

    return pixels;
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



      String type = resource.getProperty("DDSFormat");
      if (type.equals("DXT1") || type.equals("DXT3") || type.equals("DXT5") || type.equals("RGBA")){
        // the format of the DDS image is found
        }
      else {
        // not a DDS image originally (ie doing a conversion), or not a valid format value, so ask the user
        type = WSImagePrompts.askForType(new String[]{"DXT1","DXT3","DXT5","RGBA"});
        }



      if (type.equals("DXT1")){
        writeDXT1(fm,resource);
        }
      else if (type.equals("DXT3")){
        writeDXT3(fm,resource);
        }
      else if (type.equals("DXT5")){
        writeDXT5(fm,resource);
        }
      else if (type.equals("RGBA")){
        writeRGBA(fm,resource);
        }


      fm.close();
      }
    catch (Throwable t){
      logError(t);
      }
    }



/**
**********************************************************************************************
Writes the header and all mipmaps
**********************************************************************************************
**/
  public static void writeDXT1(FileManipulator fm, Resource resource) {
    try {

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

      writeDXT1Header(fm,width,height,numMipMaps);

      writeDXT1Pixels(fm,pixels,width,height,numMipMaps);

      }
    catch (Throwable t){
      logError(t);
      }
    }


/**
**********************************************************************************************
Writes the header and all mipmaps
**********************************************************************************************
**/
  public static void writeDXT3(FileManipulator fm, Resource resource) {
    try {

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

      writeDXT3Header(fm,width,height,numMipMaps);

      writeDXT3Pixels(fm,pixels,width,height,numMipMaps);

      }
    catch (Throwable t){
      logError(t);
      }
    }


/**
**********************************************************************************************
Writes the header and all mipmaps
**********************************************************************************************
**/
  public static void writeDXT5(FileManipulator fm, Resource resource) {
    try {

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

      writeDXT5Header(fm,width,height,numMipMaps);

      writeDXT5Pixels(fm,pixels,width,height,numMipMaps);

      }
    catch (Throwable t){
      logError(t);
      }
    }


/**
**********************************************************************************************
Writes the header and all mipmaps
**********************************************************************************************
**/
  public static void writeRGBA(FileManipulator fm, Resource resource) {
    try {

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

      writeRGBAHeader(fm,width,height,numMipMaps);

      writeRGBAPixels(fm,pixels,width,height,numMipMaps);

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
    writeDXT1Pixels(fm,pixels,width,height);

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
      writeDXT1Pixels(fm,pixels,width,height);
      }
    }


/**
**********************************************************************************************
Writes a single mipmap
**********************************************************************************************
**/
  public static void writeDXT1Pixels(FileManipulator fm, int[] pixels, int width, int height) throws Exception {
    int[] pixelBlock = new int[16];

    int numBlocksWide = width / 4;
    int numBlocksHigh = height / 4;

    for (int i=0;i<numBlocksHigh;i++){
      for (int j=0;j<numBlocksWide;j++){

        // build the array of data
        int position = (i*4*width) + (j*4);
        System.arraycopy(pixels,position,pixelBlock,0,4);
        position += width;
        System.arraycopy(pixels,position,pixelBlock,4,4);
        position += width;
        System.arraycopy(pixels,position,pixelBlock,8,4);
        position += width;
        System.arraycopy(pixels,position,pixelBlock,12,4);


        // split the colors into RGB values
        ColorSplit[] colors = new ColorSplit[16];
        for (int p=0;p<16;p++){
          colors[p] = new ColorSplit(pixelBlock[p]);
          }

        for (int k=0;k<pixelBlock.length;k++){
          pixelBlock[k] = convertTo565(colors[k]);
          colors[k] = convertTo565(pixelBlock[k]);
          }

        int[] extremes = getExtremes(colors);

        int extreme1 = extremes[0];
        int extreme2 = extremes[1];

        if (pixelBlock[extreme1] < pixelBlock[extreme2]){
          int temp = extreme1;
          extreme1 = extreme2;
          extreme2 = temp;
          }

        fm.writeShortL((short) pixelBlock[extreme1]);
        fm.writeShortL((short) pixelBlock[extreme2]);

        long bitmask = computeBitMask(colors, extreme1, extreme2);
        fm.writeIntL((int) bitmask);
        }
      }
    }


/**
**********************************************************************************************
Writes all mipmaps
**********************************************************************************************
**/
  public static void writeDXT3Pixels(FileManipulator fm, int[] pixels, int width, int height, int numMipmaps) throws Exception {
    writeDXT3Pixels(fm,pixels,width,height);

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
      writeDXT3Pixels(fm,pixels,width,height);
      }
    }


/**
**********************************************************************************************
Writes a single mipmap
**********************************************************************************************
**/
  public static void writeDXT3Pixels(FileManipulator fm, int[] pixels, int width, int height) throws Exception {
    int[] pixelBlock = new int[16];

    int numBlocksWide = width / 4;
    int numBlocksHigh = height / 4;

    for (int i=0;i<numBlocksHigh;i++){
      for (int j=0;j<numBlocksWide;j++){

        // build the array of data
        int position = (i*4*width) + (j*4);
        System.arraycopy(pixels,position,pixelBlock,0,4);
        position += width;
        System.arraycopy(pixels,position,pixelBlock,4,4);
        position += width;
        System.arraycopy(pixels,position,pixelBlock,8,4);
        position += width;
        System.arraycopy(pixels,position,pixelBlock,12,4);


        // split the colors into RGB values
        ColorSplit[] colors = new ColorSplit[16];
        for (int p=0;p<16;p++){
          colors[p] = new ColorSplit(pixelBlock[p]);
          }

        // Store the alpha table
        for (int k=0;k<16;k+=2){
          fm.writeByte((byte) ((pixels[k] >>> 24) | (pixels[k + 1] >>> 28)));
          }

        for (int k=0;k<pixelBlock.length;k++){
          pixelBlock[k] = convertTo565(colors[k]);
          colors[k] = convertTo565(pixelBlock[k]);
          }

        int[] extremes = getExtremes(colors);

        int extreme1 = extremes[0];
        int extreme2 = extremes[1];

        if (pixelBlock[extreme1] < pixelBlock[extreme2]){
          int temp = extreme1;
          extreme1 = extreme2;
          extreme2 = temp;
          }

        fm.writeShortL((short) pixelBlock[extreme1]);
        fm.writeShortL((short) pixelBlock[extreme2]);

        long bitmask = computeBitMask(colors, extreme1, extreme2);
        fm.writeIntL((int) bitmask);
        }
      }
    }


/**
**********************************************************************************************
Writes all mipmaps
**********************************************************************************************
**/
  public static void writeDXT5Pixels(FileManipulator fm, int[] pixels, int width, int height, int numMipmaps) throws Exception {
    writeDXT3Pixels(fm,pixels,width,height,numMipmaps);
    }


/**
**********************************************************************************************
Writes a single mipmap
**********************************************************************************************
**/
  public static void writeDXT5Pixels(FileManipulator fm, int[] pixels, int width, int height) throws Exception {
    writeDXT3Pixels(fm,pixels,width,height);
    }


/**
**********************************************************************************************
Writes all mipmaps
**********************************************************************************************
**/
  public static void writeRGBAPixels(FileManipulator fm, int[] pixels, int width, int height, int numMipmaps) throws Exception {
    writeRGBAPixels(fm,pixels,width,height);

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
      writeRGBAPixels(fm,pixels,width,height);
      }
    }


/**
**********************************************************************************************
Writes a single mipmap
**********************************************************************************************
**/
  public static void writeRGBAPixels(FileManipulator fm, int[] pixels, int width, int height) throws Exception {

    int length = width*height;

    // X - Texture Data (DXT1=W*H/2, DXT3/5=W*H, RGBA=W*H*4)
    int numPixels = pixels.length;

    for (int i=0;i<numPixels;i++){
      // 1 - Red
      // 1 - Green
      // 1 - Blue
      // 1 - Alpha
      fm.writeIntL(pixels[i]);
      }
    }


/**
**********************************************************************************************
Writes the header
**********************************************************************************************
**/
  public static void writeDXT1Header(FileManipulator fm, int width, int height, int numMipmaps) {
    writeHeader(fm,"DXT1",width,height,numMipmaps);
    }


/**
**********************************************************************************************
Writes the header
**********************************************************************************************
**/
  public static void writeDXT3Header(FileManipulator fm, int width, int height, int numMipmaps) {
    writeHeader(fm,"DXT3",width,height,numMipmaps);
    }


/**
**********************************************************************************************
Writes the header
**********************************************************************************************
**/
  public static void writeDXT5Header(FileManipulator fm, int width, int height, int numMipmaps) {
    writeHeader(fm,"DXT5",width,height,numMipmaps);
    }


/**
**********************************************************************************************
Writes the header
**********************************************************************************************
**/
  public static void writeHeader(FileManipulator fm, String format, int width, int height, int numMipmaps) {
    int DDSD_CAPS = 0x0001;
    int DDSD_HEIGHT = 0x0002;
    int DDSD_WIDTH = 0x0004;
    int DDSD_PIXELFORMAT = 0x1000;
    int DDSD_MIPMAPCOUNT = 0x20000;
    int DDSD_LINEARSIZE = 0x80000;

    // Write the header

    // 4 - Header (DDS )
    fm.writeString("DDS ");

    // 4 - Header 1 Length (124)
    fm.writeIntL(124);

    // 4 - Flags
    int flag = DDSD_CAPS | DDSD_HEIGHT | DDSD_WIDTH | DDSD_PIXELFORMAT | DDSD_MIPMAPCOUNT | DDSD_LINEARSIZE;
    fm.writeIntL(flag);

    // 4 - Height
    fm.writeIntL(height);

    // 4 - Width
    fm.writeIntL(width);

    // 4 - Linear Size
    fm.writeIntL(width * height / 2);

    // 4 - Depth
    fm.writeIntL(0);

    // 4 - Number Of MipMaps
    fm.writeIntL(numMipmaps);

    // 4 - Alpha Bit Depth
    fm.writeIntL(0);

    // 40 - Unknown
    fm.writeIntL(0);
    fm.writeIntL(0);
    fm.writeIntL(0);
    fm.writeIntL(0);
    fm.writeIntL(0);
    fm.writeIntL(0);
    fm.writeIntL(0);
    fm.writeIntL(0);
    fm.writeIntL(0);
    fm.writeIntL(0);

    // 4 - Header 2 Length (32)
    fm.writeIntL(32);

    // 4 - Flags 2
    fm.writeIntL(0x0004);

    // 4 - Format Code (DXT1 - DXT5)
    fm.writeString(format);

    // 4 - Color Bit Count
    fm.writeIntL(0);

    // 4 - Red Bit Mask
    fm.writeIntL(0);

    // 4 - Green Bit Mask
    fm.writeIntL(0);

    // 4 - Blue Bit Mask
    fm.writeIntL(0);

    // 4 - Alpha Bit Mask
    fm.writeIntL(0);

    // 16 - DDCAPS2
    // 4 - Texture Stage
    // X - Unknown
    fm.writeIntL(0x1000);
    fm.writeIntL(0);
    fm.seek(128);
    }


/**
**********************************************************************************************
Writes the header
**********************************************************************************************
**/
  public static void writeRGBAHeader(FileManipulator fm, int width, int height, int numMipmaps) {
    int DDSD_CAPS = 0x0001;
    int DDSD_HEIGHT = 0x0002;
    int DDSD_WIDTH = 0x0004;
    int DDSD_PIXELFORMAT = 0x1000;
    int DDSD_MIPMAPCOUNT = 0x20000;
    int DDSD_LINEARSIZE = 0x80000;

    // Write the header

    // 4 - Header (DDS )
    fm.writeString("DDS ");

    // 4 - Header 1 Length (124)
    fm.writeIntL(124);

    // 4 - Flags
    int flag = DDSD_CAPS | DDSD_HEIGHT | DDSD_WIDTH | DDSD_PIXELFORMAT | DDSD_MIPMAPCOUNT | DDSD_LINEARSIZE;
    fm.writeIntL(flag);

    // 4 - Height
    fm.writeIntL(height);

    // 4 - Width
    fm.writeIntL(width);

    // 4 - Linear Size
    fm.writeIntL(width * height / 2);

    // 4 - Depth
    fm.writeIntL(0);

    // 4 - Number Of MipMaps
    fm.writeIntL(numMipmaps);

    // 4 - Alpha Bit Depth
    fm.writeIntL(0);

    // 40 - Unknown
    fm.writeIntL(0);
    fm.writeIntL(0);
    fm.writeIntL(0);
    fm.writeIntL(0);
    fm.writeIntL(0);
    fm.writeIntL(0);
    fm.writeIntL(0);
    fm.writeIntL(0);
    fm.writeIntL(0);
    fm.writeIntL(0);

    // 4 - Header 2 Length (32)
    fm.writeIntL(32);

    // 4 - Flags 2
    fm.writeIntL(0x0004);

    // 4 - Format Code (DXT1 - DXT5)
    fm.writeIntL(0);

    // 4 - Color Bit Count
    fm.writeIntL(32);

    // 4 - Red Bit Mask
    fm.writeByte(0);
    fm.writeByte(0);
    fm.writeByte(255);
    fm.writeByte(0);

    // 4 - Green Bit Mask
    fm.writeByte(0);
    fm.writeByte(255);
    fm.writeByte(0);
    fm.writeByte(0);

    // 4 - Blue Bit Mask
    fm.writeByte(255);
    fm.writeByte(0);
    fm.writeByte(0);
    fm.writeByte(0);

    // 4 - Alpha Bit Mask
    fm.writeByte(0);
    fm.writeByte(0);
    fm.writeByte(0);
    fm.writeByte(255);

    // 16 - DDCAPS2
    // 4 - Texture Stage
    // X - Unknown
    fm.writeIntL(0x1000);
    fm.writeIntL(0);
    fm.seek(128);
    }



/**
**********************************************************************************************

**********************************************************************************************
**/
  public static int convertTo565(ColorSplit color){
    int r = color.getRed() >> 3;
    int g = color.getGreen() >> 2;
    int b = color.getBlue() >> 3;
    return r << 11 | g << 5 | b;
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public static ColorSplit convertTo565(int pixel){
    ColorSplit color = new ColorSplit();
    color.setRed  ((int)(((long) pixel) & 0xf800) >> 11);
    color.setGreen((int)(((long) pixel) & 0x07e0) >> 5);
    color.setBlue ((int)(((long) pixel) & 0x001f));
    return color;
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public static int[] getExtremes(ColorSplit[] colors){
    int farthest = 0;

    int extreme1 = 0;
    int extreme2 = 0;

    for (int i=0;i<16-1;i++){
      ColorSplit color = colors[i];

      for (int j=i+1;j<16;j++){
        int d = color.getCloseness(colors[j]);
        if (d > farthest){
          farthest = d;

          extreme1 = i;
          extreme2 = j;
          }
        }
      }

    return new int[]{extreme1,extreme2};
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public static long computeBitMask(ColorSplit[] colors, int extreme1, int extreme2){

    ColorSplit color1 = colors[extreme1];
    ColorSplit color2 = colors[extreme2];

    if (color1.equals(color2)){
      return 0;
      }

    ColorSplit color3 = new ColorSplit();
    color3.setRed  ((2 * color1.getRed()   + color2.getRed()   + 1) / 3);
    color3.setGreen((2 * color1.getGreen() + color2.getGreen() + 1) / 3);
    color3.setBlue ((2 * color1.getBlue()  + color2.getBlue()  + 1) / 3);

    ColorSplit color4 = new ColorSplit();
    color4.setRed  ((color1.getRed()   + 2 * color2.getRed()   + 1) / 3);
    color4.setGreen((color1.getGreen() + 2 * color2.getGreen() + 1) / 3);
    color4.setBlue ((color1.getBlue()  + 2 * color2.getBlue()  + 1) / 3);


    ColorSplit[] colorPoints = new ColorSplit[]{color1,color2,color3,color4};


    long bitmask = 0;
    for (int i=0;i<16;i++){
      int closest = 1000;
      int mask = 0;

      ColorSplit color = colors[i];

      for (int j=0;j<4;j++){
        int d = color.getCloseness(colorPoints[j]);
        if (d < closest){
          closest = d;
          mask = j;
          }
        }
      bitmask |= mask << i * 2;
      }

    return bitmask;
    }


  }