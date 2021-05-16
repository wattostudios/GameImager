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

import org.watto.ErrorLogger;
import org.watto.Settings;

import java.io.File;
import javax.swing.JLabel;
import java.awt.Image;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.MemoryImageSource;

public class Resource {

  File sourcePath;
  ArchivePlugin readPlugin;

  int[] pixels = new int[0];
  Palette palette = new Palette();
  int width = 0;
  int height = 0;

  Image thumbnail = null;
  int thumbnailSize = 0;

  Resource_Property[] properties = null;


/**
**********************************************************************************************
Palette to be set later
**********************************************************************************************
**/
  public Resource(File sourcePath, int[] pixels, int width, int height){
    this.sourcePath = sourcePath;
    this.pixels = pixels;
    this.width = width;
    this.height = height;
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public Resource(File sourcePath, int[] pixels, int width, int height, int[] palette){
    this.sourcePath = sourcePath;
    this.pixels = pixels;
    this.width = width;
    this.height = height;
    setPalette(new Palette(palette));
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public Resource(File sourcePath, int[] pixels, int width, int height, Palette palette){
    this.sourcePath = sourcePath;
    this.pixels = pixels;
    this.width = width;
    this.height = height;
    setPalette(palette);
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void addProperty(String code, String value){
    Resource_Property property = new Resource_Property(code,value);

    if (properties == null){
      // add property to new array
      properties = new Resource_Property[]{property};
      return;
      }

    int numProperties = properties.length;

    // expand array then add property
    Resource_Property[] temp = properties;
    properties = new Resource_Property[numProperties+1];
    System.arraycopy(temp,0,properties,0,numProperties);
    properties[numProperties] = property;
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void changeColor(int oldColor, int newColor){
    palette.changeColor(oldColor,newColor);

    if (Settings.getBoolean("UpdateThumbnails")){
      thumbnail = null;
      }
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public Object clone(){
    Palette clonedPalette = (Palette)palette.clone();
    PaletteManager.addPalette(clonedPalette,false);

    Resource resource = new Resource(sourcePath,pixels.clone(),width,height,clonedPalette);
    resource.setReadPlugin(readPlugin);
    return resource;
    }


/**
**********************************************************************************************
Copies all the values from <i>resource</i> into this resource (ie does a replace without
affecting pointers)
**********************************************************************************************
**/
  public void copyFrom(Resource resource){
    this.pixels = resource.getPixels();
    this.palette = resource.getPaletteObject();
    this.width = resource.getWidth();
    this.height = resource.getHeight();
    this.readPlugin = resource.getReadPlugin();
    thumbnail = null;
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public int getHeight(){
    return height;
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public Image getImage(){
    ColorModel model = new DirectColorModel(32,0x00ff0000,0x0000ff00,0x000000ff,0xff000000);
    return new JLabel().createImage(new MemoryImageSource(width,height,model,getImagePixels(),0,width));
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public int[] getImagePixels(){
    int[] palette = this.palette.getPalette();

    int numPixels = pixels.length;
    int[] rgb = new int[numPixels];
    for (int i=0;i<numPixels;i++){
      rgb[i] = palette[pixels[i]];
      }
    return rgb;
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public int getNumColors(){
    return palette.getNumColors();
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public int getNumPixels(){
    return pixels.length;
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public int[] getPalette(){
    return palette.getPalette();
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public Palette getPaletteObject(){
    return palette;
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public int[] getPixels(){
    return pixels;
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public String getProperty(String code){
    if (properties == null){
      return "";
      }

    int numProperties = properties.length;

    for (int i=0;i<numProperties;i++){
      if (properties[i].getCode().equals(code)){
        // found
        return properties[i].getValue();
        }
      }

    return "";
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public ArchivePlugin getReadPlugin(){
    return readPlugin;
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public File getSource(){
    return sourcePath;
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public Image getThumbnail(){
    if (thumbnail == null){
      Image image = getImage();
      int thumbWidth = image.getWidth(null);
      int thumbHeight = image.getHeight(null);

      if (thumbWidth > thumbHeight){
        thumbnail = image.getScaledInstance(thumbnailSize,-1,Image.SCALE_FAST);
        }
      else {
        thumbnail = image.getScaledInstance(-1,thumbnailSize,Image.SCALE_FAST);
        }
      }
    return thumbnail;
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public int getThumbnailSize(){
    return thumbnailSize;
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public int getWidth(){
    return width;
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void setHeight(int height){
    this.height = height;
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void setPalette(int[] palette){
    if (this.palette.getNumColors() == 0){
      // needs the palette to be added to PaletteManager
      setPalette(new Palette(palette));
      }
    else {
      // just update the current palette
      this.palette.setPalette(palette);
      if (Settings.getBoolean("UpdateThumbnails")){
        thumbnail = null;
        }
      }
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void setPalette(Palette palette){
    this.palette = palette;
    PaletteManager.addPalette(palette);

    if (Settings.getBoolean("UpdateThumbnails")){
      thumbnail = null;
      }
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void setPixels(int[] pixels){
    this.pixels = pixels;
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void setProperty(String code, String value){
    Resource_Property property = new Resource_Property(code,value);

    if (properties == null){
      // add property to new array
      properties = new Resource_Property[]{property};
      return;
      }

    int numProperties = properties.length;

    for (int i=0;i<numProperties;i++){
      if (properties[i].getCode().equals(code)){
        // found, so replace
        properties[i] = property;
        return;
        }
      }

    // expand array then add property
    Resource_Property[] temp = properties;
    properties = new Resource_Property[numProperties+1];
    System.arraycopy(temp,0,properties,0,numProperties);
    properties[numProperties] = property;
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void setReadPlugin(ArchivePlugin readPlugin){
    this.readPlugin = readPlugin;
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void setSource(File sourcePath){
    this.sourcePath = sourcePath;
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void setThumbnail(Image thumbnail){
    this.thumbnail = thumbnail;
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void setThumbnailSize(int size){
    if (thumbnailSize != size){
      setThumbnail(null);
      }
    thumbnailSize = size;
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void setWidth(int width){
    this.width = width;
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public String toString(){
    return sourcePath.getName();
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void logError(Throwable t){
    ErrorLogger.log(t);
    }


  }