package com.cdyt.be.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;

public class TextUtils {

  public static String removeAccents(String input) {
    if (input == null || input.isEmpty()) {
      return null;
    }
    String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
    return normalized.replaceAll("\\p{M}", "");
  }

  public static String stringToMD5(String input) {
    if (input == null || input.isEmpty()) {
      return null;
    }
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      byte[] digest = md.digest(input.getBytes());
      // Convert byte[] to hex string
      StringBuilder hexString = new StringBuilder();
      for (byte b : digest) {
        String hex = Integer.toHexString(0xff & b);
        if (hex.length() == 1) {
          hexString.append('0');
        }
        hexString.append(hex);
      }
      return hexString.toString();
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("MD5 algorithm not found", e);
    }
  }

  public static boolean isEmpty(String password) {
    return password == null && password.trim().isEmpty();
  }
}
