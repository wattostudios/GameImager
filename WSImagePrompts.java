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

import org.watto.ErrorLogger;
import org.watto.Settings;
import org.watto.component.*;
import org.watto.xml.*;

import java.awt.Dimension;
import java.io.File;
import javax.swing.JPanel;


/**
**********************************************************************************************
Prompts the user for information
**********************************************************************************************
**/
public class WSImagePrompts {

/**
**********************************************************************************************

**********************************************************************************************
**/
  public WSImagePrompts(){
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
  public static Dimension askForDimensions(){
    return askForDimensions(0,0);
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public static Dimension askForDimensions(long dataSize){
    long width = 0;
    long height = 0;

    long tryValue = (int)Math.sqrt(dataSize);

    while (tryValue > 0){

      // see if it divides evenly
      long remainder = dataSize%tryValue;

      if (remainder == 0){
        // found an even division
        height = tryValue;
        width = dataSize/tryValue; // make the width the longer value

        tryValue = 0;
        break;
        }

      tryValue--;

      }

    return askForDimensions(width,height);
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public static Dimension askForDimensions(long width, long height){
    try {

      XMLNode srcNode = XMLReader.read(new File(Settings.getString("WSImagePrompts_AskForDimensions")));
      JPanel panel = (JPanel)WSHelper.buildComponent(srcNode);


      // set the dimensions
      ((WSTextField)WSRepository.get("WSImagePrompt_AskForDimensions_Width")).setText(""+width);
      ((WSTextField)WSRepository.get("WSImagePrompt_AskForDimensions_Height")).setText(""+height);


      String code = WSPopupPanel.show(panel,"WSImagePrompts_AskForDimensions");

      if (! code.equals("WSImagePrompt_AskForDimensions_OK")){
        return new Dimension(0,0);
        }
      else {
        width = Long.parseLong(((WSTextField)WSRepository.get("WSImagePrompt_AskForDimensions_Width")).getText());
        height = Long.parseLong(((WSTextField)WSRepository.get("WSImagePrompt_AskForDimensions_Height")).getText());

        return new Dimension((int)width,(int)height);
        }
      }
    catch (Throwable t){
      ErrorLogger.log(t);
      return new Dimension(0,0);
      }
    }



/**
**********************************************************************************************

**********************************************************************************************
**/
  public static String askForType(String[] types){
    try {

      XMLNode srcNode = XMLReader.read(new File(Settings.getString("WSImagePrompts_AskForType")));
      JPanel panel = (JPanel)WSHelper.buildComponent(srcNode);


      // set the dimensions
      ((WSComboBox)WSRepository.get("WSImagePrompt_AskForType_Chooser")).setData(types);


      String code = WSPopupPanel.show(panel,"WSImagePrompts_AskForType");

      if (! code.equals("WSImagePrompt_AskForType_OK")){
        return "";
        }
      else {
        return (String)((WSComboBox)WSRepository.get("WSImagePrompt_AskForType_Chooser")).getSelectedValue();
        }
      }
    catch (Throwable t){
      ErrorLogger.log(t);
      return "";
      }
    }





  }