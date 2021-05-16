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
public class Plugin_GXA_BMHD extends ArchivePlugin {


/**
**********************************************************************************************

**********************************************************************************************
**/
  public Plugin_GXA_BMHD() {
    super("GXA_BMHD","GXA_BMHD");

    //            read write
    setProperties(true,true);
    setIsCollection(false);

    setGames("Redguard: Elder Scrolls Adventures");
    setExtensions("gxa"); // MUST BE LOWER CASE
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
      if (fm.readString(4).equals("BMHD")){
        rating += 50;
        }
      else {
        rating = 0;
        }

      // 4 - Color Palette Offset (34) (relative to the end of this field)
      if (check.offset(fm.readIntB(),256)){
        rating += 5;
        }

      fm.skip(32);

      // 2 - Number Of Colors
      if (check.numColors(fm.readShortB())){
        rating += 5;
        }

      // 4 - Header
      if (fm.readString(4).equals("BPAL")){
        rating += 5;
        }

      // 4 - Color Palette Length (768)
      if (check.length(fm.readIntB(),769)){
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

      // 4 - Header (BMHD)
      // 4 - Color Palette Offset (34) (relative to the end of this field)
      // 32 - Description (GXlib image conversion          )
      fm.skip(40);

      // 2 - Number Of Colors (256)
      short numColors = fm.readShortB();
      check.numColors(numColors);

      // 4 - Header (BPAL)
      // 4 - Color Palette Length (768)
      fm.skip(8);


      // X - Palette
      int[] palette = new int[numColors];
      for (int i=0;i<numColors;i++){
        // 1 - Red
        // 1 - Green
        // 1 - Blue
        int r = fm.readByteU();
        int g = fm.readByteU();
        int b = fm.readByteU();

        palette[i] = ((255 << 24) | (b << 16) | (g << 8) | (r));
        }


      // 4 - Header (BBMP)
      fm.skip(4);

      // 4 - Pixel Data Length
      int pixelLength = fm.readIntB();
      int width = (int)Math.sqrt(pixelLength);
      check.width(width);
      int height = pixelLength/width;



      // X - Pixels
      int numPixels = width*height;
      int[] pixels = new int[numPixels];
      for (int p=0;p<numPixels;p++){
        // 1 - Color Palette Index
        pixels[p] = fm.readByteU();
        }


      fm.close();

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



      // Reduce to 256 colors, if necessary
      ColorConverter.changeNumColors(resource,256);



      // X - Palette
      int numColors = 256;
      int[] palette = resource.getPalette();




      // 4 - Header (BMHD)
      fm.writeString("BMHD");

      // 4 - Color Palette Offset (34) (relative to the end of this field)
      fm.writeIntB(34);

      // 32 - Description (GXlib image conversion          )
      fm.writeString("GXlib image conversion          ");


      // 2 - Number Of Colors (256)
      fm.writeShortB((short)numColors);

      // 4 - Header (BPAL)
      fm.writeString("BPAL");

      // 4 - Color Palette Length (768)
      fm.writeIntB(numColors*3);


      for (int i=0;i<numColors;i++){
        int color = palette[i];

        // 1 - Red
        // 1 - Green
        // 1 - Blue
        fm.writeByte(color);
        fm.writeByte(color>>8);
        fm.writeByte(color>>16);
        }


      // 4 - Header (BBMP)
      fm.writeString("BBMP");


      int[] pixels = resource.getPixels();
      int numPixels = pixels.length;


      // 4 - Pixel Data Length
      fm.writeIntB(numPixels);


      // X - Pixels
      for (int p=0;p<numPixels;p++){
        // 1 - Color Palette Index
        fm.writeByte((byte)pixels[p]);
        }


      // 4 - Header (END )
      fm.writeString("END ");


      fm.close();

      }
    catch (Throwable t){
      logError(t);
      }
    }


  }