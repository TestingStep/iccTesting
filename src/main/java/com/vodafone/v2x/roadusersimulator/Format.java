package com.vodafone.v2x.roadusersimulator;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.Vector;

public class Format {

    //public final static String TAG = "Format";
    private static Logger logger = LogManager.getLogger(Format.class);

    /**
     * Convert a String representing an Hexadecimal value into a byte array
     * ex: "CAFE0056" is converted into {0xCA,0xFE,0x00,0x56}.
     * @param s
     * @return a byte array
     */
    public static byte[] hexStringToByteArray (String s) {
        String cmd;
        if ((s.length() & 0x01)==0x01) {
            logger.error("Invalid String length, the length should be EVEN, real length="+s.length()+" String will be Truncated");
            cmd = s.substring(0,s.length()-1);
        }
        else cmd = s;
        byte[] command = new byte[cmd.length() / 2];
        int result = 0;
        int i = 0;
        try {
            for (i = 0; i < cmd.length()/2; i++) {
                String byteToConvert =cmd.substring(i*2,(i*2)+2);
                result = Integer.parseInt(byteToConvert,16);
                command[i]=(byte)result; }
        }
        catch (NumberFormatException e) {
            logger.error("E73 s="+s,e);
        }
        return command;
    }

    public static String formatDate(long timeInMs) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS z");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return simpleDateFormat.format(timeInMs);
    }


    /**
     * Send back an hexadecimal representation of a byte array.
     * ex:ex: {0xCA,0xFE,0x00,0x56} is converted into "CAFE0056".
     * @param b
     * @return a String
     */
    public static String byteArrayToHexString(byte[] b) {
        if (b==null) return "";
        StringBuffer reply = new StringBuffer(b.length * 2);
        for (int i = 0; i < b.length; i++)
        {reply.append(integerToHex((int) b[i], 2));}
        return reply.toString();
    }

    /**
     * Convert an integer into an hexadecimal string having the specified length.
     * The integer could be padded with zeros but it could be truncated as well.
     * @param i the integer to convert
     * @param length the final length of the integer
     * @return a string representing the given integer
     */
    public static String integerToHex(int i, int length) {
        String hexadecimal = Integer.toHexString(i).toUpperCase();
        return leftPadOrLeftTruncate(hexadecimal, '0', length);
    }

    /**
     * Parse the String s using the char sep as separator
     * Results are returned inside a Vector Object.
     * param String to parse
     * param separator character to use
     * return  Vector containing all the parameters extracted.
     */
    public static Vector parseString(String s, char sep) {
        Vector result = new Vector();
        try {
            int beginIndex = 0;
            int endIndex = 0;
            if (s != null) {
                while ((beginIndex < s.length()) && (endIndex = s.indexOf(sep, beginIndex)) > -1) {
                    result.addElement(s.substring(beginIndex, endIndex));
                    beginIndex = endIndex + 1;
                }
                if (beginIndex < (s.length())) {
                    result.addElement(s.substring(beginIndex));
                }
                if (endIndex == s.length() - 1) {
                    result.addElement("");
                }
            }
        } catch (IndexOutOfBoundsException e) {
            logger.error("E131 s=" + s + " sep=" + sep, e);
        }
        return result;
    }

    /**
     * Left pad or Left Truncate the string provided to obtain the expected length.
     * @param source
     * @param pad
     * @param expectedLength
     * @return
     */
    public static String leftPadOrLeftTruncate(String source, char pad, int expectedLength) {
        while (source.length()!=expectedLength) {
            if (source.length() < expectedLength) {
                source = pad + source;
            }else {
                source = source.substring(1);
            }
        }
        return source;
    }

    /**
     * Right pad or Right Truncate the string provided to obtain the expected length.
     * @param source
     * @param pad
     * @param expectedLength
     * @return
     */
    public static String rightPadOrRightTruncate(String source, char pad, int expectedLength) {
        while (source.length()!=expectedLength) {
            if (source.length() < expectedLength) {
                source = source +pad ;
            }else {
                source = source.substring(0,source.length()-1);
            }
        }
        return source;
    }


    public static String getUTCTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date());
    }

    public static String getDay() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date());
    }

}
