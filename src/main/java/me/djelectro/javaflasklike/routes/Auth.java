package me.djelectro.javaflasklike.routes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.http.Context;
import me.djelectro.javaflasklike.Route;
import me.djelectro.javaflasklike.enums.RouteType;
import me.djelectro.javaflasklike.routes.authorize.AuthHelpers;
import me.djelectro.javaflasklike.types.User;
import me.djelectro.javaflasklike.utils.PasswordFactory;
import me.djelectro.javaflasklike.utils.SecureTokenGenerator;

import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

/**
 * Authentication Module
 */
public class Auth extends Routes {

  public Auth(){
    super("/auth");
    shouldActivate = true;
  }

  /**
   * Get all user details
   * @param ctx Javalin Context for this call
   */
  @Route(url="/me", type= RouteType.GET, accessMethod = AuthHelpers.LoggedIn.class)
  public void getSelf(Context ctx){
    User rUser = ctx.attribute("user");
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      HashMap<String, Object> userMap = new HashMap<>();
      userMap.put("id", rUser.getUserId());
      userMap.put("fullName", rUser.getFullName());
      userMap.put("email", rUser.getEmail());
      userMap.put("accessMap", rUser.getAccessMap());
      userMap.put("billingClientId", rUser.getBlestaClientId());
      ctx.result(objectMapper.writeValueAsString(userMap));
    } catch (JsonProcessingException e) {
      logger.error(e.getMessage());
      ctx.result("There was an error retrieving user info, please contact support or try again later");
      ctx.status(500);
    }
  }

  /**
   * Register a user with the program
   * @param ctx Javalin Context for this call
   */
  @Route(url="/register", type=RouteType.POST, accessMethod = AuthHelpers.All.class)
  public void register(Context ctx){
    String fullName = ctx.formParam("username");
    String email = ctx.formParam("email");
    String pass = ctx.formParam("password");

    if(fullName == null || email == null || pass == null){
      ctx.status(401).result("Not all form information provided");
      return;
    }

    fullName = fullName.strip();
    email = email.strip();

    String encodedPass = PasswordFactory.getInstance().encodeString(pass);

    int res = User.commitUser(fullName, email, encodedPass);

    if(res == 0){
      ctx.status(200);
      ctx.result("Account created successfully");
    }
    else if (res == 2){
      ctx.status(401);
      ctx.result("User Already Exists");
    }
    else{
      ctx.status(500);
      ctx.result("There was an unknown error. Please contact support or try again later.");
    }
  }

  /**
   * Log a user info the program
   * @param ctx Javalin Context for this call
   */
  @Route(url="/login", type=RouteType.POST, accessMethod = AuthHelpers.All.class)
  public void login(Context ctx){

      String uname = ctx.formParam("email");
      String pass = ctx.formParam("password");


      if(uname == null || pass == null){
          ctx.status(401);
          ctx.result("Invalid username or password");
          return;
      }

      User thisUser;
      try {
          thisUser = new User(uname);
      } catch (Exception e) {
          ctx.status(401);
          ctx.result("User not found");
          return;
      }


      if(Objects.equals(thisUser.getHashedPassword(), PasswordFactory.getInstance().encodeString(pass))){
          // User is who they claim to be!
          String token = SecureTokenGenerator.nextToken();
          jedisConn.storeUser(thisUser, token);
          ctx.result(token);
      }
      else{
          ctx.status(401);
          ctx.result("Incorrect password");
      }


  }

  @Route(url = "/update", type= RouteType.POST, accessMethod = AuthHelpers.LoggedIn.class)
  public void updateUserProfile(Context ctx){
    User c = ctx.attribute("user");
    if(c == null){
      ctx.status(400);
      ctx.result("Invalid user");
      return;
    }

    // Compare user submitted values to old values. If they're different, call update functions
    String newFullName = ctx.formParam("name");
    String newEmail = ctx.formParam("email");

    if(newFullName == null || newEmail == null){
      ctx.status(400);
      ctx.result("Name or Email not specified in request");
      return;
    }
    newFullName = newFullName.strip();
    newEmail = newEmail.strip();

    if(!Objects.equals(newFullName, c.getFullName()) && !newFullName.isEmpty()){
      c.updateFullName(newFullName);
    }

    if(!Objects.equals(newEmail, c.getEmail()) && !newEmail.isEmpty()){
      c.updateEmail(newEmail);
    }

    ctx.result("Information updated successfully");
  }


}
