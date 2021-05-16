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


public class ColorSplitAlpha extends ColorSplit{

  int alpha = 0;

/**
**********************************************************************************************

**********************************************************************************************
**/
  public ColorSplitAlpha(){
    super();
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public ColorSplitAlpha(int color){
    super(color);
    this.alpha = ((color & 0xff000000) >> 24);
    }



/////
//
// METHODS
//
/////



/**
**********************************************************************************************

**********************************************************************************************
**/
  public int getCloseness(ColorSplit otherColor){
    if (mappedColor != null || otherColor.getMappedColor() != null){
      //this color was replaced
      return 999;
      }

    int r = red - otherColor.getRed();
    int g = green - otherColor.getGreen();
    int b = blue - otherColor.getBlue();
    int a = 0;

    if (otherColor instanceof ColorSplitAlpha){
      a = alpha - ((ColorSplitAlpha)otherColor).getAlpha();
      }

    if (r < 0){
      r = 0 - r;
      }
    if (g < 0){
      g = 0 - g;
      }
    if (b < 0){
      b = 0 - b;
      }
    if (a < 0){
      a = 0 - a;
      }

    return r+g+b+a;
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public int getAlpha(){
    return alpha;
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void setAlpha(int alpha){
    this.alpha = alpha;
    }




  }