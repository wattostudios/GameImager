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
public class Plugin_TEX_TEX_34 extends ArchivePlugin {


/**
**********************************************************************************************

**********************************************************************************************
**/
  public Plugin_TEX_TEX_34() {
    super("TEX_TEX_34","Lost Planet TEX (DDS Format)");

    //            read write
    setProperties(true,true);

    setGames("Lost Planet");
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

      // 4 - Header
      if (fm.readString(4).equals("TEX" + (char)0)){
        rating += 50;
        }
      else {
        rating = 0;
        }

      // 2 - Unknown (112)
      fm.skip(2);

      // 2 - Unknown (2/34)
      if (fm.readShortL() == 34){
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

      // 4 - Header (TEX + null)
      // 2 - Unknown (112)
      fm.skip(6);

      // 2 - Unknown (2/34)
      int dataFormat = fm.readShortL();

      // 4 - Unknown (257)
      fm.skip(4);

      // 2 - Width
      int width = fm.readShortL();
      check.width(width);

      // 2 - Height
      int height = fm.readShortL();
      check.height(height);

      // 4 - null
      fm.skip(4);

      // 4 - Direct X Format Header (DXT5)
      String fourCC = fm.readString(4);

      // 4 - Unknown (1065353216)
      // 4 - Unknown (1065353216)
      // 4 - Unknown (1065353216)
      // 4 - Unknown (1065353216)
      fm.skip(16);

      // 4 - Header Length (44)
      int headerLength = fm.readIntL();
      check.length(headerLength,fm.length());
      fm.seek(headerLength);


      // X - Image Data
      if (dataFormat == 2){
        Resource resource = readRGBA(fm,width,height);
        resource.setProperty("DDSFormat","RGBA");
        fm.close();
        return new Resource[]{resource};
        }


      Resource resource;

      if (fourCC.equals("DXT3")){
        resource = Plugin_DDS_DDS.readDXT3(fm,width,height);
        resource.setProperty("DDSFormat","DXT3");
        }
      else if (fourCC.equals("DXT5")){
        resource = Plugin_DDS_DDS.readDXT5(fm,width,height);
        resource.setProperty("DDSFormat","DXT5");
        }
      else{
        resource = Plugin_DDS_DDS.readDXT1(fm,width,height);
        resource.setProperty("DDSFormat","DXT1");
        }

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
Reads a RGBA image
**********************************************************************************************
**/
    public static Resource readRGBA(FileManipulator fm, int width, int height) throws Exception {

      // X Bytes - Pixel Data
      int numPixels = width*height;
      int[] pixels = new int[numPixels];


      for (int i=0;i<numPixels;i++){
        // 1 - Red
        // 1 - Green
        // 1 - Blue
        // 1 - null
        int r = fm.readByteU();
        int g = fm.readByteU();
        int b = fm.readByteU();
        fm.skip(1);

        pixels[i] = ((255 << 24) | (b << 16) | (g << 8) | (r));
        }


      Resource resource = new Resource(fm.getFile(),pixels,width,height);
      ColorConverter.convertToPaletted(resource);

      return resource;

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


      int height = resource.getHeight();
      int width = resource.getWidth();
      int[] pixels = resource.getImagePixels();


      String type = resource.getProperty("DDSFormat");
      if (type.equals("DXT1") || type.equals("DXT3") || type.equals("DXT5") || type.equals("RGBA")){
        // the format of the DDS image is found
        }
      else {
        // not a DDS image originally (ie doing a conversion), or not a valid format value, so set to DXT3
        type = "RGBA";
        }


      // 4 - Header (TEX + null)
      fm.writeString("TEX");
      fm.writeByte(0);

      // 2 - Unknown(112)
      fm.writeShortL((short)112);

      // 2 - Format (34=DDS, 2=RGBA)
      if (type.equals("RGBA")){
        fm.writeShortL((short)2);
        }
      else {
        fm.writeShortL((short)34);
        }

      // 4 - Unknown (257)
      fm.writeIntL(257);

      // 2 - Width
      fm.writeShortL((short)width);

      // 2 - Height
      fm.writeShortL((short)height);

      // 4 - null
      fm.writeIntL(0);

      // 4 - Direct X Format Header (DXT5)
      if (type.equals("RGBA")){
        fm.writeIntL(21);
        }
      else {
        fm.writeString(type);
        }

      // 4 - Unknown (1065353216)
      fm.writeIntL(1065353216);

      // 4 - Unknown (1065353216)
      fm.writeIntL(1065353216);

      // 4 - Unknown (1065353216)
      fm.writeIntL(1065353216);

      // 4 - Unknown (1065353216)
      fm.writeIntL(1065353216);

      // 4 - Header Length (44)
      fm.writeIntL(44);


      if (type.equals("RGBA")){
        writeRGBAPixels(fm,pixels,width,height);
        }
      else if (type.equals("DXT1")){
        Plugin_DDS_DDS.writeDXT1Pixels(fm,pixels,width,height);
        }
      else if (type.equals("DXT3")){
        Plugin_DDS_DDS.writeDXT3Pixels(fm,pixels,width,height);
        }
      else if (type.equals("DXT5")){
        Plugin_DDS_DDS.writeDXT5Pixels(fm,pixels,width,height);
        }


      fm.close();

      }
    catch (Throwable t){
      logError(t);
      }
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public static void writeRGBAPixels(FileManipulator fm, int[] pixels, int width, int height) throws Exception {

    // X - Texture Data (DXT1=W*H/2, DXT3/5=W*H, RGBA=W*H*4)
    int numPixels = pixels.length;

    for (int p=0;p<numPixels;p++){
      int pixel = pixels[p];

      // 1 - Red
      fm.writeByte(pixel);

      // 1 - Green
      fm.writeByte(pixel>>8);

      // 1 - Blue
      fm.writeByte(pixel>>16);

      // 1 - Alpha
      fm.writeByte(0);
      }
    }


  }