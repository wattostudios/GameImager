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
public class Palette_PAL_JASCPAL extends PalettePlugin {


/**
**********************************************************************************************

**********************************************************************************************
**/
  public Palette_PAL_JASCPAL() {
    super("PAL_JASCPAL","PAL_JASCPAL");

    //            read write
    setProperties(true,true);

    setGames("Paint Shop Pro",
             "Pro Pinball: Big Race USA");
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

      // 4=8 - Header
      if (fm.readString(8).equals("JASC-PAL")){
        rating += 50;
        }
      else {
        rating = 0;
        }

      fm.skip(2);

      // 4 - Version
      if (fm.readString(4).equals("0100")){
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


      // 8 - Header (JASC-PAL)
      // 2 - New Line Characters (13,10)
      // X - Version (0100)
      // 2 - New Line Characters (13,10)
      fm.skip(16);

      // X - Number Of Colors (256)
      // 2 - New Line Characters (13,10)
      int numColors = Integer.parseInt(fm.readTerminatedString((byte)13));
      fm.skip(1);


      // X - Palette
      int[] palette = new int[numColors];
      for (int i=0;i<numColors;i++){
        // X - Red
        // 1 - Separator (32)
        int r = Integer.parseInt(fm.readTerminatedString((byte)32));

        // X - Green
        // 1 - Separator (32)
        int g = Integer.parseInt(fm.readTerminatedString((byte)32));

        // X - Blue
        // 2 - New Line Characters (13,10)
        int b = Integer.parseInt(fm.readTerminatedString((byte)13));
        fm.skip(1);


        palette[i] = ((255 << 24) | (r << 16) | (g << 8) | (b));
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


      // 8 - Header (JASC-PAL)
      fm.writeString("JASC-PAL");

      // 2 - New Line Characters (13,10)
      fm.writeByte(13);
      fm.writeByte(10);

      // X - Version (0100)
      fm.writeString("0100");

      // 2 - New Line Characters (13,10)
      fm.writeByte(13);
      fm.writeByte(10);

      // X - Number Of Colors (256)
      fm.writeString(""+numColors);
      // 2 - New Line Characters (13,10)
      fm.writeByte(13);
      fm.writeByte(10);


      // X - Palette
      for (int i=0;i<numColors;i++){
        int color = colors[i];

        int r = ((color | 0x00FF0000) >> 16);
        int g = ((color | 0x0000FF00) >> 8);
        int b = ((color | 0x000000FF));

        // X - Red
        fm.writeString(""+r);

        // 1 - Separator (32)
        fm.writeByte(32);

        // X - Green
        fm.writeString(""+g);

        // 1 - Separator (32)
        fm.writeByte(32);

        // X - Blue
        fm.writeString(""+b);

        // 2 - New Line Characters (13,10)
        fm.writeByte(13);
        fm.writeByte(10);

        }

      fm.close();

      }
    catch (Throwable t){
      logError(t);
      }
    }



  }