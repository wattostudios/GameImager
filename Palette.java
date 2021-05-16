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


public class Palette {

  int[] palette = new int[0];


/**
**********************************************************************************************

**********************************************************************************************
**/
  public Palette(){
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public Palette(int[] palette){
    this.palette = palette;
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void changeColor(int oldColor, int newColor){
    int numColors = palette.length;
    for (int i=0;i<numColors;i++){
      if (palette[i] == oldColor){
        palette[i] = newColor;
        }
      }
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public Object clone(){
    return new Palette(palette.clone());
    }


/**
**********************************************************************************************
Copies all the values from <i>resource</i> into this resource (ie does a replace without
affecting pointers)
**********************************************************************************************
**/
  public void copyFrom(Palette palette){
    this.palette = palette.getPalette();
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public int getNumColors(){
    return palette.length;
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public int[] getPalette(){
    return palette;
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void setPalette(int[] palette){
    this.palette = palette;
    }


  }