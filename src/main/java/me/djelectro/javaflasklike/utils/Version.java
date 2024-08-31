package me.djelectro.javaflasklike.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;

public class Version {
  public static final int MAJOR_RELEASE = 0;
  public static final int MINOR_RELEASE = 3;
  public static final int API_REVISION = 1;
  public static final int BACKEND_BUILD_NUMBER = 1;
  public static final String RELEASE_NAME = "BETA";

  public static String getReleaseInformation(){
      return String.format("%d.%d.%d \"%s\"", MAJOR_RELEASE, MINOR_RELEASE, BACKEND_BUILD_NUMBER, RELEASE_NAME);
  }

  /**
   *
   * @param includemessage Specify whether to include the "MESSAGE" element in the returned response. Useful for the API Home
   * @return HashMap containing all elements of the Version class
   */
  public static HashMap<String, Object> getReleaseInformationMap(boolean includemessage){

    HashMap<String, Object> res = new HashMap<>();

    if(includemessage)
      res.put("MESSAGE", "Welcome to Java Flask-Like");
    res.put("MAJOR_RELEASE", MAJOR_RELEASE);
    res.put("MINOR_RELEASE", MINOR_RELEASE);
    res.put("API_REVISION", API_REVISION);
    res.put("BUILD_NUMBER", BACKEND_BUILD_NUMBER);
    res.put("RELEASE_NAME", RELEASE_NAME);


    return res;
  }

  public static String getReleaseInformationMapAsJsonString(){
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      return objectMapper.writeValueAsString(getReleaseInformationMap(true));
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }


}
