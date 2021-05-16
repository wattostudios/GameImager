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

import org.watto.Settings;
import org.watto.component.WSPanel;
import org.watto.plaf.AquanauticPainter;

import java.awt.*;
import java.awt.image.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;

/**
**********************************************************************************************
  UI for JPanels
**********************************************************************************************
**/

public class AquanauticWSImagePanelUI extends BasicPanelUI {

  private final static AquanauticWSImagePanelUI buttonUI = new AquanauticWSImagePanelUI();

  static Image gridImage = null;


/**
**********************************************************************************************

**********************************************************************************************
**/
  public static ComponentUI createUI(JComponent c) {
    return buttonUI;
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void paint(Graphics g, JComponent c){
    int x = 0;
    int y = 0;
    int w = c.getWidth();
    int h = c.getHeight();

    WSImagePanel panel = (WSImagePanel)c;
    Image image = panel.getImage();
    if (image == null){
      return;
      }

    int imageWidth = panel.getImageWidth();
    int imageHeight = panel.getImageHeight();

    x = (w-imageWidth)/2;
    y = (h-imageHeight)/2;


    if (Settings.getBoolean("PaintTransparentGrid")){
      // draw the transparency grid

      if (gridImage == null){
        generateGridImage();
        }

      Shape origClip = g.getClip();
      Rectangle origClipRect = g.getClipBounds();

      int clipWidth = (int)origClipRect.getWidth();
      int clipHeight = (int)origClipRect.getHeight();
      int clipX = x;
      int clipY = y;


      if (imageWidth < clipWidth){
        clipWidth = imageWidth;
        }
      if (imageHeight < clipHeight){
        clipHeight = imageHeight;
        }

      if (clipX < 0){
        clipX = 0;
        }
      if (clipY < 0){
        clipY = 0;
        }


      g.setClip(clipX,clipY,clipWidth,clipHeight);

      for (int grid_x=0;grid_x<imageWidth;grid_x+=16){
        for (int grid_y=0;grid_y<imageHeight;grid_y+=16){
          g.drawImage(gridImage,x+grid_x,y+grid_y,null);
          }
        }

      g.setClip(origClip);

      }


    // draw the image
    g.drawImage(image,x,y,null);
    }





/**
**********************************************************************************************

**********************************************************************************************
**/
  public void generateGridImage(){
    int color1 = new Color(255,255,255,255).getRGB(); // white
    int color2 = new Color(204,204,204,255).getRGB(); // grey

    int[] pixels = new int[256];

    // Top Line
    int colorPos = 0;
    for (int i=0;i<8;i++){

      // Top Left - white
      for (int j=0;j<8;j++){
        pixels[colorPos] = color1;
        colorPos++;
        }

      // Top Right - grey
      for (int j=0;j<8;j++){
        pixels[colorPos] = color2;
        colorPos++;
        }
      }

    // Bottom Line
    for (int i=0;i<8;i++){

      // Bottom Left - grey
      for (int j=0;j<8;j++){
        pixels[colorPos] = color2;
        colorPos++;
        }

      // Bottom Right - white
      for (int j=0;j<8;j++){
        pixels[colorPos] = color1;
        colorPos++;
        }
      }

    ColorModel model = new DirectColorModel(32,0x00ff0000,0x0000ff00,0x000000ff,0xff000000);
    gridImage = new JLabel().createImage(new MemoryImageSource(16,16,model,pixels,0,16));
    }



  }
