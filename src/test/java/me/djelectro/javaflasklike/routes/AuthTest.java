package me.djelectro.javaflasklike.routes;

import io.javalin.http.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import me.djelectro.javaflasklike.DatabaseBasedTest;
import me.djelectro.javaflasklike.utils.PasswordFactory;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AuthTest extends DatabaseBasedTest {
  private final Auth inst = new Auth();

  private static final String[] userEmailPass = {"abcde123", "testmockuser@raisable.org", "abcde123"};

  @BeforeEach
  public void setDb(){
    inst.setDatabase(db);
  }

//  @Test
//  public void testGetSelf(@Mock Context ctx){
//    when(ctx.attribute("user")).thenReturn(new User(30));
//    inst.getSelf(ctx);
//    verify(ctx).result("{\"accessMap\":{\"1\":\"MEMBER\"},\"fullName\":\"Test User\",\"id\":30,\"email\":\"test123@example.com\"}");
//  }

  // Cant use this test atm

  @Test
  public void testRegisterAndLogin(@Mock Context ctx){
    // Setup
    when(ctx.formParam("username")).thenReturn(userEmailPass[0]);
    when(ctx.formParam("email")).thenReturn(userEmailPass[1]);
    when(ctx.formParam("password")).thenReturn(userEmailPass[2]);
    PasswordFactory.initFactory("hello");

    // Invoke
    inst.register(ctx);

    verify(ctx).status(200);
  }

}
