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
public class Plugin_DCT_DC2 extends ArchivePlugin {


/**
**********************************************************************************************

**********************************************************************************************
**/
  public Plugin_DCT_DC2() {
    super("DCT_DC2","Paris Chase DCT");

    //            read write
    setProperties(true,true);

    setGames("Paris Chase");
    setExtensions("dct");
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
      if (fm.readString(3).equals("DC2")){
        rating += 50;
        }
      else {
        rating = 0;
        }

      // 4 - Format
      if (fm.readIntL() == 2){
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

      // 3 - Header (DC2)
      // 4 - Format (2=Image, 0=Model, 3=Camera)
      // 4 - Unknown (1)
      // 4 - null
      // 4 - Unknown
      // 4 - Width/Height
      // 4 - Width/Height
      // 4 - Unknown (1)
      fm.skip(31);

      // 1 - Unknown (0/5/8/9)
      int formatType = fm.readByteU();

      String fourCC = "DXT1";
      if (formatType == 9){
        fourCC = "DXT5";
        }
      else if (formatType == 8){
        fourCC = "DXT1";
        }


      // 4 - Unknown
      fm.skip(4);

      // 4 - Width/Height
      int width = fm.readIntL();
      check.width(width);

      // 4 - Width/Height
      int height = fm.readIntL();
      check.height(height);

      // 4 - Unknown
      fm.skip(4);


      // X - Image Data

      Resource resource;

      if (fourCC.equals("DXT3")){
        resource = Plugin_DDS_DDS.readDXT3(fm,width,height);
        resource.setProperty("DDSFormat","DXT3");
        }
      else if (fourCC.equals("DXT5")){
        resource = Plugin_DDS_DDS.readDXT5(fm,width,height);
        resource.setProperty("DDSFormat","DXT5");
        }
      else{
        resource = Plugin_DDS_DDS.readDXT1(fm,width,height);
        resource.setProperty("DDSFormat","DXT1");
        }

      fm.close();

      return new Resource[]{resource};


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
  public void write(Resource[] resources, File path) {
    try {


      FileManipulator fm = new FileManipulator(path,"rw");

      Resource resource = resources[0];

      WSProgressDialog.setMessage(Language.get("Progress_WritingFiles"));



      int height = resource.getHeight();
      int width = resource.getWidth();
      int[] pixels = resource.getImagePixels();



      String type = resource.getProperty("DDSFormat");
      if (type.equals("DXT1") || type.equals("DXT3") || type.equals("DXT5")){
        // the format of the DDS image is found
        }
      else {
        // not a DDS image originally (ie doing a conversion), or not a valid format value, so set to DXT3
        type = "DXT3";
        }


      // 3 - Header (DC2)
      fm.writeString("DC2");

      // 4 - Format (2=Image, 0=Model, 3=Camera)
      fm.writeIntL(2);

      // 4 - Unknown (1)
      fm.writeIntL(1);

      // 4 - null
      fm.writeIntL(0);

      // 4 - Unknown
      fm.writeIntL(0);

      // 4 - Width/Height
      fm.writeIntL(width);

      // 4 - Width/Height
      fm.writeIntL(height);

      // 4 - Unknown (1)
      fm.writeIntL(1);

      // 1 - Unknown (0/5/8/9)
      if (type.equals("DXT1")){
        fm.writeByte(8);
        }
      else if (type.equals("DXT5")){
        fm.writeByte(9);
        }
      else {
        fm.writeByte(5);
        }

      // 4 - Unknown
      fm.writeIntL(1);

      // 4 - Width/Height
      fm.writeIntL(width);

      // 4 - Width/Height
      fm.writeIntL(height);

      // 4 - Unknown
      fm.writeIntL(0);


      if (type.equals("DXT1")){
        Plugin_DDS_DDS.writeDXT1Pixels(fm,pixels,width,height);
        }
      else if (type.equals("DXT3")){
        Plugin_DDS_DDS.writeDXT3Pixels(fm,pixels,width,height);
        }
      else if (type.equals("DXT5")){
        Plugin_DDS_DDS.writeDXT5Pixels(fm,pixels,width,height);
        }


      fm.close();


      }
    catch (Throwable t){
      logError(t);
      }
    }



  }