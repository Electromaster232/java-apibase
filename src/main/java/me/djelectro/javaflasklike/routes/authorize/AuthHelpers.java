package me.djelectro.javaflasklike.routes.authorize;

import io.javalin.http.Context;
import me.djelectro.javaflasklike.enums.AccessLevel;
import me.djelectro.javaflasklike.types.Types;
import me.djelectro.javaflasklike.types.User;

public class AuthHelpers extends Types {

  public AuthHelpers(){}
  public static boolean loggedIn(Context ctx){
    String cookieStr = ctx.header("Authorization");
    User resUser;
    if (cookieStr == null)
      return false;
    resUser = jedisConn.getUser(cookieStr);
    if (resUser != null) {
      ctx.attribute("user", resUser);
      return true;
    }
    return false;
  }

  public static class All implements Authorizer {
    @Override
    public boolean authorize(Context ctx, AccessLevel level) {
      return true;
    }
  }

  public static class LoggedIn implements Authorizer {
    @Override
    public boolean authorize(Context ctx, AccessLevel level) {
      return AuthHelpers.loggedIn(ctx);
    }
  }

}






