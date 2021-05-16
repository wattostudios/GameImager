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
public class Plugin_RAW_2 extends ArchivePlugin {


/**
**********************************************************************************************

**********************************************************************************************
**/
  public Plugin_RAW_2() {
    super("RAW_2","RAW_2");

    //            read write
    setProperties(true,true);
    setIsCollection(false);

    setGames("Austerlitz");
    setExtensions("raw"); // MUST BE LOWER CASE
    setPlatforms("PC");
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public int getMatchRating(FileManipulator fm) {
    try {

      int rating = 0;

      // NOTE - DON'T WANT THIS TO BE USED GENERICALLY, ONLY
      // WHEN SELECTED TO OPEN USING THIS SPECIFIC PLUGIN!!!
      String extension = fm.getExtension();
      for (int i=0;i<extensions.length;i++){
        if (extension.equals(extensions[i])){
          rating += 25;
          break;
          }
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


      Dimension size = WSImagePrompts.askForDimensions(fm.length()/3);

      // Width
      int width = (int)size.getWidth();
      check.width(width);

      // Height
      int height = (int)size.getHeight();
      check.height(height);



      // X - Pixels
      int numPixels = width*height;
      int[] pixels = new int[numPixels];
      for (int p=0;p<numPixels;p++){
        // 1 - Red
        // 1 - Green
        // 1 - Blue
        int r = fm.readByteU();
        int g = fm.readByteU();
        int b = fm.readByteU();

        pixels[p] = ((255 << 24) | (b << 16) | (g << 8) | (r));
        }



      fm.close();

      Resource resource = new Resource(path,pixels,width,height);
      ColorConverter.convertToPaletted(resource);

      return new Resource[]{resource};
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


      // X - Pixels
      int[] pixels = resource.getImagePixels();
      int numPixels = pixels.length;

      for (int p=0;p<numPixels;p++){
        int pixel = pixels[p];

        // 1 - Red
        // 1 - Green
        // 1 - Blue
        fm.writeByte(pixel);
        fm.writeByte(pixel>>8);
        fm.writeByte(pixel>>16);
        }

      fm.close();

      }
    catch (Throwable t){
      logError(t);
      }
    }


  }