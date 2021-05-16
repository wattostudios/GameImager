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
import org.watto.Settings;
import org.watto.component.*;
import org.watto.event.*;
import org.watto.xml.*;

import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.plaf.PanelUI;

/**
**********************************************************************************************
A ExtendedTemplate
**********************************************************************************************
**/
public class WSPalettePanel extends WSPanel implements WSClickableInterface {

  int[] palette = new int[0];
  int selectedColor = -1;

  WSGradientColorChooser colorChooser;
  WSPopupMenu popup;


/**
**********************************************************************************************

**********************************************************************************************
**/
  public WSPalettePanel(){
    super();
    }


/**
**********************************************************************************************
Constructor to construct the component from an XMLNode <i>tree</i>
@param node the XMLNode describing this component
**********************************************************************************************
**/
  public WSPalettePanel(XMLNode node){
    super(node);
    setOpaque(false);
    }



///////////////
//
// Class-Specific Methods
//
///////////////


/**
**********************************************************************************************
Build this object from the <i>node</i>
@param node the XML node that indicates how to build this object
**********************************************************************************************
**/
  public void buildObject(XMLNode node){
    super.buildObject(node);
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void changeSelectedColor(){
    if (palette == null || palette.length == 0){
      return;
      }

    Resource resource = ((WSFileListPanelHolder)WSRepository.get("FileListPanelHolder")).getCurrentResource();
    if (resource == null){
      return;
      }

    colorChooser = (WSGradientColorChooser)WSRepository.get("SidePanel_Palette_GradientPanel");
    colorChooser.setColor(new Color(palette[selectedColor]));
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void changeSelectedColor(Color newColor){
    Resource resource = ((WSFileListPanelHolder)WSRepository.get("FileListPanelHolder")).getCurrentResource();
    if (resource == null){
      return;
      }

    int newColorInt = newColor.getRGB();
    resource.changeColor(palette[selectedColor],newColorInt);
    palette[selectedColor] = newColorInt;

    setSelectedColor(selectedColor);
    ((WSFileListPanelHolder)WSRepository.get("FileListPanelHolder")).reload();
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public Color getColorAtPoint(int xPos, int yPos){

    int numCols = getWidth() / 15;
    int numRows = yPos/15;

    int paletteNum = xPos/15 + numRows*numCols;

    if (palette == null || palette.length == 0){
      return null;
      }
    else if (paletteNum < 0){
      return null;
      }
    else if (paletteNum >= palette.length){
      return null;
      }

    return new Color(palette[paletteNum]);

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
  public int getNumColorsAcross(){
    return getWidth() / 15;
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
  public int getSelectedColor(){
    return selectedColor;
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public boolean onClick(JComponent c, MouseEvent e){
    if (c instanceof WSPalettePanel){

      int xPos = e.getX();
      int yPos = e.getY();

      int numCols = getWidth() / 15;
      int numRows = yPos/15;

      int paletteNum = xPos/15 + numRows*numCols;
      setSelectedColor(paletteNum);
      requestFocus();

      Color color = getColorAtPoint(e.getX(),e.getY());
      if (color != null){
        changeSelectedColor();
        }

      return true;
      }
    return false;
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void setPalette(int[] palette){
    if (palette == null || palette.length == 0){
      return;
      }

    this.palette = palette;

    int numColors = palette.length;

    int width = ((WSSidePanelHolder)WSRepository.get("SidePanelHolder")).getWidth() - 47;

    int numCols = width / 15;
    int numRows = numColors / numCols;
    if (numRows == 0 || numColors%numRows != 0){
      numRows++;
      }

    Dimension newSize = new Dimension(width,numRows*15);
    setMaximumSize(newSize);
    setPreferredSize(newSize);
    //setSize(width,numRows*15);

    setSelectedColor(selectedColor);

    revalidate();
    repaint();

    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void setSelectedColor(int selectedColor){
    if (palette == null || palette.length == 0){
      return;
      }
    else if (selectedColor < 0){
      selectedColor = 0;
      }
    else if (selectedColor >= palette.length){
      selectedColor = palette.length - 1;
      }

    this.selectedColor = selectedColor;
    repaint();

    colorChooser = (WSGradientColorChooser)WSRepository.get("SidePanel_Palette_GradientPanel");
    colorChooser.setColor(new Color(palette[selectedColor]),false);

    //WSColorPanel currentColor = (WSColorPanel)WSRepository.get("SidePanel_Colors_CurrentColor");
    //currentColor.setColor(palette[selectedColor]);
    //currentColor.repaint();
    }


/**
**********************************************************************************************
Overwritten to force use of AquanauticBorderLabelUI
@param ui not used
**********************************************************************************************
**/
  public void setUI(PanelUI ui){
    super.setUI(AquanauticWSPalettePanelUI.createUI(this));
    }




  }