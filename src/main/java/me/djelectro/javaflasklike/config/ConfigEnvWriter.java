package me.djelectro.javaflasklike.config;

import java.io.FileWriter;
import java.io.IOException;

/**
 * ConfigEnvWriter: On launch, read in the file specified in args[1], and attempt to pass to Config.
 * For each Config value, try to find an Environment Variable with the same name, and if it exists,
 * overwrite the configuration value with the env var's value.
 * <p>
 * Write the output to the file named args[2]
 */
public class ConfigEnvWriter {

  public static void main(String[] args) {

    if (args.length < 3) {
      System.err.println("Invalid number of parameters specified.");
      printUsage();
      System.exit(1);
    }

    // Try to read in old config.
    Config c = null;
    try {
      c = new Config(args[1]);
    } catch (IOException e) {
      e.printStackTrace();
      System.err.println("Error opening or parsing file. Please check input and try again");
      printUsage();
      System.exit(1);
    }


    String[] keys = c.getAllKeys();

    for(String key : keys){
      // For every key, try to find a system environment variable with the same name. If exists - overwrite value
      // If not, leave value unchanged.
      // The next step will be to iterate over every key again and write it to a file.
      String val = System.getenv(key);
      if(val != null)
        c.updateValue(key, val);
    }

    try(FileWriter fr = new FileWriter(args[2])) {
      fr.write(c.returnJsonObj());
    } catch (IOException e) {
        throw new RuntimeException(e);
    }

    System.out.println("File written successfully.");


  }

  private static void printUsage() {
    System.out.println("Replace configuration values with those from environment variables.");
    System.out.println("Usage:\n");
    System.out.println("dashboard-BETA.jar config <inputFile> <outputFile>");
    System.out.println("Where inputFile is the existing/template configuration to be read, and outputFile is where you would like the result placed");
  }
}
