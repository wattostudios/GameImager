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
import javax.imageio.stream.*;
import java.io.*;
import com.sun.image.codec.jpeg.*;

/**
**********************************************************************************************

**********************************************************************************************
**/
public class Plugin_JPEG_JFIF extends ArchivePlugin {


/**
**********************************************************************************************

**********************************************************************************************
**/
  public Plugin_JPEG_JFIF() {
    super("JPEG_JFIF","JPEG Image");

    //         read write
    setProperties(true,true);

    setGames("");
    setExtensions("jpg","jpeg","jif","jfif");
    setPlatforms("PC");
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public int getMatchRating(FileManipulator fm) {
    try {

      int rating = 0;

      for (int i=0;i<extensions.length;i++){
        if (fm.getExtension().equals(extensions[i])){
          rating += 25;
          i = extensions.length;
          }
        }

      fm.skip(6);

      // header
      if (fm.readString(4).equals("JFIF")){
        rating += 50;
        }
      else {
        rating = 0;
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

      ImageIcon icon = new ImageIcon(path.getAbsolutePath());

      int width = icon.getIconWidth();
      int height = icon.getIconHeight();
      Image image = icon.getImage();

      // now get the pixels and palette of the image
      PixelGrabber pixelGrabber=new PixelGrabber(image,0,0,width,height,false);
      pixelGrabber.grabPixels();

      // get the pixels, and convert them to positive values in an int[] array
      int[] pixels = (int[])pixelGrabber.getPixels();

      Resource resource = new Resource(path,pixels,width,height);
      ColorConverter.convertToPaletted(resource);
      ColorConverter.removeAlpha(resource);

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

      Resource resource = resources[0];

      FileManipulator fm = new FileManipulator(path,"rw");

      WSProgressDialog.setMessage(Language.get("Progress_WritingFiles"));

      int imageWidth = resource.getWidth();
      int imageHeight = resource.getHeight();

      if (imageWidth <= 0 || imageHeight <= 0){
        return;
        }

      OutputStream out = new FileManipulatorOutputStream(fm);

      BufferedImage bufImage = new BufferedImage(imageWidth,imageHeight,BufferedImage.TYPE_INT_RGB);
      Graphics g = bufImage.getGraphics();
      g.drawImage(resource.getImage(), 0, 0, null);

      JPEGImageEncoder jencoder = JPEGCodec.createJPEGEncoder(out);
      jencoder.encode(bufImage);

      out.flush();
      out.close();

      g.dispose();


      fm.close();

      }
    catch (Throwable t){
      logError(t);
      }
    }


  }