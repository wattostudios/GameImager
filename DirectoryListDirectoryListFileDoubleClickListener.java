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

import org.watto.component.*;

import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import javax.swing.tree.TreePath;
import java.awt.Point;


/**
**********************************************************************************************

**********************************************************************************************
**/
public class DirectoryListDirectoryListFileDoubleClickListener implements MouseListener{

  DirectoryList_DirectoryList list;


/**
**********************************************************************************************

**********************************************************************************************
**/
  public DirectoryListDirectoryListFileDoubleClickListener(DirectoryList_DirectoryList list){
    this.list = list;
    }

/**
**********************************************************************************************

**********************************************************************************************
**/
  public void mouseClicked(MouseEvent e){

    if (e.getClickCount() == 2) {
      ((WSPanelPlugin)((WSSidePanelHolder)WSRepository.get("SidePanelHolder")).getCurrentPanel()).onDoubleClick(list,e);
      }

    }

/**
**********************************************************************************************

**********************************************************************************************
**/
  public void mousePressed(MouseEvent e){
    }

/**
**********************************************************************************************

**********************************************************************************************
**/
  public void mouseReleased(MouseEvent e){
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void mouseEntered(MouseEvent e){
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void mouseExited(MouseEvent e){
    }


  }