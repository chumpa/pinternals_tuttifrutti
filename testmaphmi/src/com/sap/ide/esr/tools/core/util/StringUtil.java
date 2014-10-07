package com.sap.ide.esr.tools.core.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.StringTokenizer;

public final class StringUtil {

   public static final String EMPTY_STRING = "";
   public static final String SLASH = "/";
   private static char[] toHex = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
   private static final int BLOCK_SIZE = 512;
   private static byte[] fromHex = new byte[]{(byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)0, (byte)1, (byte)2, (byte)3, (byte)4, (byte)5, (byte)6, (byte)7, (byte)8, (byte)9, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)10, (byte)11, (byte)12, (byte)13, (byte)14, (byte)15, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)10, (byte)11, (byte)12, (byte)13, (byte)14, (byte)15, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1};


   public static String byteArrayToHex(byte[] b) {
      if(b == null) {
         return null;
      } else {
         char[] result = new char[b.length * 2];
         int pos = 0;

         for(int i = 0; i < b.length; ++i) {
            byte x = b[i];
            result[pos++] = toHex[(x & 240) >> 4];
            result[pos++] = toHex[x & 15];
         }

         return new String(result);
      }
   }

   public static byte[] hexToByteArray(String hex) {
      if(hex == null) {
         return null;
      } else {
         int len = hex.length();
         int size = len / 2;
         boolean startpos = false;
         byte[] result;
         byte var14;
         if(len % 2 == 1) {
            ++size;
            result = new byte[size];
            result[0] = fromHex[hex.charAt(0)];
            var14 = 1;
            if(result[0] == -1) {
               throwNumberFormatException(hex);
            }
         } else {
            result = new byte[size];
            var14 = 0;
         }

         int bufferSize = size > 512?1024:2 * size;
         char[] buffer = new char[bufferSize];
         int hexOffset = var14;

         for(int blockStart = var14; blockStart < size; blockStart += 512) {
            int blockEnd = blockStart + 512;
            if(blockEnd > size) {
               blockEnd = size;
            }

            hex.getChars(hexOffset, hexOffset + (blockEnd - blockStart) * 2, buffer, 0);
            int bufpos = 0;

            for(int j = blockStart; j < blockEnd; ++j) {
               byte highNibble = fromHex[buffer[bufpos++]];
               byte lowNibble = fromHex[buffer[bufpos++]];
               if(highNibble == -1 || lowNibble == -1) {
                  throwNumberFormatException(hex);
               }

               result[j] = (byte)((byte)(highNibble << 4) + lowNibble);
            }

            hexOffset += 1024;
         }

         return result;
      }
   }

   public static String[] split(String path, String delim) {
      if(path == null) {
         return new String[0];
      } else {
         StringTokenizer tokenizer = new StringTokenizer(path, delim);
         ArrayList pathList = new ArrayList();

         while(tokenizer.hasMoreTokens()) {
            pathList.add(tokenizer.nextToken());
         }

         return (String[])pathList.toArray(new String[0]);
      }
   }

   private static void throwNumberFormatException(String hex) throws NumberFormatException {
      throw new NumberFormatException("\"" + hex + "\" is not a hexadecimal number.");
   }

   public static String printStackTrace(Throwable t) {
      if(null == t) {
         return "";
      } else {
         StringWriter sw = new StringWriter();
         PrintWriter pw = new PrintWriter(sw, true);
         t.printStackTrace(pw);
         pw.flush();
         sw.flush();
         return sw.toString();
      }
   }

   public static String getNonDigitString(String s) {
      StringBuffer strBuffer = new StringBuffer();
      int length = s.length();

      for(int i = 0; i < length; ++i) {
         char ch = s.charAt(i);
         if(!Character.isDigit(ch)) {
            strBuffer.append(ch);
         }
      }

      return strBuffer.toString();
   }

   public static boolean isEmpty(String input) {
      return null == input || "".equals(input);
   }

   public static int commonPrefix(String a, String b) {
      int length = Math.max(a.length(), b.length());
      int res = -1;

      for(int i = 0; i < length && a.charAt(i) == b.charAt(i); res = i++) {
         ;
      }

      return res;
   }

}
