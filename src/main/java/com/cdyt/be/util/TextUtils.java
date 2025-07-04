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

  /**
   * Generates a URL-friendly slug from the given input string.
   * Converts to lowercase, removes accents, replaces spaces and special
   * characters with hyphens,
   * and removes consecutive hyphens.
   */
  public static String generateSlug(String input) {
    if (input == null || input.trim().isEmpty()) {
      return "";
    }

    // Remove accents and normalize
    String slug = removeAccents(input.trim());

    // Convert to lowercase
    slug = slug.toLowerCase();

    // Replace spaces and special characters with hyphens
    slug = slug.replaceAll("[^a-z0-9]+", "-");

    // Remove leading and trailing hyphens
    slug = slug.replaceAll("^-+|-+$", "");

    // Replace multiple consecutive hyphens with single hyphen
    slug = slug.replaceAll("-+", "-");

    return slug;
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
