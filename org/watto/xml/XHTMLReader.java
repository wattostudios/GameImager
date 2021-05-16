////////////////////////////////////////////////////////////////////////////////////////////////
//                                                                                            //
//                                       XML UTILITIES                                        //
//                Java Classes for Reading, Writing, and Manipulating XML Files               //
//                                    http://www.watto.org                                    //
//                                                                                            //
//                           Copyright (C) 2003-2008  WATTO Studios                           //
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

package org.watto.xml;

import org.watto.*;
import org.watto.manipulator.*;
import org.watto.manipulator.StringBuffer;

import java.io.*;


/**
**********************************************************************************************
Reads an XHTML file into a tree of <i>XMLNode</i>s.
THIS IS DIFFERENT TO AN XMLREADER!!!
This is because, text in an XHTML document can have nested tags within the text, such as <br />
Therefore, each text has its own special node called !TEXT! which is added as a child, instead
of being content on the node itself. This keeps the nested tags in the correct place.
**********************************************************************************************
**/
public class XHTMLReader {


  /** The stream for reading the file **/
  protected static FileManipulator fm = null;

  /** The currently-read character **/
  protected static char character;

  protected static boolean unicode = false;


/**
**********************************************************************************************
Constructor
**********************************************************************************************
**/
  public XHTMLReader() {
    }


/**
**********************************************************************************************
Loads an XML document <i>path</i> into a tree
@param path the path to the XML document
@return the generated tree of <i>XMLNode</i>s
**********************************************************************************************
**/
  public static XMLNode read(File path){
    XMLNode root = new XMLNode();
    read(path,root);
    try {
      return (XMLNode)root.getChild(0);
      }
    catch (Throwable t){
      return root;
      }
    }


/**
**********************************************************************************************
Loads an XML document <i>path</i> into a tree
@param fmIn the file to read from
@return the generated tree of <i>XMLNode</i>s
**********************************************************************************************
**/
  public static XMLNode read(FileManipulator fmIn){
    XMLNode root = new XMLNode();
    read(fmIn,root);
    try {
      return (XMLNode)root.getChild(0);
      }
    catch (Throwable t){
      return root;
      }
    }


/**
**********************************************************************************************
Builds a tree from a String of XML data. The <i>xml</i> should be valid XML, such as:
<code>&lt;root&gt;&lt;node1 attribute="value" /&gt;&lt;node2&gt;&lt;/root&gt;</code>. There
is no limit to the complexity of the xml data - it can have as many nodes and levels as you
wish, o just be a single tag if wanted.
@param xml a String of XML data
@return the generated tree of <i>XMLNode</i>s
**********************************************************************************************
**/
  public static XMLNode readString(String xml){
    XMLNode root = new XMLNode();
    readString(xml,root);
    try {
      return (XMLNode)root.getChild(0);
      }
    catch (Throwable t){
      return root;
      }
    }


/**
**********************************************************************************************
Loads an XML document <i>path</i> into the tree <i>root</i>. <i>Root</i> can be an empty node,
or any node in an existing tree
@param path the path to the XML document
@param root the node to construct the tree into
**********************************************************************************************
**/
  public static void read(File path, XMLNode root){
    try {

      fm = new FileManipulator(path,"r");

      // look for unicode header
      if (fm.read() == 254 && fm.read() == 255){
        unicode = true;
        }
      else {
        unicode = false;
        fm.seek(0);
        }

      readTag(root);
      fm.close();
      }
    catch (Throwable t){
      logError(t);
      }
    }


/**
**********************************************************************************************
Builds a tree from a String of XML data into the tree <i>root</i>. The <i>xml</i> should be
valid XML, such as:
<code>&lt;root&gt;&lt;node1 attribute="value" /&gt;&lt;node2&gt;&lt;/root&gt;</code>. There
is no limit to the complexity of the xml data - it can have as many nodes and levels as you
wish, o just be a single tag if wanted.
@param xml a String of XML data
@param root the node to construct the tree into
@return the generated tree of <i>XMLNode</i>s
**********************************************************************************************
**/
  public static void readString(String xml, XMLNode root){
    read(new FileManipulator(new StringBuffer(xml)),root);
    }


/**
**********************************************************************************************
Loads an XML document <i>path</i> into the tree <i>root</i>. <i>Root</i> can be an empty node,
or any node in an existing tree
@param fmIn the file to read from
@param root the node to construct the tree into
**********************************************************************************************
**/
  public static void read(FileManipulator fmIn, XMLNode root){
    try {

      fm = fmIn;

      // look for unicode header
      if (fm.read() == 254 && fm.read() == 255){
        unicode = true;
        }
      else {
        unicode = false;
        fm.seek(0);
        }

      readTag(root);
      fm.close();
      }
    catch (Throwable t){
      logError(t);
      }
    }


/**
**********************************************************************************************
Reads the next character from the file into <i>character</i>
**********************************************************************************************
**/
  public static void readChar(){
    try {
      if (unicode){
        character = ((FileManipulator)fm).readChar();
        }
      else {
        character = (char)fm.readByte();
        }
      }
    catch (Throwable t){
      logError(t);
      }
    }


/**
**********************************************************************************************
Reads a single tag from the current position
@param root the root node to add this tag to
**********************************************************************************************
**/
  public static void readTag(XMLNode root){
    try {

      //System.out.println("Tag");

      readChar();

      if (character == '<'){
        readChar();
        }

      if (fm.getFilePointer() >= fm.length()){
        return;
        }

      boolean endTag = false;
      if (character == '/'){
        endTag = true;
        readChar();
        }
      else if (character == '!'){
        readComment();

        readText(root);
        return;
        }
      else if (character == '?'){
        readDeclaration();

        readText(root);
        return;
        }

      String tag = "";

      while (character != '>' && character != ' ' && character != '/'){
        tag += character;
        readChar();
        }


      if (endTag){
        // finished reading an end tag

        // this tag is finished, so continue reading the text for the nodes parent
        readText((XMLNode)root.getParent());
        return;
        }


      XMLNode node = new XMLNode(tag);
      root.addChild(node);



      if (character == ' '){
        // tag has attributes - read them
        // also could be a single tag
        readAttributes(node);
        }

      // NOTE starts with an "if" incase readAttributes() above changes the character!
      if (character == '>'){
        // opening tag is finished

        // start reading the text
        readText(node);

        }
      else if (character == '/'){
        // a single tag

        // Skip the ">" character
        readChar();

        readText(root);
        }
      else {
        // ERROR
        return;
        }

      // done reading this tag

      }
    catch (Throwable t){
      logError(t);
      }
    }


/**
**********************************************************************************************
Reads the text of the tag
@param root the tag that is having its contents read
**********************************************************************************************
**/
  public static void readText(XMLNode root){
    try {

      //System.out.println("text");

      if (character == '>'){
        readChar();
        }

      String text = "";

      boolean previousWhite = false;
      while (character != '<'){
        if (fm.getFilePointer() >= fm.length()){
          return;
          }

        if (character == '\n' || character == '\t' || character == '\r'){
          character = ' ';
          }

        if (character == ' '){
          if (previousWhite){
            // don't write duplicate white spaces
            }
          else {
            // write the single white space, and set up to detect duplicates
            previousWhite = true;
            text += character;
            }
          }
        else {
          // write the normal text
          previousWhite = false;
          text += character;
          }

        readChar();
        }


      // add the text as a child on the current tag
      if (! text.equals("") && !(text.equals(" "))){
        //root.addContent(text);
        XMLNode textNode = new XMLNode("!TEXT!",text);
        root.addChild(textNode);
        }

      readTag(root);

      // done reading the text

      }
    catch (Throwable t){
      logError(t);
      }
    }


/**
**********************************************************************************************
Reads the attributes of the tag
@param root the tag that is having its attributes read
**********************************************************************************************
**/
  public static void readAttributes(XMLNode root){
    try {

      //System.out.println("attributes");

      // loop that continues for all attributes
      while (character != '>' && character != '/'){

        while (character == ' '){
          // Skip the ' ' between tag name and attribute, or between attributes
          readChar();
          }

        if (character == '/'){
          // the end of a single tag
          // could have attributes, but they are already read at this point
          return;
          }

        String key = "";
        String value = "";

        while (character != '=' && character != ' ' && character != '>' && character != '/'){
          key += character;
          readChar();
          }

        if (character == ' ' || character == '>' || character == '/'){
          // a single attribute rather than an key-value pair
          }
        else {
          // now need to read the value

          // read the character after the = sign
          readChar();

          if (character == '\"'){
            // skip the " character at the start of the value
            readChar();
            }

          while (character != '\"'){
            value += character;
            readChar();
            }

          // skip the " character at the end of the value
          readChar();

          }


        // now we have a complete attribute-value pair
        root.addAttribute(key,value);

        }

      // done reading the attributes

      }
    catch (Throwable t){
      logError(t);
      }
    }


/**
**********************************************************************************************
Reads (and discards) a comment tag from the current position
**********************************************************************************************
**/
  public static void readComment(){
    try {

      //System.out.println("comment");

      while (character != '<'){
        if (fm.getFilePointer() >= fm.length()){
          return;
          }

        readChar();

        if (character == '-'){
          readChar();
          if (character == '-'){
            readChar();
            if (character == '>'){
              // end of the comments
              return;
              }
            }
          }
        }

      }
    catch (Throwable t){
      logError(t);
      }
    }


/**
**********************************************************************************************
Reads (and discards) a declaration tag from the current position
**********************************************************************************************
**/
  public static void readDeclaration(){
    try {

      //System.out.println("declaration");

      readChar();

      while (character != '?'){
        readChar();
        if (character == '>'){
          // end of the declaration
          return;
          }
        }

      }
    catch (Throwable t){
      logError(t);
      }
    }



/**
**********************************************************************************************
Prints an error report
@param t the error that occurred.
**********************************************************************************************
**/
  public static void logError(Throwable t){
    ErrorLogger.log(t);
    }


  }

