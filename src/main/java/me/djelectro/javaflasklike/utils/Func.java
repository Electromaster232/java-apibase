package me.djelectro.javaflasklike.utils;

public class Func {

  public static Integer intParse(String text) {
    try {
      return Integer.parseInt(text);
    } catch (NumberFormatException e) {
      return null;
    }
  }

}
