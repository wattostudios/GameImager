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
public class Plugin_TXD extends ArchivePlugin {


/**
**********************************************************************************************

**********************************************************************************************
**/
  public Plugin_TXD() {
    super("TXD","TXD");

    //            read write
    setProperties(true,false);
    setIsCollection(false);

    setGames("Sonic Heroes");
    setExtensions("txd"); // MUST BE LOWER CASE
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

      int numFiles = Archive.getMaxFiles();
      int realNumFiles = 0;

      Resource[] resources = new Resource[numFiles];


      long arcSize = fm.length();

      long nextImage = 0;

      while (fm.getFilePointer() < arcSize){
        // 4 - Entry Type
        int entryType = fm.readIntL();

        if (entryType == 1){
          // Entry Holder

          // 4 - Entry Length
          int entryLength = fm.readIntL();

          // 4 - Unknown
          // X - Data
          if (entryLength == 4){
            fm.skip(8);
            }
          else {
            fm.skip(4);

            nextImage = entryLength + fm.getFilePointer();
            check.offset(nextImage);
            }
          }
        else if (entryType == 2){
          // Empty
          }
        else if (entryType == 3){
          // Blank

          // 4 - null
          // 4 - Unknown
          fm.skip(8);
          }
        else if (entryType == 21){
          // Image Descriptor

          // 4 - Image Data Length (including these fields and the entry type field)
          // 4 - Unknown
          fm.skip(8);
          }
        else if (entryType == 22){
          // Archive Header

          // 4 - Archive Length [+12]
          // 4 - Unknown
          fm.skip(8);
          }
        else if (entryType == 8){
          // Image Data

          // 4 - Unknown
          // 64 - Filename (null terminated)
          // 4 - Unknown
          fm.skip(72);

          // 4 - Image Type
          int imageType = fm.readIntL();

          // 2 - Width
          short width = fm.readShortL();
          check.width(width);

          // 2 - Height
          short height = fm.readShortL();
          check.height(height);

          // 4 - Unknown
          imageType = fm.readByteU();
          fm.skip(3);

          //if (imageType == 0){
          if (imageType == 8){
            //System.out.println("Type 8 at " + fm.getFilePointer());
            // Paletted

            // X - Palette
            int numColors = 256;
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

              palette[i] = ((a << 24) | (b << 16) | (g << 8) | (r));
              }

            // 4 - Pixel Data Length
            fm.skip(4);

            // X - Pixels
            int numPixels = width*height;
            int[] pixels = new int[numPixels];
            for (int p=0;p<numPixels;p++){
              // 1 - Color Palette Index
              pixels[p] = fm.readByteU();
              }

            resources[realNumFiles] = new Resource(path,pixels,width,height,palette);
            realNumFiles++;

            }
          //else if (imageType == 1){
          else if (imageType == 32){
            //System.out.println("Type 32 at " + fm.getFilePointer());
            // RGBA

            // 4 - Image Data Length
            fm.skip(4);

            // X - Pixels
            int numPixels = width*height;
            int[] pixels = new int[numPixels];
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

            Resource resource = new Resource(path,pixels,width,height);
            ColorConverter.convertToPaletted(resource);

            resources[realNumFiles] = resource;
            realNumFiles++;

            fm.seek(nextImage);
            }
          else if (imageType == 16){
            //System.out.println("Type 16 at " + fm.getFilePointer());
            // DXT1

            // 4 - Image Data Length
            fm.skip(4);

            // X - Pixels
            Resource resource = Plugin_DDS_DDS.readDXT1(fm,width,height);

            resources[realNumFiles] = resource;
            realNumFiles++;

            fm.seek(nextImage);
            }
          else {
            //System.out.println(fm.getFilePointer());
            throw new WSPluginException("Unknown image type: " + imageType);
            }

          }
        else {
          //System.out.println(fm.getFilePointer());
          throw new WSPluginException("Unknown entry type: " + entryType);
          }
        }


      fm.close();

      resources = resizeResources(resources,realNumFiles);
      return resources;

      }
    catch (Throwable t){
      logError(t);
      return null;
      }
    }



  }