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
public class Plugin_DXT_TXD extends ArchivePlugin {


/**
**********************************************************************************************

**********************************************************************************************
**/
  public Plugin_DXT_TXD() {
    super("DXT_TXD","DXT_TXD");

    //            read write
    setProperties(true,true);
    setIsCollection(true);

    setGames("Hitman: Codename 47");
    setExtensions("dxt"); // MUST BE LOWER CASE
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
      if (fm.readString(3).equals("TXD") && fm.read() == 0){
        rating += 50;
        }
      else {
        rating = 0;
        }

      fm.skip(4);

      // 4 - Archive Size
      if (fm.readIntL() == fm.length()){
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

      // 4 - Header ("TXD" + null)
      fm.skip(4);

      // 4 - Hash?
      int archiveHash = fm.readIntL();

      // 4 - Archive Size
      fm.skip(4);

      // 4 - Number Of Files
      int numFiles = fm.readIntL();
      check.numFiles(numFiles);


      Resource[] resources = new Resource[numFiles];


      for (int i=0;i<numFiles;i++){
        long nextOffset = fm.getFilePointer();

        // 4 - Header (3TXD)
        fm.skip(4);
        //System.out.println(fm.getFilePointer() + " - " + fm.readString(4));

        // 4 - File Length (including all these header fields)
        int length = fm.readIntL();
        check.length(length,arcSize);
        nextOffset += length;

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
        check.numFiles(numMipmaps);

        // 4 - File Type? (28)
        int hash = fm.readIntL();

        // 4 - Hash?
        fm.skip(4);

        // X - Filename
        // 1 - null Filename Terminator
        String filename = fm.readNullString();

        // 4 - Data Length
        length = fm.readIntL();
        check.length(length,arcSize);

        // X - Image Data (DDS-3 format)
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
        resource.addProperty("Hash",""+hash);
        resource.addProperty("FileID",""+fileID);
        resource.addProperty("Filename",filename);

        resources[i] = resource;

        fm.seek(nextOffset);
        }


      resources[0].addProperty("ArchiveHash",""+archiveHash);

      return resources;

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
      int numFiles = resources.length;

      WSProgressDialog.setMessage(Language.get("Progress_WritingFiles"));


      // 4 - Header ("TXD" + null)
      fm.writeString("TXD");
      fm.writeByte(0);

      // 4 - Hash?
      int archiveHash = 0;
      String archiveHashString = resource.getProperty("ArchiveHash");
      try {
        archiveHash = Integer.parseInt(archiveHashString);
        }
      catch (Throwable t){
        }
      fm.writeIntL(archiveHash);

      // 4 - Archive Size
      fm.writeIntL(0);

      // 4 - Number Of Files
      fm.writeIntL(numFiles);



      for (int i=0;i<numFiles;i++){
        resource = resources[i];

        // get the properties of the file
        int fileID = 1;
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

        String filename = resource.getProperty("Filename");


        int height = resource.getHeight();
        int width = resource.getWidth();
        int[] pixels = resource.getImagePixels();


        // 4 - Header (3TXD)
        if (type.equals("DXT1")){
          fm.writeString("1TXD");
          }
        else {
          fm.writeString("3TXD");
          }

        // 4 - File Length (including all these header fields)
        long lengthPos = fm.getOffset();
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


        if (type.equals("DXT1")){
          writeDXT1Pixels(fm,pixels,width,height,numMipMaps);
          }
        else if (type.equals("DXT3")){
          writeDXT3Pixels(fm,pixels,width,height,numMipMaps);
          }


        // go back and write the file length
        long nextOffset = fm.getFilePointer();
        fm.seek(lengthPos);
        fm.writeIntL((int)(nextOffset-(lengthPos-4)));
        fm.seek(nextOffset);
        }


      // go back and write the file length
      fm.seek(8);
      fm.writeIntL((int)fm.length());

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

      // 4 - Length
      fm.writeIntL(length);

      // X - Texture Data (DXT1=W*H/2, DXT3/5=W*H, RGBA=W*H*4)
      Plugin_DDS_DDS.writeDXT3Pixels(fm,pixels,width,height);
      }
    }


  }