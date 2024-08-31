package me.djelectro.javaflasklike.utils;

import me.djelectro.javaflasklike.enums.AccessLevel;
import me.djelectro.javaflasklike.types.User;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.*;

public class Redis {

  private final JedisPool pool;
  private final int userTableIndex;
  private final int campaignTableIndex;

  public Redis(String host, String port, String pass, int userTableIndex, int campaignTableIndex) {
    this.userTableIndex = userTableIndex;
    this.campaignTableIndex = campaignTableIndex;
    if(pass.equals("NONE")){
      pool = new JedisPool(new GenericObjectPoolConfig<>(), host, Integer.parseInt(port), 2000);
    }
    else {
      pool = new JedisPool(new GenericObjectPoolConfig<>(), host, Integer.parseInt(port), 2000, pass);
    }
    System.out.println("Redis successfully connected");
  }

  public void storeUser(User user, String token) {
    try (Jedis connection = pool.getResource()) {
      connection.select(userTableIndex);
      connection.hdel(token, "id", "access", "organizations");
      connection.hset(token, "id", Integer.toString(user.getUserId()));
      connection.hset(token, "access", user.getAccessMap().toString());
      connection.hset(token, "organizations", user.getOrganizationMap().toString());
      connection.expire(token, 604800);
    }
  }

  public User getUser(String token) {
    try (Jedis connection = pool.getResource()) {
      connection.select(userTableIndex);
      String redisRes = connection.hget(token, "id");
      if (redisRes == null)
        return null;
      int id = Integer.parseInt(redisRes);
      Map<Integer, AccessLevel> access = new HashMap<>();
      String value = connection.hget(token, "access");
      value = value.substring(1, value.length() - 1);
      StringTokenizer st = new StringTokenizer(value, ",");
      while (st.hasMoreTokens()) {
        String[] array = st.nextToken().split("=");
        access.put(Integer.valueOf(array[0].replaceAll("\\s", "")), AccessLevel.valueOf(array[1].replaceAll("\\s", "").toUpperCase()));
      }

      value = connection.hget(token, "organizations");
      value = value.substring(1, value.length() - 1);
      Map<Integer, String> orgs = new HashMap<>();
      StringTokenizer st2 = new StringTokenizer(value, ",");
      while (st2.hasMoreTokens()) {
        String[] array = st2.nextToken().split("=");
        orgs.put(Integer.valueOf(array[0]), array[1]);
      }
      connection.expire(token, 604800);
      return new User(id, access, orgs, token);
    }
  }


}
