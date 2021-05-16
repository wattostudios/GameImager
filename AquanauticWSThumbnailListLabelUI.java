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

import org.watto.plaf.*;

import org.watto.component.WSPanel;

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

public class AquanauticWSThumbnailListLabelUI extends BasicPanelUI {

  private final static AquanauticWSThumbnailListLabelUI buttonUI = new AquanauticWSThumbnailListLabelUI();


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
  public void installUI(JComponent c) {
    super.installUI(c);
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void uninstallUI(JComponent c) {
    super.uninstallUI(c);
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

    Resource resource = ((WSThumbnailListLabel)c).getResource();
    resource.setThumbnailSize(w-13);

    Image thumbnail = resource.getThumbnail();
    int thumbnailSize = resource.getThumbnailSize();

    int thumbWidth = thumbnail.getWidth(null);
    int thumbHeight = thumbnail.getHeight(null);

    int topPos = (w-thumbHeight)/2;
    int leftPos = (w-thumbWidth)/2;


    AquanauticPainter.paintSolidBackground((Graphics2D)g,x,y,w,h,AquanauticTheme.COLOR_BG);
    if (((WSThumbnailListLabel)c).isSelected()){
      AquanauticPainter.paintCurvedGradient((Graphics2D)g,x,y,w-1,h-1,AquanauticTheme.COLOR_LIGHT,AquanauticTheme.COLOR_MID);
      }
    else {
      //AquanauticPainter.paintCurvedGradient((Graphics2D)g,j*buttonWidth,i*buttonWidth,buttonWidth-2,buttonWidth-2);
      AquanauticPainter.paintSquareSolid((Graphics2D)g,x,y,w-1,h-1,AquanauticTheme.COLOR_BG);
      }


    g.drawImage(thumbnail,leftPos,topPos,null);

    }


  }
