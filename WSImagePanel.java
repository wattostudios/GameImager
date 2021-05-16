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

import java.awt.Image;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.plaf.PanelUI;

/**
**********************************************************************************************
A ExtendedTemplate
**********************************************************************************************
**/
public class WSImagePanel extends WSPanel implements WSHoverableInterface {

  Image image = null;
  int imageHeight = 0;
  int imageWidth = 0;


/**
**********************************************************************************************

**********************************************************************************************
**/
  public WSImagePanel(){
    super();
    }


/**
**********************************************************************************************
Constructor to construct the component from an XMLNode <i>tree</i>
@param node the XMLNode describing this component
**********************************************************************************************
**/
  public WSImagePanel(XMLNode node){
    super(node);
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
  public Image getImage(){
    return image;
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public int getImageHeight(){
    return imageHeight;
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public int getImageWidth(){
    return imageWidth;
    }


/**
**********************************************************************************************
The event that is triggered from a WSHoverableListener when the mouse moves over an object
@param c the component that triggered the event
@param e the event that occurred
**********************************************************************************************
**/
  public boolean onHover(JComponent c, MouseEvent e){
    Resource resource = ((WSFileListPanelHolder)WSRepository.get("FileListPanelHolder")).getCurrentResource();
    if (resource != null){
      ((WSStatusBar)WSRepository.get("StatusBar")).setText(Language.get("Image") + ": " + resource.toString());
      return true;
      }
    return false;
    }


/**
**********************************************************************************************
The event that is triggered from a WSHoverableListener when the mouse moves out of an object
@param c the component that triggered the event
@param e the event that occurred
**********************************************************************************************
**/
  public boolean onHoverOut(JComponent c, MouseEvent e){
    return false;
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void setImage(Image image){
    this.image = image;
    repaint();
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void setImage(Image image, int imageWidth, int imageHeight){
    this.image = image;
    this.imageWidth = imageWidth;
    this.imageHeight = imageHeight;

    setPreferredSize(new Dimension(imageWidth,imageHeight));
    revalidate();
    repaint();

    }


/**
**********************************************************************************************
Overwritten to force use of AquanauticBorderLabelUI
@param ui not used
**********************************************************************************************
**/
  public void setUI(PanelUI ui){
    super.setUI(AquanauticWSImagePanelUI.createUI(this));
    }




  }