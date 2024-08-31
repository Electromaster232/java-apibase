package me.djelectro.javaflasklike.routes.authorize;

import io.javalin.http.Context;
import me.djelectro.javaflasklike.enums.AccessLevel;

public interface Authorizer {

   boolean authorize(Context ctx, AccessLevel level);

}
