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
public class Plugin_3TXD_3TXD extends ArchivePlugin {


/**
**********************************************************************************************

**********************************************************************************************
**/
  public Plugin_3TXD_3TXD() {
    super("3TXD_3TXD","3TXD_3TXD");

    //            read write
    setProperties(true,true);
    setIsCollection(false);

    setGames("Hitman: Codename 47");
    setExtensions("3txd"); // MUST BE LOWER CASE
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
      if (fm.readString(4).equals("3TXD")){
        rating += 50;
        }
      else {
        rating = 0;
        }

      // 4 - File Length
      if (fm.readIntL() == fm.length()){
        rating += 5;
        }

      fm.skip(4);

      // 2 - Image Height
      if (check.height(fm.readShortL())){
        rating += 5;
        }

      // 2 - Image Width
      if (check.width(fm.readShortL())){
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

      // 4 - Header (3TXD)
      // 4 - File Length (including all these header fields)
      fm.skip(8);

      // 4 - File ID
      int fileID = fm.readIntL();

      // 2 - Image Height
      short height = fm.readShortL();
      check.height(height);

      // 2 - Image Width
      short width = fm.readShortL();
      check.width(width);


      // 4 - Number Of Mipmaps
      int numMipmaps = fm.readIntL();

      // 4 - File Type (28)
      fm.skip(4);

      // 4 - Hash?
      int hash = fm.readIntL();

      // X - Filename
      // 1 - null Filename Terminator
      String filename = fm.readNullString();


      // 4 - Data Length
      fm.skip(4);


      // X - Pixels
      Resource resource = Plugin_DDS_DDS.readDXT3(fm,width,height);


      fm.close();

      //ColorConverter.convertToPaletted(resource);

      resource.addProperty("DDSNumMipMaps",""+numMipmaps);
      resource.setProperty("Hash",""+hash);
      resource.setProperty("FileID",""+fileID);
      resource.setProperty("Filename",filename);

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

      WSProgressDialog.setMessage(Language.get("Progress_WritingFiles"));



      int width = resource.getWidth();
      int height = resource.getHeight();



      int numMipMaps = 1;
      String mipMapCount = resource.getProperty("DDSNumMipMaps");
      try {
        numMipMaps = Integer.parseInt(mipMapCount);
        }
      catch (Throwable t){
        }

      int fileID = 0;
      String fileIDString = resource.getProperty("FileID");
      try {
        fileID = Integer.parseInt(fileIDString);
        }
      catch (Throwable t){
        }

      int hash = 0;
      String hashString = resource.getProperty("Hash");
      try {
        hash = Integer.parseInt(hashString);
        }
      catch (Throwable t){
        }

      String filename = resource.getProperty("Filename");



      // 4 - Header (3TXD)
      fm.writeString("3TXD");

      // 4 - File Length (including all these header fields)
      fm.writeIntL(0);

      // 4 - File ID
      fm.writeIntL(fileID);

      // 2 - Image Height
      fm.writeShortL((short)height);

      // 2 - Image Width
      fm.writeShortL((short)width);

      // 4 - Number Of Mipmaps
      fm.writeIntL(numMipMaps);

      // 4 - File Type? (28)
      fm.writeIntL(28);

      // 4 - Hash?
      fm.writeIntL(hash);

      // X - Filename
      // 1 - null Filename Terminator
      fm.writeString(filename);
      fm.writeByte(0);


      int[] pixels = resource.getImagePixels();

      // X - Mipmaps
      for (int i=0;i<numMipMaps;i++){

        int numPixels = pixels.length;

        // 4 - Data Length
        fm.writeIntL(numPixels);

        // X - Pixels
        Plugin_DDS_DDS.writeDXT3Pixels(fm,pixels,width,height);

        // prepare the next mipmap
        if (i != numMipMaps-1){
          int newWidth = width/2;
          int newHeight = height/2;

          int[] newPixels = new int[newWidth*newHeight];
          for (int h=0,j=0;h<height;h+=2,j++){
            for (int w=0,x=0;w<width;w+=2,x++){
              // average of the 4 pixels
              int rowStart1 = (h*width)+w;
              int rowStart2 = ((h+1)*width)+w;
              newPixels[j*newWidth+x] = ColorConverter.getAverage(pixels[rowStart1],pixels[rowStart1+1],pixels[rowStart2],pixels[rowStart2+1]);
              }
            }

          pixels = newPixels;
          width = newWidth;
          height = newHeight;
          }

        }


      // go back and write the file length
      fm.seek(4);
      fm.writeIntL((int)fm.length());


      fm.close();

      }
    catch (Throwable t){
      logError(t);
      }
    }


  }