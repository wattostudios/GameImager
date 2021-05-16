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

import org.watto.*;
import org.watto.component.*;
import org.watto.event.*;
import org.watto.xml.*;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;

/**
**********************************************************************************************
A PanelPlugin
**********************************************************************************************
**/
public class SidePanel_Palette extends WSPanelPlugin implements WSKeyableInterface,
                                                                WSEventableInterface {


/**
**********************************************************************************************
Constructor for extended classes only
**********************************************************************************************
**/
  public SidePanel_Palette(){
    super(new XMLNode());
    }


/**
**********************************************************************************************
Constructor to construct the component from an XMLNode <i>tree</i>
@param node the XMLNode describing this component
@param caller the object that contains this component, created this component, or more formally,
              the object that receives events from this component.
**********************************************************************************************
**/
  public SidePanel_Palette(XMLNode node){
    super(node);
    }



///////////////
//
// Configurable
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

    setLayout(new BorderLayout());

    // Build an XMLNode tree containing all the elements on the screen
    XMLNode srcNode = XMLReader.read(new File("settings" + File.separator + "interface_SidePanel_Palette.wsd"));

    // Build the components from the XMLNode tree
    Component component = WSHelper.buildComponent(srcNode);
    add(component,BorderLayout.CENTER);

    // setting up this object in the repository (overwrite the base panel with this object)
    setCode(((WSComponent)component).getCode());
    WSRepository.add(this);
    }


/**
**********************************************************************************************
Builds an XMLNode that describes this object
@return an XML node with the details of this object
**********************************************************************************************
**/
  public XMLNode buildXML(){
    return super.buildXML();
    }


/**
**********************************************************************************************
Registers the events that this component generates
**********************************************************************************************
**/
  public void registerEvents(){
    super.registerEvents();
    }



///////////////
//
// Class-Specific Methods
//
///////////////


/**
**********************************************************************************************
The event that is triggered from a WSClickableListener when a click occurs
@param c the component that triggered the event
@param e the event that occurred
**********************************************************************************************
**/
  public boolean onClick(JComponent c, MouseEvent e){
    if (c instanceof WSPalettePanel){
      ((WSPalettePanel)WSRepository.get("SidePanel_Palette_PalettePanel")).changeSelectedColor();
      return true;
      }
    return false;
    }


/**
**********************************************************************************************
Performs any functionality that needs to happen when the panel is to be closed. This method
does nothing by default, but can be overwritten to do anything else needed before the panel is
closed, such as garbage collecting and closing pointers to temporary objects.
**********************************************************************************************
**/
  public void onCloseRequest(){
    }


/**
**********************************************************************************************
The event that is triggered from a WSSelectableListener when an item is deselected
@param c the component that triggered the event
@param e the event that occurred
**********************************************************************************************
**/
  public boolean onDeselect(JComponent c, Object e){
    return false;
    }


/**
**********************************************************************************************
The event that is triggered from a WSDoubleClickableListener when a double click occurs
@param c the component that triggered the event
@param e the event that occurred
**********************************************************************************************
**/
  public boolean onDoubleClick(JComponent c, MouseEvent e){
    return false;
    }


/**
**********************************************************************************************
Triggered when the color is changed in the WSSmallColorChooser
**********************************************************************************************
**/
  public boolean onEvent(JComponent c, WSEvent e){
    if (e.getID() == WSEvent.COLOR_CHANGED){
      Color color = ((WSGradientColorChooser)c).getColor();
      // change the color
      ((WSPalettePanel)WSRepository.get("SidePanel_Palette_PalettePanel")).changeSelectedColor(color);
      return true;
      }
    return false;
    }


/**
**********************************************************************************************
Performs any functionality that needs to happen when the panel is to be opened. By default,
it just calls checkLoaded(), but can be overwritten to do anything else needed before the
panel is displayed, such as resetting or refreshing values.
**********************************************************************************************
**/
  public void onOpenRequest(){
    reloadPalette();
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public boolean onKeyPress(JComponent c, KeyEvent e){
    if (c instanceof WSPalettePanel){
      WSPalettePanel panel = (WSPalettePanel)c;
      int color = panel.getSelectedColor();
      int newColor = color;

      int keyCodeInt = e.getKeyCode();

      if (keyCodeInt == KeyEvent.VK_UP){
        newColor -= panel.getNumColorsAcross();
        if (newColor < 0){
          newColor = color;
          }
        }
      else if (keyCodeInt == KeyEvent.VK_DOWN){
        newColor += panel.getNumColorsAcross();
        if (newColor >= panel.getNumColors()){
          newColor = color;
          }
        }
      else if (keyCodeInt == KeyEvent.VK_LEFT){
        newColor--;
        }
      else if (keyCodeInt == KeyEvent.VK_RIGHT){
        newColor++;
        }
      else {
        return true;
        }

      panel.setSelectedColor(newColor);
      return true;
      }
    return false;
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void reloadPalette(){
    Resource currentResource = ((WSFileListPanelHolder)WSRepository.get("FileListPanelHolder")).getCurrentResource();
    if (currentResource != null){
      ((WSPalettePanel)WSRepository.get("SidePanel_Palette_PalettePanel")).setPalette(currentResource.getPalette());
      }
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void requestFocus(){
    //dirHolder.requestFocus();
    }



///////////////
//
// Default Implementations
//
///////////////


/**
**********************************************************************************************
Gets the plugin description
**********************************************************************************************
**/
  public String getDescription(){
    String description = toString() + "\n\n" + Language.get("Description_SidePanel");

    if (! isEnabled()){
      description += "\n\n" + Language.get("Description_PluginDisabled");
      }
    else {
      description += "\n\n" + Language.get("Description_PluginEnabled");
      }

    return description;
    }


/**
**********************************************************************************************
Gets the plugin name
**********************************************************************************************
**/
  public String getText(){
    return super.getText();
    }


/**
**********************************************************************************************
The event that is triggered from a WSHoverableListener when the mouse moves over an object
@param c the component that triggered the event
@param e the event that occurred
**********************************************************************************************
**/
  public boolean onHover(JComponent c, MouseEvent e){
    return super.onHover(c,e);
    }


/**
**********************************************************************************************
The event that is triggered from a WSHoverableListener when the mouse moves out of an object
@param c the component that triggered the event
@param e the event that occurred
**********************************************************************************************
**/
  public boolean onHoverOut(JComponent c, MouseEvent e){
    return super.onHoverOut(c,e);
    }


/**
**********************************************************************************************
Sets the description of the plugin
@param description the description
**********************************************************************************************
**/
  public void setDescription(String description){
    super.setDescription(description);
    }



  }