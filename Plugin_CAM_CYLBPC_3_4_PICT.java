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
public class Plugin_CAM_CYLBPC_3_4_PICT extends ArchivePlugin {


/**
**********************************************************************************************

**********************************************************************************************
**/
  public Plugin_CAM_CYLBPC_3_4_PICT() {
    super("CAM_CYLBPC_3_4_PICT","Playboy: The Mansion PICT Image");

    //            read write
    setProperties(true,true);

    setGames("Playboy: The Mansion");
    setExtensions("pict");
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

      // 4 - Version (1)
      if (fm.readIntL() == 1){
        rating += 5;
        }

      // 2 - Number Of Colors (256)
      if (check.numColors(fm.readShortL(),256)){
        rating += 5;
        }

      // 4 - Unknown (1)
      if (fm.readIntL() == 1){
        rating += 5;
        }

      // 4 - Unknown (8193)
      if (fm.readIntL() == 8193){
        rating += 5;
        }

      // 2 - null
      if (fm.readShortL() == 0){
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

      // 4 - Version (1)
      fm.skip(4);

      // 2 - Number Of Colors (256)
      int numColors = fm.readShortL();
      check.numColors(numColors,256);

      // 4 - Unknown (1)
      // 4 - Unknown (8193)
      // 2 - null
      fm.skip(10);

      // 2 - Width (512)
      int width = fm.readShortL();
      check.width(width);

      // 2 - Height (256)
      int height = fm.readShortL();
      check.height(height);

      // 2 - Bit Depth (8) ie paletted 256 color
      fm.skip(2);


      int[] palette = new int[numColors];

      // read the color palette
      for (int i=0;i<numColors;i++){
        // 1 - Red
        int r = fm.readByteU();

        // 1 - Green
        int g = fm.readByteU();

        // 1 - Blue
        int b = fm.readByteU();

        // 1 - Alpha
        int a = fm.readByteU();

        palette[i] = ((a << 24) | (r) | (g << 8) | (b << 16) );
        }


      int[] pixels = new int[width*height];
      int numPixels = pixels.length-1;
      for (int i=numPixels;i>=0;i--){
        // 1 - Palette Color Index
        pixels[i] = fm.readByteU();
        }


      return new Resource[]{new Resource(path,pixels,width,height,palette)};
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


      // 4 - Version (1)
      fm.writeIntL(1);

      // 2 - Number Of Colors (256)
      fm.writeShortL((short)256);

      // 4 - Unknown (1)
      fm.writeIntL(1);

      // 4 - Unknown (8193)
      fm.writeIntL(8193);

      // 2 - null
      fm.writeShortL((short)0);

      // 2 - Width (512)
      fm.writeShortL((short)resource.getWidth());

      // 2 - Height (256)
      fm.writeShortL((short)resource.getHeight());

      // 2 - Bit Depth (8) ie paletted 256 color
      fm.writeShortL((short)8);


      // Reduce to 256 colors, if necessary
      ColorConverter.changeNumColors(resource,256);


      int numColors = 256;
      int[] palette = resource.getPalette();


      for (int i=0;i<numColors;i++){
        int color = palette[i];

        // 1 - Red
        // 1 - Green
        // 1 - Blue
        // 1 - Alpha
        fm.writeByte((byte)color);
        fm.writeByte((byte)color>>8);
        fm.writeByte((byte)color>>16);
        fm.writeByte((byte)color>>24);
        }


      // X - Pixels
      int[] pixels = resource.getPixels();
      int numPixels = pixels.length;

      for (int i=numPixels-1;i>=0;i--){
        // 1 - Color Palette Index
        fm.writeByte((byte)pixels[i]);
        }

      fm.close();

      }
    catch (Throwable t){
      logError(t);
      }
    }


  }