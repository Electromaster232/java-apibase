package me.djelectro.javaflasklike.types;

import me.djelectro.javaflasklike.utils.Database;
import me.djelectro.javaflasklike.utils.Redis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public abstract class Types {
  protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	protected static Database dbConn;
	protected static Redis jedisConn;


	public static void setDatabase(Database db){
		dbConn = db;
	}

	public static void setJedisConn(Redis db) { jedisConn = db;}

  public static boolean testDatabase(){
    if(dbConn == null)
      return false;

    Map<Integer, String[]> a = dbConn.executeAndReturnData("SELECT 1");
    return a.size() == 1;
  }

}
