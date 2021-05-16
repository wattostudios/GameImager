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
import org.watto.xml.*;

import java.awt.BorderLayout;


/**
**********************************************************************************************
A ExtendedTemplate
**********************************************************************************************
**/
public class WSSidePanelHolder extends WSPanel {

  WSPanel currentPanel = new WSPanel(XMLReader.readString("<WSPanel />"));

/**
**********************************************************************************************
Constructor to construct the component from an XMLNode <i>tree</i>
@param node the XMLNode describing this component
**********************************************************************************************
**/
  public WSSidePanelHolder(XMLNode node){
    super(node);
    }


///////////////
//
// Configurable
//
///////////////


/**
**********************************************************************************************
Builds an XMLNode that describes this object
@return an XML node with the details of this object
**********************************************************************************************
**/
  public XMLNode buildXML(){
    return WSHelper.buildXML(this);
    }



///////////////
//
// Class-Specific Methods
//
///////////////

/**
**********************************************************************************************

**********************************************************************************************
**/
  public WSPanel getCurrentPanel(){
    return currentPanel;
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public String getCurrentPanelCode(){
    return currentPanel.getCode();
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void onCloseRequest(){
    if (currentPanel != null && currentPanel instanceof WSPanelPlugin){
      ((WSPanelPlugin)currentPanel).onCloseRequest();
      }
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void onOpenRequest(){
    if (currentPanel != null && currentPanel instanceof WSPanelPlugin){
      ((WSPanelPlugin)currentPanel).onOpenRequest();
      }
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void loadPanel(String code){
    loadPanel(code,true);
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void loadPanel(String code, boolean changeSetting){
    currentPanel = (WSPanelPlugin)WSPluginManager.group("SidePanel").getPlugin(code);
    if (Settings.getBoolean("AutoChangedToHexPreview")){
      // need this here because otherwise the hex preview is overwritten by the blank preview
      return;
      }

    if (currentPanel == null){
      currentPanel = new WSPanel(XMLReader.readString("<WSPanel />"));
      }
    else {
      if (changeSetting){
        Settings.set("CurrentSidePanel",currentPanel.getCode());
        }
      }

    removeAll();
    add(currentPanel,BorderLayout.CENTER);

    reload();
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void reload(){
    revalidate();
    repaint();
    currentPanel.requestFocus();
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void reloadPanel(){
    try {
      loadPanel(currentPanel.getCode());
      }
    catch (Throwable t){
      }
    }


/**
**********************************************************************************************
Rebuilds the panels from their XML
**********************************************************************************************
**/
  public void rebuild(){
    WSPlugin[] plugins = WSPluginManager.group("SidePanel").getPlugins();
    for (int i=0;i<plugins.length;i++){
      ((WSPanelPlugin)plugins[i]).buildObject(new XMLNode());
      }

    //reload();
    loadPanel(currentPanel.getCode());
    }


  }