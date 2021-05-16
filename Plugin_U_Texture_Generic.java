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
import org.watto.component.WSPluginException;
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
public class Plugin_U_Texture_Generic extends ArchivePlugin {

  boolean newVersionUsed = true;


/**
**********************************************************************************************

**********************************************************************************************
**/
  public Plugin_U_Texture_Generic() {
    super("U_Texture_Generic","Unreal Engine Paletted Texture Image");

    //            read write
    setProperties(true,true);

    setGames(
             "Adventure Pinball: Forgotten Island",
             "Clive Barker's Undying",
             "Deus Ex",
             "Harry Potter And The Chamber Of Secrets",
             "Mobile Forces",
             "Nerf ArenaBlast",
             "Rune",
             "Star Trek: Deep Space Nine: The Fallen: Maximum Warp",
             "Star Trek: The Next Generation: Klingon Honor Guard",
             "Unreal",
             "Unreal Tournament",
             "Virtual Reality Notre Dame: A Real Time Construction",
             "Wheel Of Time",
             "X-Com Enforcer"
             );
    setExtensions("texture"); // MUST BE LOWER CASE
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

      // 1 - Number Of Mipmaps (9)
      if (fm.readByteU() < 50){
        rating += 5;
        }

      if (! getDirectoryFile(fm.getFile(),"Palette",false).exists()){
        rating--;
        }

      // 4 - Length of something
      //if (check.length(fm.readIntL())){
      //  rating += 5;
      //  }

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
    return read(path,true);
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public Resource[] read(File path, boolean newVersion) {
    this.newVersionUsed = newVersion;

    try {

      FileManipulator fm = new FileManipulator(path,"r");

      long arcSize = fm.length();

      // 1 - Number Of Mipmaps (9)
      int numMipmaps = fm.readByteU();
      check.length(numMipmaps,50);

      // for each mipmap
      for (int i=0;i<numMipmaps;i++){
        if (newVersion){
          // 4 - Unknown
          fm.skip(4);
          }

        // 1-5 - Image Data Length
        int dataLength = (int)readIndex(fm);
        check.length(dataLength,arcSize);

        // X - Texture Data (DXT1=W*H/2, DXT3/5=W*H)
        fm.skip(dataLength);

        // 4 - Width
        int width = fm.readIntL();
        check.width(width);

        // 4 - Height
        int height = fm.readIntL();
        check.height(height);

        //check.equals(width*height,dataLength);

        // 1 - ID
        // 1 - ID
        fm.skip(2);

        if (dataLength > 0){
          fm.seek(fm.getFilePointer() - 10 - dataLength);

          //int format = 1;
          //if (width*height == dataLength){
          //  format = 1;
            Resource resource = loadPaletted(fm,width,height);
            resource.addProperty("DDSNumMipMaps",""+numMipmaps);
          //  }
          //else{
          //  format = 1;
          //  return loadPaletted(fm,width,height);
          //  }

          // see if there is a palette in the same directory, with the same name
          boolean paletteSet = false;
          File paletteFile = getDirectoryFile(path,"Palette",false);

          if (paletteFile != null && paletteFile.exists()){
            Palette palette = new Palette_U_Palette_Generic().readPalette(paletteFile);
            if (palette != null){
              resource.setPalette(palette);
              paletteSet = true;
              }
            }

          if (!paletteSet){
            //resource.setPalette(PaletteGenerator.getGrayscalePalette());
            throw new WSPluginException("Bad color palette");
            }

          return new Resource[]{resource};

          }

        }

      return null;

      }
    catch (Throwable t){

      if (newVersion){
        // try reading as an old texture version (<63)
        return read(path,false);
        }

      logError(t);
      return null;
      }
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
    public Resource loadPaletted(FileManipulator fm, int width, int height) throws Exception {

      // X Bytes - Pixel Data
      int[] data = new int[width*height];

      for (int y=0;y<height;y++){
        for (int x=0;x<width;x++){
          data[x+y*width] = fm.readByteU();
          }
        }

      Resource resource = new Resource(fm.getFile(),data,width,height);

      return resource;

      }


/**
**********************************************************************************************
Reads a single CompactIndex value from the given file
**********************************************************************************************
**/
  public long readIndex(FileManipulator fm) throws Exception {

    boolean[] bitData = new boolean[35];
    java.util.Arrays.fill(bitData,false);
    boolean[] byte1 = fm.readBitsL();

    int bytes = 1;

    boolean negative = false;
    if (byte1[0]){ // positive or negative
      negative = true;
      }

    System.arraycopy(byte1, 2, bitData, 29, 6);
    if (byte1[1]){ // next byte?
      // Read byte 2
      bytes = 2;

      boolean[] byte2 = fm.readBitsL();
      System.arraycopy(byte2, 1, bitData, 22, 7);
      if (byte2[0]){ // next byte?
        // Read byte 3
        bytes = 3;

        boolean[] byte3 = fm.readBitsL();
        System.arraycopy(byte3, 1, bitData, 15, 7);
        if (byte3[0]){ // next byte?
          // Read byte 4
          bytes = 4;

          boolean[] byte4 = fm.readBitsL();
          System.arraycopy(byte4, 1, bitData, 8, 7);
          if (byte4[0]){ // next byte?
            // Read byte 5 (last byte)
            bytes = 5;

            boolean[] byte5 = fm.readBitsL();
            System.arraycopy(byte5, 0, bitData, 0, 8);

            }

          }

        }

      }

    long number = 0;

    //calculate number
    if (bytes >= 5){
      if (bitData[7]){number += 134217728;};
      if (bitData[6]){number += 268435456;};
      if (bitData[5]){number += 536870912;};
      if (bitData[4]){number += 1073727824;};
      if (bitData[3]){number += 2147483648L;};
      if (bitData[2]){number += 4294967296L;};
      if (bitData[1]){number += 8589934592L;};
      if (bitData[0]){number += 17179869184L;};
      }
    if (bytes >= 4){
      if (bitData[14]){number += 1048576;};
      if (bitData[13]){number += 2097152;};
      if (bitData[12]){number += 2794304;};
      if (bitData[11]){number += 8388608;};
      if (bitData[10]){number += 16777216;};
      if (bitData[9]){number += 33554432;};
      if (bitData[8]){number += 67108864;};
      }
    if (bytes >= 3){
      if (bitData[21]){number += 8192;};
      if (bitData[20]){number += 16384;};
      if (bitData[19]){number += 32768;};
      if (bitData[18]){number += 65536;};
      if (bitData[17]){number += 131072;};
      if (bitData[16]){number += 262144;};
      if (bitData[15]){number += 524288;};
      }
    if (bytes >= 2){
      if (bitData[28]){number += 64;};
      if (bitData[27]){number += 128;};
      if (bitData[26]){number += 256;};
      if (bitData[25]){number += 512;};
      if (bitData[24]){number += 1024;};
      if (bitData[23]){number += 2048;};
      if (bitData[22]){number += 4096;};
      }
    if (bytes >= 1){
      if (bitData[34]){number += 1;};
      if (bitData[33]){number += 2;};
      if (bitData[32]){number += 4;};
      if (bitData[31]){number += 8;};
      if (bitData[30]){number += 16;};
      if (bitData[29]){number += 32;};
      }

    if (negative){
      number = 0 - number;
      }

    return number;

    }



/**
**********************************************************************************************
Writes a single CompactIndex value to the given file
**********************************************************************************************
**/
  public static void writeIndex(FileManipulator fm, long value) throws Exception {

    int numBytes = 1;
    if (value > 134217727){
      numBytes = 5;
      }
    else if (value > 1048575){
      numBytes = 4;
      }
    else if (value > 8191){
      numBytes = 3;
      }
    else if (value > 63){
      numBytes = 2;
      }


    int[] bytes = new int[numBytes];


    int byteVal = 0;

    if (numBytes >= 5){
      if (value >= 17179869184L){byteVal |= 128; value -= 17179869184L;};
      if (value >= 8589934592L) {byteVal |= 64;  value -= 8589934592L;};
      if (value >= 4294967296L) {byteVal |= 32;  value -= 4294967296L;};
      if (value >= 2147483648L) {byteVal |= 16;  value -= 2147483648L;};
      if (value >= 1073741824)  {byteVal |= 8;   value -= 1073741824;};
      if (value >= 536870912)   {byteVal |= 4;   value -= 536870912;};
      if (value >= 268435456)   {byteVal |= 2;   value -= 268435456;};
      if (value >= 134217728)   {byteVal |= 1;   value -= 134217728;};
      bytes[4] = byteVal;
      }


    byteVal = 0;

    if (numBytes >= 4){

      if (numBytes > 4){
        // has next byte
        byteVal |= 128;
        };

      if (value >= 67108864){byteVal |= 64; value -= 67108864;};
      if (value >= 33554432){byteVal |= 32; value -= 33554432;};
      if (value >= 16777216){byteVal |= 16; value -= 16777216;};
      if (value >= 8388608) {byteVal |= 8;  value -= 8388608;};
      if (value >= 4194304) {byteVal |= 4;  value -= 4194304;};
      if (value >= 2097152) {byteVal |= 2;  value -= 2097152;};
      if (value >= 1048576) {byteVal |= 1;  value -= 1048576;};

      bytes[3] = byteVal;
      }


    byteVal = 0;

    if (numBytes >= 3){

      if (numBytes > 3){
        // has next byte
        byteVal |= 128;
        };

      if (value >= 524288){byteVal |= 64; value -= 524288;};
      if (value >= 262144){byteVal |= 32; value -= 262144;};
      if (value >= 131072){byteVal |= 16; value -= 131072;};
      if (value >= 65536) {byteVal |= 8;  value -= 65536;};
      if (value >= 32768) {byteVal |= 4;  value -= 32768;};
      if (value >= 16384) {byteVal |= 2;  value -= 16384;};
      if (value >= 8192)  {byteVal |= 1;  value -= 8192;};

      bytes[2] = byteVal;
      }


    byteVal = 0;

    if (numBytes >= 2){

      if (numBytes > 2){
        // has next byte
        byteVal |= 128;
        };

      if (value >= 4096){byteVal |= 64; value -= 4096;};
      if (value >= 2048){byteVal |= 32; value -= 2048;};
      if (value >= 1024){byteVal |= 16; value -= 1024;};
      if (value >= 512) {byteVal |= 8;  value -= 512;};
      if (value >= 256) {byteVal |= 4;  value -= 256;};
      if (value >= 128) {byteVal |= 2;  value -= 128;};
      if (value >= 64)  {byteVal |= 1;  value -= 64;};

      bytes[1] = byteVal;
      }


    byteVal = 0;

    if (numBytes >= 1){

      if (value < 0){
        // negative
        byteVal |= 128;
        };

      if (numBytes > 1){
        // has next byte
        byteVal |= 64;
        };

      if (value >= 32){byteVal |= 32; value -= 32;};
      if (value >= 16){byteVal |= 16; value -= 16;};
      if (value >= 8) {byteVal |= 8;  value -= 8;};
      if (value >= 4) {byteVal |= 4;  value -= 4;};
      if (value >= 2) {byteVal |= 2;  value -= 2;};
      if (value >= 1) {byteVal |= 1;  value -= 1;};

      bytes[0] = byteVal;
      }


    for (int i=0;i<numBytes;i++){
      fm.writeByte(bytes[i]);
      }

    }


/**
**********************************************************************************************
Writes an [archive] File with the contents of the Resources
**********************************************************************************************
**/
  public void write(Resource[] resources, File path) {
    try {
      Resource resource = resources[0];

      // write the Palette
      Palette palette = resource.getPaletteObject();

      File paletteFile = getDirectoryFile(path,"Palette",false);
      new Palette_U_Palette_Generic().writePalette(palette,paletteFile);



      FileManipulator fm = new FileManipulator(path,"rw");

      WSProgressDialog.setMessage(Language.get("Progress_WritingFiles"));

      // write the Texture
      int height = resource.getHeight();
      int width = resource.getWidth();
      int[] pixels = resource.getPixels();

      int numMipMaps = 1;
      String mipMapCount = resource.getProperty("DDSNumMipMaps");
      try {
        numMipMaps = Integer.parseInt(mipMapCount);
        }
      catch (Throwable t){
        }


      // 1 - Number Of Mipmaps (9)
      fm.writeByte(numMipMaps);

      // for each mipmap
      for (int m=0;m<numMipMaps;m++){

        int length = height*width;

        if (newVersionUsed){
          // 4 - Unknown
          fm.writeIntL(length);
          }

        // 1-5 - Image Data Length
        writeIndex(fm,length);

        // X - Texture Data (DXT1=W*H/2, DXT3/5=W*H)
        for (int p=0;p<length;p++){
          fm.writeByte(pixels[p]);
          }

        // 4 - Width
        fm.writeIntL(width);

        // 4 - Height
        fm.writeIntL(height);

        // 1 - ID
        // 1 - ID
        fm.writeByte(2);
        fm.writeByte(2);


        // calculate the next mipmap
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
            newPixels[j*newWidth+y] = pixels[rowStart]; // just use the top-left pixel so we don't have to rebuild the color palette
            }
          }

        pixels = newPixels;
        width = newWidth;
        height = newHeight;
        }


      fm.close();

      }
    catch (Throwable t){
      logError(t);
      }
    }


  }