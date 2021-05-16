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
public class Plugin_PCX extends ArchivePlugin {


/**
**********************************************************************************************

**********************************************************************************************
**/
  public Plugin_PCX() {
    super("PCX","PCX Image");

    //            read write
    setProperties(true,true);
    setIsCollection(false);

    setGames("");
    setExtensions("pcx"); // MUST BE LOWER CASE
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

      // 1 - Header (10)
      if (fm.readByte() == 10){
        rating += 5;
        }

      // 1 - Version
      int version = fm.readByte();
      if (version >= 0 && version <= 5){
        rating += 5;
        }

      // 1 - Encoding (1)
      if (fm.readByte() == 1){
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

      int numColors = 256;
      int[] palette = new int[0];

      // 1 – Manufacturer (10)
      fm.skip(1);

      // 1 – Version (0/2/3/5)
      int version = fm.readByteU();

      // 1 – Encoding (1)
      fm.skip(1);

      // 1 - Bits per pixel
      int bitCount = fm.readByteU();

      // 2 – Xmin
      short xMin = fm.readShortL();

      // 2 – Ymin
      short yMin = fm.readShortL();

      // 2 – Xmax
      int width = fm.readShortL() - xMin + 1;
      check.width(width);

      // 2 - Ymax
      int height = fm.readShortL() - yMin + 1;
      check.height(height);

      // 2 - Horizontal Resolution
      // 2 - Vertical Resolution
      fm.skip(4);

      // 48 – Colormap (16 colors * 3x RGB)
      if (version < 5){
        numColors = 16;

        palette = new int[numColors];
        for (int i=0;i<numColors;i++){
          // 1 - Red
          // 1 - Green
          // 1 - Blue
          int r = fm.readByteU();
          int g = fm.readByteU();
          int b = fm.readByteU();

          palette[i] = ((255 << 24) | (b << 16) | (g << 8) | (r));
          }
        }
      else {
        fm.skip(48);
        }

      // 1 - null
      fm.skip(1);

      // 1 - Number of color planes
      int numPlanes = fm.readByteU();

      // 2 - Number of bytes per scan line per color plane
      int numScanBytes = fm.readShortL();

      // 2 - Palette Type (1 = color/BW, 2 = grayscale)
      // 58 – Padding to 128 bytes
      fm.skip(60);

      if (version == 5 && numPlanes == 1){
        // go to the end of the file and read the color palette
        fm.seek(arcSize-768);

        numColors = 256;

        palette = new int[numColors];
        for (int i=0;i<numColors;i++){
          // 1 - Blue
          // 1 - Green
          // 1 - Red
          int b = fm.readByteU();
          int g = fm.readByteU();
          int r = fm.readByteU();

          palette[i] = ((255 << 24) | (b << 16) | (g << 8) | (r));
          }

        fm.seek(128);
        }



      // X - Pixels
      int numPixels = width*height;
      int[] pixels = new int[numPixels];

      int totalBytes = numPlanes * numScanBytes;
      int[] scanline = new int[totalBytes];

      for (int y=0;y<height;y++){
        int x = 0;

        // build the scan line
        while (x < totalBytes){
          int value = fm.readByteU();
          if ((value&192) == 192){
            int count = (value&63);
            value = fm.readByteU();
            for (int i=0;i<count;i++){
              //pixels[y*width+x+i] = value;
              scanline[x+i] = value;
              }
            x += count;
            }
          else {
            //pixels[y*width+x] = value;
            scanline[x] = value;
            x++;
            }
          }

        // convert into RGB
        if (numPlanes == 3){
          for (int i=0,j=numScanBytes,k=numScanBytes*2;i<numScanBytes;i++,j++,k++){
            scanline[i] = ((255 << 24) | (scanline[i] << 16) | (scanline[j] << 8) | (scanline[k]));
            }
          }

        // copy to the pixel array
        System.arraycopy(scanline,0,pixels,y*width,width);
        }


      fm.close();

      if (numPlanes == 1){
        // Paletted
        return new Resource[]{new Resource(path,pixels,width,height,palette)};
        }
      else if (numPlanes == 3){
        // RGB
        Resource resource = new Resource(path,pixels,width,height);
        ColorConverter.convertToPaletted(resource);

        return new Resource[]{resource};
        }
      else {
        throw new WSPluginException("Unsupported number of planes");
        }

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



      int width = resource.getWidth();
      int height = resource.getHeight();



      // 1 – Manufacturer (10)
      fm.writeByte(10);

      // 1 – Version (0/2/3/5)
      fm.writeByte(5);

      // 1 – Encoding (1)
      fm.writeByte(1);

      // 1 - Bits per pixel
      fm.writeByte(8);

      // 2 – Xmin
      fm.writeShortL((short)0);

      // 2 – Ymin
      fm.writeShortL((short)0);

      // 2 – Xmax
      fm.writeShortL((short)(width-1));

      // 2 - Ymax
      fm.writeShortL((short)(height-1));

      // 2 - Horizontal Resolution
      fm.writeShortL((short)72);

      // 2 - Vertical Resolution
      fm.writeShortL((short)72);


      // 48 – Colormap (16 colors * 3x RGB)
      for (int i=0;i<255;i+=16){
        // 1 - Blue
        // 1 - Green
        // 1 - Red
        fm.writeByte(i);
        fm.writeByte(i);
        fm.writeByte(i);
        }


      // 1 - null
      fm.writeByte(0);

      // 1 - Number of color planes
      fm.writeByte(3);


      int scanWidth = width;
      if (scanWidth%2 == 1){
        scanWidth++;
        }
      int scanHeight = height;
      if (scanHeight%2 == 1){
        scanHeight++;
        }


      // 2 - Number of bytes per scan line per color plane
      fm.writeShortL((short)scanWidth);

      // 2 - Palette Type (1 = color/BW, 2 = grayscale)
      fm.writeShortL((short)1);

      // 58 – Padding to 128 bytes
      fm.write(new byte[58]);



      // X - Pixels
      int[] pixels = resource.getImagePixels();
      int numPixels = pixels.length;

      for (int y=0;y<height;y++){
        ColorSplit[] colors = new ColorSplit[scanWidth];

        // fill the pixels for this line
        for (int x=0;x<width;x++){
          colors[x] = new ColorSplit(pixels[y*width+x]);
          }
        // fill the padding pixels
        for (int x=width;x<scanWidth;x++){
          colors[x] = new ColorSplit(0);
          }


        // red pixels for this scan line
        for (int x=0;x<scanWidth;x++){
          // 1 - Red
          fm.writeByte(colors[x].getRed());
          }
        // green pixels for this scan line
        for (int x=0;x<scanWidth;x++){
          // 1 - Green
          fm.writeByte(colors[x].getGreen());
          }
        // blue pixels for this scan line
        for (int x=0;x<scanWidth;x++){
          // 1 - Blue
          fm.writeByte(colors[x].getBlue());
          }
        }

      // height padding scan lines at the end
      fm.write(new byte[(scanHeight-height)*scanWidth]);


      fm.close();

      }
    catch (Throwable t){
      logError(t);
      }
    }


  }