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
public class Plugin_U_Texture_Generic_DDS extends ArchivePlugin {


/**
**********************************************************************************************

**********************************************************************************************
**/
  public Plugin_U_Texture_Generic_DDS() {
    super("U_Texture_Generic_DDS","Unreal Engine DDS Texture Image");

    //            read write
    setProperties(true,true);

    setGames(
             "Devastation",
             "Land Of The Dead: Road To Fiddlers Green",
             "Lemony Snicket's A Series of Unfortunate Events",
             "Pariah",
             "Postal 2",
             "Rainbow Six 3: Raven Shield",
             "Redneck Kentucky and the Next Generation Chickens",
             "Shadow Ops: Red Mercury",
             "Splinter Cell",
             "Splinter Cell: Pandora Tomorrow",
             "Star Wars: Republic Commando",
             "Tribes Vengeance",
             "Unreal 2: The Awakening",
             "Unreal Tournament 2003",
             "Unreal Tournament 2004",
             "Warpath",
             "XIII"
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

      // 1 - Number Of MipMaps
      int numMipmaps = fm.readByteU();
      if (numMipmaps == 0){
        numMipmaps = fm.readByteU(); // Unreal 2 - The Awakening
        }

      if (numMipmaps < 50){
        rating += 5;
        }

      if (getDirectoryFile(fm.getFile(),"Palette",false).exists()){
        rating--;
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

      long arcSize = fm.length();

      // 1 - Number Of Mipmaps (5)
      int numMipmaps = fm.readByteU();
      if (numMipmaps == 0){
        numMipmaps = fm.readByteU(); // Unreal 2 - The Awakening
        }

      // for each mipmap
      for (int i=0;i<numMipmaps;i++){
        // 4 - Unknown
        fm.skip(4);

        // 1-5 - Image Data Length
        int dataLength = (int)readIndex(fm);
        if (dataLength == 0){
          dataLength = (int)readIndex(fm);
          }
        check.length(dataLength,arcSize);

        // X - Texture Data (DXT1=W*H/2, DXT3/5=W*H)
        fm.skip(dataLength);

        // 4 - Width
        int width = fm.readIntL();
        check.width(width);

        // 4 - Height
        int height = fm.readIntL();
        check.height(height);

        // 1 - ID
        // 1 - ID
        fm.skip(2);

        if (dataLength > 0){
          fm.seek(fm.getFilePointer() - 10 - dataLength);

          Resource resource;
          if (width*height == dataLength){
            resource = Plugin_DDS_DDS.readDXT3(fm,width,height);
            resource.setProperty("DDSFormat","DXT3");
            }
          else if (width*height == dataLength/4){
            resource = readRGBA(fm,width,height);
            resource.setProperty("DDSFormat","RGBA");
            }
          else{
            resource = Plugin_DDS_DDS.readDXT1(fm,width,height);
            resource.setProperty("DDSFormat","DXT1");
            }

          resource.addProperty("DDSNumMipMaps",""+numMipmaps);

          fm.close();

          return new Resource[]{resource};

          }

        }

      return null;

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
    public static Resource readRGBA(FileManipulator fm, int width, int height) throws Exception {

      // X Bytes - Pixel Data
      int[] data = new int[width*height];

      for (int y=0;y<height;y++){
        for (int x=0;x<width;x++){
          data[x+y*width] = ((fm.readByte() << 16) | (fm.readByte() << 8) | fm.readByte() | (fm.readByte() << 24));
          }
        }

      Resource resource = new Resource(fm.getFile(),data,width,height);
      ColorConverter.convertToPaletted(resource);

      return resource;

      }


/**
**********************************************************************************************
Reads a single CompactIndex value from the given file
**********************************************************************************************
**/
  public static long readIndex(FileManipulator fm) throws Exception {

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
      if (bitData[4]){number += 1073741824;};
      if (bitData[3]){number += 2147483648L;};
      if (bitData[2]){number += 4294967296L;};
      if (bitData[1]){number += 8589934592L;};
      if (bitData[0]){number += 17179869184L;};
      }
    if (bytes >= 4){
      if (bitData[14]){number += 1048576;};
      if (bitData[13]){number += 2097152;};
      if (bitData[12]){number += 4194304;};
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
      if (type.equals("DXT1") || type.equals("DXT3") || type.equals("DXT5") || type.equals("RGBA")){
        // the format of the DDS image is found
        }
      else {
        // not a DDS image originally (ie doing a conversion), or not a valid format value, so set to DXT3
        type = "DXT3";
        }

      if (type.equals("RGBA")){
        numMipMaps = 1;
        }


      // 4 - Number Of MipMaps
      fm.writeIntL(numMipMaps);


      if (type.equals("RGBA")){
        writeRGBAPixels(fm,pixels,width,height,numMipMaps);
        }
      else if (type.equals("DXT1")){
        writeDXT1Pixels(fm,pixels,width,height,numMipMaps);
        }
      else if (type.equals("DXT3")){
        writeDXT3Pixels(fm,pixels,width,height,numMipMaps);
        }
      else if (type.equals("DXT5")){
        writeDXT5Pixels(fm,pixels,width,height,numMipMaps);
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

    // 4 - Unknown
    fm.writeIntL(length);

    // 4 - Image Data Length
    fm.writeIntL(length);

    // X - Texture Data (DXT1=W*H/2, DXT3/5=W*H, RGBA=W*H*4)
    Plugin_DDS_DDS.writeDXT1Pixels(fm,pixels,width,height);

    // 4 - Width
    fm.writeIntL(width);

    // 4 - Height
    fm.writeIntL(height);

    // 1 - ID
    fm.writeByte(numMipmaps);

    // 1 - ID
    fm.writeByte(numMipmaps);

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

      // 4 - Unknown
      fm.writeIntL(length);

      // 4 - Image Data Length
      fm.writeIntL(length);

      // X - Texture Data (DXT1=W*H/2, DXT3/5=W*H, RGBA=W*H*4)
      Plugin_DDS_DDS.writeDXT1Pixels(fm,pixels,width,height);

      // 4 - Width
      fm.writeIntL(width);

      // 4 - Height
      fm.writeIntL(height);

      // 1 - ID
      fm.writeByte(numMipmaps - m);

      // 1 - ID
      fm.writeByte(numMipmaps - m);
      }
    }


/**
**********************************************************************************************
Writes all mipmaps
**********************************************************************************************
**/
  public static void writeDXT3Pixels(FileManipulator fm, int[] pixels, int width, int height, int numMipmaps) throws Exception {

    int length = width*height;

    // 4 - Unknown
    fm.writeIntL(length);

    // 4 - Image Data Length
    fm.writeIntL(length);

    // X - Texture Data (DXT1=W*H/2, DXT3/5=W*H, RGBA=W*H*4)
    Plugin_DDS_DDS.writeDXT3Pixels(fm,pixels,width,height);

    // 4 - Width
    fm.writeIntL(width);

    // 4 - Height
    fm.writeIntL(height);

    // 1 - ID
    fm.writeByte(numMipmaps);

    // 1 - ID
    fm.writeByte(numMipmaps);

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

      // 4 - Unknown
      fm.writeIntL(length);

      // 4 - Image Data Length
      fm.writeIntL(length);

      // X - Texture Data (DXT1=W*H/2, DXT3/5=W*H, RGBA=W*H*4)
      Plugin_DDS_DDS.writeDXT3Pixels(fm,pixels,width,height);

      // 4 - Width
      fm.writeIntL(width);

      // 4 - Height
      fm.writeIntL(height);

      // 1 - ID
      fm.writeByte(numMipmaps - m);

      // 1 - ID
      fm.writeByte(numMipmaps - m);
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

**********************************************************************************************
**/
  public static void writeRGBAPixels(FileManipulator fm, int[] pixels, int width, int height, int numMipmaps) throws Exception {

    int length = width*height;

    // 4 - Unknown
    fm.writeIntL(length);

    // 4 - Image Data Length
    fm.writeIntL(length);

    // X - Texture Data (DXT1=W*H/2, DXT3/5=W*H, RGBA=W*H*4)
    int numPixels = pixels.length;

    ColorSplitAlpha color;

    for (int i=0;i<numPixels;i++){
      color = new ColorSplitAlpha(pixels[i]);

      // 1 - Red
      color.getRed();

      // 1 - Green
      color.getGreen();

      // 1 - Blue
      color.getBlue();

      // 1 - Alpha
      color.getAlpha();
      }


    // 4 - Width
    fm.writeIntL(width);

    // 4 - Height
    fm.writeIntL(height);

    // 1 - ID
    fm.writeByte(numMipmaps);

    // 1 - ID
    fm.writeByte(numMipmaps);
    }


  }