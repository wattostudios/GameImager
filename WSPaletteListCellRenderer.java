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

//import org.watto.component.*;
import org.watto.plaf.AquanauticTheme;

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ImageIcon;
import java.awt.Image;


/**
**********************************************************************************************
Allows WSPalettes to be shown in a WSList (typicaly WSButton or WSComboButton - small
components like that)
**********************************************************************************************
**/
public class WSPaletteListCellRenderer extends DefaultListCellRenderer {

/**
**********************************************************************************************
Constructor to construct the component from an XMLNode <i>tree</i>
@param node the XMLNode describing this component
**********************************************************************************************
**/
  public WSPaletteListCellRenderer(){
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
  public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus){
    if (value instanceof Image){
      //JLabel label = new JLabel(new ImageIcon((Image)value));
      //if (isSelected){
      //  label.setBackground(AquanauticTheme.COLOR_MID);
      //  }
      WSPaletteListLabel label = new WSPaletteListLabel((Image)value,isSelected);
      //label.setSelected(isSelected);
      return label;
      }
    return super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
    }





  }