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
import org.watto.component.WSPluginException;
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
public class Palette_U_Palette_Generic extends PalettePlugin {


/**
**********************************************************************************************

**********************************************************************************************
**/
  public Palette_U_Palette_Generic() {
    super("U_Palette_Generic","U_Palette_Generic");

    //            read write
    setProperties(true,false);

    setGames("Unreal Engine Games");
    setExtensions("palette"); // MUST BE LOWER CASE
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

      // 1-5 - Number Of Colors
      int numColors = (int)readIndex(fm);
      //if (numColors == 0){
      //  numColors = (int)readIndex(fm);
      //  }
      if (check.numColors(numColors)){
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
  public Palette readPalette(File path) {
    try {

      FileManipulator fm = new FileManipulator(path,"r");

      // 1-5 - Number Of Colors
      int numColors = (int)readIndex(fm);
      //if (numColors == 0){
      //  numColors = (int)readIndex(fm);
      //  }
      check.numColors(numColors);

      //check.length(numColors*4,fm.length());
      if (fm.length() - (numColors*4) > 50){
        throw new WSPluginException("NumColors is incorrect");
        }

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
        a = 255;

        palette[i] = ((a << 24) | (r << 16) | (g << 8) | (b));
        }

      fm.close();

      return new Palette(palette);
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
  public void writePalette(Palette palette, File path) {
    try {

      FileManipulator fm = new FileManipulator(path,"rw");

      int[] colors = palette.getPalette();
      int numColors = colors.length;


      // 1-5 - Number Of Colors
      writeIndex(fm,numColors);

      for (int i=0;i<numColors;i++){
        int color = colors[i];

        // 1 - Red
        // 1 - Green
        // 1 - Blue
        // 1 - Alpha
        fm.writeByte(color >> 16);
        fm.writeByte(color >> 8);
        fm.writeByte(color);
        fm.writeByte(color >> 24);
        }

      fm.close();

      }
    catch (Throwable t){
      logError(t);
      }
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


  }