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
public class Palette_ACT extends PalettePlugin {


/**
**********************************************************************************************

**********************************************************************************************
**/
  public Palette_ACT() {
    super("ACT","ACT");

    //            read write
    setProperties(true,true);

    setGames("Generic ACT Palette");
    setExtensions("act"); // MUST BE LOWER CASE
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

      if (check.numColors((short)(fm.length()/3))){
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

      // 0 - Number Of Colors
      int numColors = (short)fm.length()/3;
      check.numColors(numColors);


      // X - Palette
      int[] palette = new int[numColors];
      for (int i=0;i<numColors;i++){
        // 1 - Red
        // 1 - Green
        // 1 - Blue
        int r = fm.readByteU();
        int g = fm.readByteU();
        int b = fm.readByteU();
        int a = 255;

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


      // X - Palette
      for (int i=0;i<numColors;i++){
        int color = colors[i];

        int r = ((color | 0x00FF0000) >> 16);
        int g = ((color | 0x0000FF00) >> 8);
        int b = ((color | 0x000000FF));

        // 1 - Red
        // 1 - Green
        // 1 - Blue
        fm.writeByte(r);
        fm.writeByte(g);
        fm.writeByte(b);
        }

      fm.close();

      }
    catch (Throwable t){
      logError(t);
      }
    }



  }