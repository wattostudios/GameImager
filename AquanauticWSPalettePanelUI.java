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

import org.watto.component.WSPanel;
import org.watto.plaf.AquanauticPainter;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;

/**
**********************************************************************************************
  UI for JPanels
**********************************************************************************************
**/

public class AquanauticWSPalettePanelUI extends BasicPanelUI {

  private final static AquanauticWSPalettePanelUI buttonUI = new AquanauticWSPalettePanelUI();


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

    int[] palette = ((WSPalettePanel)c).getPalette();
    if (palette == null || palette.length == 0){
      return;
      }

    int selectedColor = ((WSPalettePanel)c).getSelectedColor();

    int numColors = palette.length;

    int numCols = w / 15;
    int numRows = numColors / numCols;
    if (numRows == 0 || numColors%numRows != 0){
      numRows++;
      }


    for (int i=0;i<numRows;i++){
      for (int j=0;j<numCols;j++){
        int colorNumber = i*numCols+j;
        if (colorNumber < numColors){
          if (colorNumber == selectedColor){
            AquanauticPainter.paintSolidBackground((Graphics2D)g,j*15,i*15,14,14,new Color(palette[colorNumber]));
            g.setColor(Color.BLACK);
            g.drawRect(j*15,i*15,13,13);
            g.setColor(Color.WHITE);
            g.drawRect(j*15+1,i*15+1,11,11);
            }
          else {
            AquanauticPainter.paintSolidBackground((Graphics2D)g,j*15,i*15,14,14,new Color(palette[colorNumber]));
            }
          }
        }
      }



    }


  }
