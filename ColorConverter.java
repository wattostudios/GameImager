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

import java.util.Arrays;

public class ColorConverter {


/**
**********************************************************************************************
This reduction method gets the unique colors in the image, then continually replaces colors
with a close match until only 256 colors remain. It does this by comparing pairs of unique
colors in the image, and determining how similar in appearance they are. If they are almost
identical, one of the 2 colors are removed. This process continues until only 256 colors
remain, which becomes the palette.

This method works well because the final color palette will be comprised of the most common-
appearing colors in the image. Colors that are only slightly different have been replaced by
a single color, so the appearance of the final image will be much closer to the original than
the Occurance method. You can kinda think of this method as being functionally similar to the
Occurance method, however in the Occurance method it compares individual colors whereas this
method compares colors that are similar to each other (ie different shades of green are
treated as a single "green" entity in this method.)
**********************************************************************************************
**/
  public static void changeNumColors(Resource resource, int newNumColors){


    int[] palette = resource.getPalette();
    int numColors = palette.length;

    if (numColors == newNumColors){
      return;
      }
    else if (numColors < newNumColors){
      int[] newPalette = new int[newNumColors];
      System.arraycopy(palette,0,newPalette,0,numColors);
      resource.setPalette(newPalette);
      return;
      }


    // split up the colors into RGB
    ColorSplit[] colors = new ColorSplit[numColors];
    for (int i=0;i<numColors;i++){
      colors[i] = new ColorSplit(palette[i]);
      }

    // loop through and merge the closest match while-ever there is > newNumColors unique colors
    int numUnique = numColors;

    for (int closestMatch = 1;closestMatch < numColors && numUnique > newNumColors ;closestMatch++){
      for (int i=0;i<numColors;i++){
        ColorSplit color1 = colors[i];

        for (int j=i+1;j<numColors;j++){
          ColorSplit color2 = colors[j];

          int closeness = color1.getCloseness(color2);
          if (closeness <= closestMatch){

            // we have a best match, so merge it (ie replace color2 with color1)
            colors[j].setMappedColor(colors[i]);
            numUnique--;

            // break out of the 2 FOR loops
            if (numUnique <= newNumColors){
              i = numColors;
              j = numColors;
              }
            }
          }
        }
      }


    // generate the final palette
    int numPaletteColors = 0;

    palette = new int[newNumColors];
    for (int i=0;i<numColors;i++){
      ColorSplit color = colors[i];
      if (color.getMappedColor() == null){
        // this is a color that wasn't changed, so we want to put it in the palette
        palette[numPaletteColors] = color.getColor();
        color.setPaletteIndex(numPaletteColors);
        numPaletteColors++;
        }
      }


    /*
    // now need to convert the pixels into the new palette indexes
    Arrays.sort(colors,0,numColors);

    int[] pixels = resource.getPixels();
    int numPixels = pixels.length;

    // at this point, the colors are sorted by color order, and the getPaletteIndex() contains the palette index.
    for (int i=0;i<numPixels;i++){
      ColorSplit pixel = new ColorSplit(pixels[i]);
      int palettePos = Arrays.binarySearch(colors,0,numColors,pixel);
      int paletteIndex = colors[palettePos].getPaletteIndex();
      pixels[i] = paletteIndex;
      }
    */


    // now need to convert the pixels into the new palette indexes
    int[] mappedIndexes = new int[numColors];
    for (int i=0;i<numColors;i++){
      mappedIndexes[i] = colors[i].getPaletteIndex();
      }

    int[] pixels = resource.getPixels();
    int numPixels = pixels.length;

    for (int i=0;i<numPixels;i++){
      pixels[i] = mappedIndexes[pixels[i]];
      }


    resource.setPalette(palette);
    resource.setPixels(pixels);
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public static void changePaletteMatch(Resource resource, Palette paletteObject){
    int[] palette = paletteObject.getPalette();

    int[] oldPalette = resource.getPalette().clone();

    // split the new palette into the primary colors
    int  numPaletteColors = palette.length;
    int[] pal_r = new int[numPaletteColors];
    int[] pal_g = new int[numPaletteColors];
    int[] pal_b = new int[numPaletteColors];

    for (int i=0;i<numPaletteColors;i++){
      int color = palette[i];

      pal_r[i] = ((color & 0x00ff0000) >> 16);
      pal_g[i] = ((color & 0x0000ff00) >> 8);
      pal_b[i] =  (color & 0x000000ff);
      }

    // map the old palette colors to the new palette colors
    for (int i=0;i<oldPalette.length;i++){
      int color = oldPalette[i];

      int r = ((color & 0x00ff0000) >> 16);
      int g = ((color & 0x0000ff00) >> 8);
      int b =  (color & 0x000000ff);

      // compare the current color to all colors in the palette, to find the closest match
      int bestValue = 1000;
      int bestMap = 0;
      for (int p=0;p<256;p++){
        int cur_r = pal_r[p];
        int cur_g = pal_g[p];
        int cur_b = pal_b[p];

        int currentValue = 0;
        if (r > cur_r){
          currentValue += (r - cur_r);
          }
        else {
          currentValue += (cur_r - r);
          }

        if (g > cur_g){
          currentValue += (g - cur_g);
          }
        else {
          currentValue += (cur_g - g);
          }

        if (b > cur_b){
          currentValue += (b - cur_b);
          }
        else {
          currentValue += (cur_b - b);
          }

        if (currentValue < bestValue){
          bestValue = currentValue;
          bestMap = p;
          }
        }

      oldPalette[i] = bestMap;
      }


    int[] pixels = resource.getPixels();
    int numPixels = pixels.length;
    for (int i=0;i<numPixels;i++){
      pixels[i] = oldPalette[pixels[i]];
      }

    resource.setPixels(pixels);
    resource.setPalette(paletteObject);
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public static void convertToPaletted(Resource resource){

    // Step 1 - build the palette
    int[] pixels = resource.getPixels();
    int numPixels = pixels.length;


    // copy and sort the color values
    int[] palette = new int[numPixels];
    System.arraycopy(pixels,0,palette,0,numPixels);
    Arrays.sort(palette);

    // remove duplicates
    int currentColor = 0;
    int checkColor = 0;

    while (checkColor < numPixels){
      if (palette[currentColor] != palette[checkColor]){
        currentColor++;
        palette[currentColor] = palette[checkColor];
        }
      checkColor++;
      }
    currentColor++;

    if (currentColor < numPixels){
      int[] temp = palette;
      palette = new int[currentColor];
      System.arraycopy(temp,0,palette,0,currentColor);
      }


    // Step 2 - change the colors to indexes
    int numColors = palette.length;

    // the colors are sorted, so we can do binary searches through it.
    for (int i=0;i<numPixels;i++){
      pixels[i] = Arrays.binarySearch(palette,0,numColors,pixels[i]);
      }

    resource.setPalette(palette);
    resource.setPixels(pixels);
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public static void removeAlpha(Resource resource){
    int[] palette = resource.getPalette();

    int numColors = palette.length;
    for (int i=0;i<numColors;i++){
      palette[i] |= 0xff000000;
      }

    resource.setPalette(palette);
    }


/**
**********************************************************************************************
Gets the average color for the given colors
**********************************************************************************************
**/
  public static int getAverage(int ... intColors){
    int numColors = intColors.length;

    int r = 0;
    int g = 0;
    int b = 0;
    int a = 0;

    for (int i=0;i<numColors;i++){
      ColorSplitAlpha color = new ColorSplitAlpha(intColors[i]);
      r += color.getRed();
      g += color.getGreen();
      b += color.getBlue();
      a += color.getAlpha();
      }

    r /= numColors;
    g /= numColors;
    b /= numColors;
    a /= numColors;

    return ((a << 24) | (r << 16) | (g << 8) | b);
    }



  }
