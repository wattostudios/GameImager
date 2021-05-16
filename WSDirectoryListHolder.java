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
import javax.swing.border.EmptyBorder;
import java.io.File;
import java.io.FileFilter;


/**
**********************************************************************************************
A ExtendedTemplate
**********************************************************************************************
**/
public class WSDirectoryListHolder extends WSPanel {

  DirectoryListPanel currentPanel;

/**
**********************************************************************************************
Constructor to construct the component from an XMLNode <i>tree</i>
@param node the XMLNode describing this component
**********************************************************************************************
**/
  public WSDirectoryListHolder(XMLNode node){
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
    setLayout(new BorderLayout(0,0));
    setBorder(new EmptyBorder(0,0,0,0));
    loadPanel(Settings.get("DirectoryListView"));
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
  public void checkFiles(){
    if (currentPanel != null){
      currentPanel.checkFilesExist();
      }
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public File[] getAllSelectedFiles(){
    if (currentPanel != null){
      return currentPanel.getAllSelectedFiles();
      }
    return new File[0];
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public File getCurrentDirectory(){
    if (currentPanel != null){
      return currentPanel.getCurrentDirectory();
      }
    return new File("");
    }


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
  public File getSelectedFile(){
    if (currentPanel != null){
      return currentPanel.getSelectedFile();
      }
    return null;
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void loadPanel(String code){
    DirectoryListPanel currentPanel = (DirectoryListPanel)WSPluginManager.group("DirectoryList").getPlugin(code);
    loadPanel(currentPanel);
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void loadPanel(DirectoryListPanel panel){
    if (panel == null){
      return;
      }

    if (this.currentPanel == panel){
      // so we don't keep reloading the same panel over and over
      return;
      }

    //panel.reload();

    this.currentPanel = panel;

    removeAll();
    add(panel,BorderLayout.CENTER);
    revalidate();

    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void reload(){
    if (currentPanel != null){
      currentPanel.reload();
      }
    revalidate();
    repaint();
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void requestFocus(){
    if (currentPanel != null){
      currentPanel.requestFocus();
      }
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void scrollToSelected(){
    if (currentPanel != null){
      currentPanel.scrollToSelected();
      }
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void setMatchFilter(FileFilter filter){
    if (currentPanel != null){
      currentPanel.setMatchFilter(filter);
      }
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void setMultipleSelection(boolean multi){
    if (currentPanel != null){
      currentPanel.setMultipleSelection(multi);
      }
    }


  }