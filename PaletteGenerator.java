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


public class PaletteGenerator {

  static Palette grayscalePalette = null;


/**
**********************************************************************************************

**********************************************************************************************
**/
  public PaletteGenerator(){
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public static int[] getGrayscale(){
    int[] palette = new int[256];
    for (int i=0;i<256;i++){
      palette[i] = ( 255 << 24 | i | i << 8 | i << 16 );
      }
    return palette;
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public static Palette getGrayscalePalette(){
    if (grayscalePalette == null){
      grayscalePalette = new Palette(getGrayscale());
      }
    return grayscalePalette;
    }


  }