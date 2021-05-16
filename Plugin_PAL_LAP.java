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
public class Plugin_PAL_LAP extends ArchivePlugin {


/**
**********************************************************************************************

**********************************************************************************************
**/
  public Plugin_PAL_LAP() {
    super("PAL_LAP","PAL_LAP");

    //            read write
    setProperties(true,false);
    setIsCollection(true);

    setGames("Hitman: Codename 47");
    setExtensions("pal"); // MUST BE LOWER CASE
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
      if (fm.readString(4).equals("LAP" + (char)0)){
        rating += 50;
        }
      else {
        rating = 0;
        }

      fm.skip(4);

      // 4 - Archive Length
      if (fm.readIntL() == fm.length()){
        rating += 5;
        }

      // 4 - Number Of Files
      if (check.numFiles(fm.readIntL())){
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

      // 4 - Header ("LAP" + null)
      // 4 - Hash?
      // 4 - Archive Size
      fm.skip(12);

      // 4 - Number Of Files?
      int numFiles = fm.readIntL();
      check.numFiles(numFiles);

      Resource[] resources = new Resource[numFiles];

      for (int i=0;i<numFiles;i++){
        // 4 - Header (ABGR)
        String format = fm.readString(4);

        // 4 - File Length (including all these header fields)
        int length = fm.readIntL() - 29;

        // 4 - File ID
        fm.skip(4);

        // 2 - Image Height
        short height = fm.readShortL();
        check.height(height);

        // 2 - Image Width
        short width = fm.readShortL();
        check.width(width);

        // 4 - Number Of Mipmaps
        int numMipmaps = fm.readIntL();

        // 4 - File Type? (28)
        // 4 - Hash?
        fm.skip(8);

        // X - Filename
        // 1 - null Filename Terminator
        String filename = fm.readNullString();

        length -= filename.length();
        check.length(length,arcSize);

        long nextOffset = fm.getFilePointer() + length;

        // 4 - Data Length
        fm.skip(4);


        if (format.equals("ABGR")){
          // X - Pixels
          int numPixels = width*height;
          int[] pixels = new int[numPixels];
          for (int p=0;p<numPixels;p++){
            // 1 - Red
            // 1 - Green
            // 1 - Blue
            // 1 - Alpha
            int b = fm.readByteU();
            int g = fm.readByteU();
            int r = fm.readByteU();
            int a = fm.readByteU();

            pixels[p] = ((a << 24) | (b << 16) | (g << 8) | (r));
            }

          Resource resource = new Resource(path,pixels,width,height);
          ColorConverter.convertToPaletted(resource);

          resources[i] = resource;
          }
        else if (format.equals("NLAP")){
          // X - Pixels
          int numPixels = width*height;
          int[] pixels = new int[numPixels];
          for (int p=0;p<numPixels;p++){
            // 1 - Color Palette Index
            pixels[p] = fm.readByteU();
            }

          //Palette palette = PaletteGenerator.getGrayscalePalette();

          // skip the other mipmaps
          for (int m=1;m<numMipmaps;m++){
            // 4 - Data Length
            int dataLength = fm.readIntL();
            check.length(dataLength,arcSize);

            // X - Pixels
            fm.skip(dataLength);
            }


          // 4 - Number Of Colors
          int numColors = fm.readIntL();

          // X - Palette
          int[] palette = new int[numColors];
          for (int p=0;p<numColors;p++){
            // 1 - Red
            // 1 - Green
            // 1 - Blue
            // 1 - Alpha
            int b = fm.readByteU();
            int g = fm.readByteU();
            int r = fm.readByteU();
            int a = fm.readByteU();

            palette[p] = ((a << 24) | (b << 16) | (g << 8) | (r));
            }

          resources[i] = new Resource(path,pixels,width,height,palette);
          }
        else {
          throw new WSPluginException("Unknown image type: " + format);
          }

        // skip the other mipmaps and just go to the next image
        fm.seek(nextOffset);
        }

      fm.close();

      return resources;

      }
    catch (Throwable t){
      logError(t);
      return null;
      }
    }


  }