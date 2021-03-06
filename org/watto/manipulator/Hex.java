////////////////////////////////////////////////////////////////////////////////////////////////
//                                                                                            //
//                                      FILE MANIPULATOR                                      //
//                Generic, Multi-Purpose File Reading and Writing Java Classes                //
//                                    http://www.watto.org                                    //
//                                                                                            //
//                           Copyright (C) 2002-2008  WATTO Studios                           //
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

package org.watto.manipulator;

import java.util.Locale;
import java.io.UnsupportedEncodingException;

/**
**********************************************************************************************
  A Hex String. Basically the same as a String, with all the same methods, however it cannot be
  used as a String because String is a final class (extends is not allowed).
**********************************************************************************************
**/

public class Hex {

  String hex;


/**
**********************************************************************************************

**********************************************************************************************
**/
  public Hex(String hex){
    this.hex = hex.toUpperCase();
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public char charAt(int index){
    return hex.charAt(index);
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
//  public int compareTo(Object o){
//    return hex.compareTo(o);
//    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public int compareTo(String anotherString){
    return hex.compareTo(anotherString);
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public int compareToIgnoreCase(String str){
    return hex.compareToIgnoreCase(str);
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public String concat(String str){
    return hex.concat(str);
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public boolean contentEquals(java.lang.StringBuffer sb){
    return hex.contentEquals(sb);
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public boolean endsWith(String suffix){
    return hex.endsWith(suffix);
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public boolean equals(Object anObject){
    return hex.equals(anObject);
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public boolean equalsIgnoreCase(String anotherString){
    return hex.equalsIgnoreCase(anotherString);
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public byte[] getBytes(){
    return hex.getBytes();
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void getBytes(int srcBegin, int srcEnd, byte[] dst, int dstBegin){
    //hex.getBytes(srcBegin,srcEnd,dst,dstBegin);
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public byte[] getBytes(String charsetName) throws UnsupportedEncodingException{
    return hex.getBytes(charsetName);
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public void getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin){
    hex.getChars(srcBegin,srcEnd,dst,dstBegin);
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public int hashCode(){
    return hex.hashCode();
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public int indexOf(int ch){
    return hex.indexOf(ch);
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public int indexOf(int ch, int fromIndex){
    return hex.indexOf(ch,fromIndex);
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public int indexOf(String str){
    return hex.indexOf(str);
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public int indexOf(String str, int fromIndex){
    return hex.indexOf(str,fromIndex);
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public String intern(){
    return hex.intern();
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public int lastIndexOf(int ch){
    return hex.lastIndexOf(ch);
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public int lastIndexOf(int ch, int fromIndex){
    return hex.lastIndexOf(ch,fromIndex);
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public int lastIndexOf(String str){
    return hex.lastIndexOf(str);
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public int lastIndexOf(String str, int fromIndex){
    return hex.lastIndexOf(str,fromIndex);
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public int length(){
    return hex.length();
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public boolean matches(String regex){
    return hex.matches(regex);
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public boolean regionMatches(boolean ignoreCase, int toffset, String other, int ooffset, int len){
    return hex.regionMatches(ignoreCase,toffset,other,ooffset,len);
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public boolean regionMatches(int toffset, String other, int ooffset, int len){
    return hex.regionMatches(toffset,other,ooffset,len);
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public String replace(char oldChar, char newChar){
    return hex.replace(oldChar,newChar);
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public String replaceAll(String regex, String replacement){
    return hex.replaceAll(regex,replacement);
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public String replaceFirst(String regex, String replacement){
    return hex.replaceFirst(regex,replacement);
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public String[] split(String regex){
    return hex.split(regex);
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public String[] split(String regex, int limit){
    return hex.split(regex,limit);
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public boolean startsWith(String prefix){
    return hex.startsWith(prefix);
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public boolean startsWith(String prefix, int toffset){
    return hex.startsWith(prefix,toffset);
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public CharSequence subSequence(int beginIndex, int endIndex){
    return hex.subSequence(beginIndex,endIndex);
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public String substring(int beginIndex){
    return hex.substring(beginIndex);
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public String substring(int beginIndex, int endIndex){
    return hex.substring(beginIndex,endIndex);
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public char[] toCharArray(){
    return hex.toCharArray();
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public String toLowerCase(){
    return hex.toLowerCase();
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public String toLowerCase(Locale locale){
    return hex.toLowerCase(locale);
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public String toString(){
    return hex.toString();
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public String toUpperCase(){
    return hex.toUpperCase();
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public String toUpperCase(Locale locale){
    return hex.toUpperCase(locale);
    }


/**
**********************************************************************************************

**********************************************************************************************
**/
  public String trim(){
    return hex.trim();
    }


  }