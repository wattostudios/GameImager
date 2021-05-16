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
public class Plugin_DDT_RTS3 extends ArchivePlugin {


/**
**********************************************************************************************

**********************************************************************************************
**/
  public Plugin_DDT_RTS3() {
    super("DDT_RTS3","DDT_RTS3");

    //            read write
    setProperties(true,true);
    setIsCollection(false);

    setGames("Age Of Empires 3");
    setExtensions("ddt"); // MUST BE LOWER CASE
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
      if (fm.readString(4).equals("RTS3")){
        rating += 50;
        }
      else {
        rating = 0;
        }


      fm.skip(4);

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

      long arcSize = fm.length();

      // 4 - Header (RTS3)
      // 3 - Unknown (DXT1=0,0,4  DXT3/5=1,4,8 or 0,4,8)
      fm.skip(7);

      // 1 - Number Of MipMaps
      int mipMapCount = fm.readByteU();

      // 4 - Width
      int width = fm.readIntL();
      check.width(width);

      // 4 - Height
      int height = fm.readIntL();
      check.height(height);

      // 4 - Image Data Offset
      int offset = fm.readIntL();
      check.offset(offset,arcSize);

      // 4 - Image Data Length
      int length = fm.readIntL();
      check.length(length,arcSize);


      fm.seek(offset);

      Resource resource;

      if (width*height > length){
        resource = Plugin_DDS_DDS.readDXT1(fm,width,height);
        resource.setProperty("DDSFormat","DXT1");
        }
      else {
        resource = Plugin_DDS_DDS.readDXT5(fm,width,height);
        resource.setProperty("DDSFormat","DXT5");
        }

      fm.close();

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
      if (type.equals("DXT1") || type.equals("DXT3") || type.equals("DXT5")){
        // the format of the DDS image is found
        }
      else {
        // not a DDS image originally (ie doing a conversion), or not a valid format value, so set to DXT3
        type = "DXT3";
        }


      // 4 - Header (RTS3)
      fm.writeString("RTS3");

      //3 - Unknown (DXT1=0,0,4  DXT3/5=1,4,8 or 0,4,8)
      if (type.equals("DXT1")){
        fm.writeByte(0);
        fm.writeByte(0);
        fm.writeByte(4);
        }
      else if (type.equals("DXT3") || type.equals("DXT5")){
        fm.writeByte(1);
        fm.writeByte(4);
        fm.writeByte(8);
        }

      // 1 - Number Of MipMaps
      fm.write(numMipMaps);

      // 4 - Image Width
      fm.writeIntL(width);

      // 4 - Image Height
      fm.writeIntL(height);

      int offset = 16 + (numMipMaps*8);
      int calcWidth = width;
      int calcHeight = height;

      // for each mipmap
      for (int i=0;i<numMipMaps;i++){
        // 4 - Image Data Offset
        fm.writeIntL(offset);

        // 4 - Image Data Length
        int length = calcWidth*calcHeight;
        if (type.equals("DXT1")){
          length /= 2;
          }
        fm.writeIntL(length);

        offset += length;
        }


      // X - DDS Image Data
      if (type.equals("DXT1")){
        Plugin_DDS_DDS.writeDXT1Pixels(fm,pixels,width,height,numMipMaps);
        }
      else if (type.equals("DXT3")){
        Plugin_DDS_DDS.writeDXT3Pixels(fm,pixels,width,height,numMipMaps);
        }
      else if (type.equals("DXT5")){
        Plugin_DDS_DDS.writeDXT5Pixels(fm,pixels,width,height,numMipMaps);
        }


      fm.close();


      }
    catch (Throwable t){
      logError(t);
      }
    }


  }