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
import org.watto.plaf.AquanauticTheme;
import org.watto.xml.*;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import javax.swing.border.LineBorder;
import java.util.*;
import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.DataFlavor;

public class FileListPanel_Image extends FileListPanel implements WSClickableInterface,
                                                                  WSDoubleClickableInterface,
                                                                  WSKeyableInterface,
                                                                  WSResizableInterface,
                                                                  WSMotionableInterface {

  WSImagePanel preview;
  WSList list;
  WSScrollPane listScroll;

  int zoom = 100;
  int selected = 0;

  JSplitPane splitPane;
  JScrollPane previewScroll;

  Object lastMotionObject = new Object();

/**
**********************************************************************************************

**********************************************************************************************
**/
  public FileListPanel_Image() {
    super("Image");
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void changeSelection(int row){
    if (list.isSelectedIndex(row)){
      list.removeSelectionInterval(row,row);
      }
    else {
      list.addSelectionInterval(row,row);
      }
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void constructInterface(){
    removeAll();

    preview = new WSImagePanel(new XMLNode(""));

    list = new WSList(new XMLNode(""));
    list.setCellRenderer(new AquanauticWSThumbnailListUI());


    listScroll = new WSScrollPane(new XMLNode(""));
    listScroll.setViewportView(list);
    listScroll.setShowBorder(false);
    listScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    listScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    //listScroll.addMouseListener(new WSHoverableListener(this));

    previewScroll = new JScrollPane(preview);

    splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,listScroll,add(previewScroll));
    splitPane.getLeftComponent().setMinimumSize(new Dimension(0,0));
    splitPane.getRightComponent().setMinimumSize(new Dimension(0,0));
    splitPane.setDividerSize(5);
    splitPane.addComponentListener(new WSResizableListener(this));
    add(splitPane,BorderLayout.CENTER);

    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public Resource getCurrentResource(){
    return Archive.getResource(selected);
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public int getFirstSelectedRow(){
    return list.getSelectedIndex();
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public int getNumSelected(){
    return list.getSelectedValues().length;
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public Resource getResource(int row){
    return Archive.getResource(row);
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public Resource[] getSelected(){
    Object[] objects = list.getSelectedValues();
    Resource[] resources = new Resource[objects.length];
    for (int i=0;i<objects.length;i++){
      resources[i] = (Resource)objects[i];
      }
    return resources;
    }


/**
**********************************************************************************************
Builds the list from all the resources in the Archive
**********************************************************************************************
**/
  public void reload(){
    reloadImage();
    reloadList();
    }


/**
**********************************************************************************************
Builds the list from all the resources in the Archive
**********************************************************************************************
**/
  public void reloadImage(){
    Resource resource = Archive.getResource(selected);
    if (resource == null){
      preview.setImage(null);
      selected = 0;
      return;
      }

    Image image = resource.getImage();
    if (image == null){
      preview.setImage(null);
      selected = 0;
      return;
      }

    int width = resource.getWidth();
    int height = resource.getHeight();

    if (zoom == 100){
      // 100%
      }
    else {
      // smaller or larger
      width = (int)(width * (((double)zoom)/100));
      height = (int)(height * (((double)zoom)/100));
      image = image.getScaledInstance(width,height,Image.SCALE_AREA_AVERAGING);
      }

    //ImageIcon icon = new ImageIcon(image);
    preview.setImage(image,width,height);
    }


/**
**********************************************************************************************
Builds the list from all the resources in the Archive
**********************************************************************************************
**/
  public void reloadList(){

    int[] selectedItems = list.getSelectedIndices();

    list.setListData(Archive.getResources());

    // set the list dimensions
    Dimension scrollSize = listScroll.getViewport().getExtentSize();
    int scrollWidth = (int)scrollSize.getWidth();
    //int scrollHeight = (int)scrollSize.getHeight();
    int scrollHeight = (int)list.getHeight();

    list.setSize(scrollWidth,scrollHeight);

    list.setSelectedIndices(selectedItems);
    }


/**
**********************************************************************************************
Reload the SidePanel_Palette when the selected image changes
This isn't needed, because it is done by SidePanel.onOpenRequest();
**********************************************************************************************
**/
  public void reloadPalette(){
    WSSidePanelHolder holder = (WSSidePanelHolder)WSRepository.get("SidePanelHolder");
    if (holder.getCurrentPanelCode().equals("SidePanel_Palette")){
      holder.onOpenRequest();
      }
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void selectAll(){
    list.setSelectionInterval(0,list.getRowCount());
    reloadList();
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void selectInverse(){
    list.setVisible(false);
    for (int i=1;i<list.getRowCount();i++){
      changeSelection(i);
      }
    changeSelection(0);
    list.setVisible(true);
    reloadList();
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void selectNone(){
    list.clearSelection();
    reloadList();
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void selectResource(int row){
    selectNone();
    changeSelection(row);
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void setCurrentImage(int imageNum){
    selected = imageNum;
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void setZoom(int zoom){
    this.zoom = zoom;
    reloadImage();
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public boolean onClick(JComponent c, java.awt.event.MouseEvent e){
    if (!e.isControlDown() && !e.isShiftDown()){
      selected = list.getSelectedIndex();
      setCurrentImage(selected);
      reloadImage();
      //reloadPalette();
      ((WSSidePanelHolder)WSRepository.get("SidePanelHolder")).onOpenRequest();
      }
    return false;
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void onCloseRequest(){
    double splitLocationOld = Settings.getDouble("FileListImage_DividerLocation");
    double splitLocationNew = (double)(splitPane.getDividerLocation()) / (double)(splitPane.getWidth());
    double diff = splitLocationOld - splitLocationNew;
    if (diff > 0.01 || diff < -0.01){
      // only set if the change is large.
      // this gets around the problem with the split slowly moving left over each load
      Settings.set("FileListImage_DividerLocation",splitLocationNew);
      }
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public boolean onKeyPress(JComponent c, java.awt.event.KeyEvent e){
    //if (!e.isControlDown() && !e.isShiftDown()){
      selected = list.getSelectedIndex();
      setCurrentImage(selected);
      reloadImage();
      //reloadPalette();
      ((WSSidePanelHolder)WSRepository.get("SidePanelHolder")).onOpenRequest();
    //  }
    return false;
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void onOpenRequest(){
    Double location = Settings.getDouble("FileListImage_DividerLocation");
    if (location < 0 || location > 1){
      location = 0.2;
      }
    splitPane.setDividerLocation(location);
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
The event that is triggered from a WSResizableListener when a component is resized
@param c the component that triggered the event
@param e the event that occurred
**********************************************************************************************
**/
  public boolean onResize(JComponent c, java.awt.event.ComponentEvent e){
    if (c == splitPane){
      // reposition the splitpane divider when the splitpane changes sizes
      double splitPos = Settings.getDouble("FileListImage_DividerLocation");
      if (splitPos < 0 || splitPos > 1){
        splitPos = 0.2;
        }

      //System.out.println("Before: " + splitPos);
      splitPane.setDividerLocation(splitPos);
      //System.out.println("After: " + ((double)((WSSplitPane)c).getDividerLocation() / ((WSSplitPane)c).getWidth()));
      }

    reloadList();
    return true;
    }


/**
**********************************************************************************************
The event that is triggered from a WSMotionableListener when a component is moved over
@param c the component that triggered the event
@param e the event that occurred
**********************************************************************************************
**/
  public boolean onMotion(JComponent c, java.awt.event.MouseEvent e){
    if (c == list){
      int index = list.locationToIndex(e.getPoint());

      Resource resource = Archive.getResource(index);

      if (resource == null || lastMotionObject.equals(resource)){
        return true; // still over the same object on the list
        }
      lastMotionObject = resource;

      String name = resource.toString();

      ((WSStatusBar)WSRepository.get("StatusBar")).setText(Language.get("Thumbnail") + ": " + resource);
      return true;

      }

    return false;
    }


  }