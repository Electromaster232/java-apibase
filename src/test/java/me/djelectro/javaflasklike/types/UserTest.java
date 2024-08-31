package me.djelectro.javaflasklike.types;

import me.djelectro.javaflasklike.DatabaseBasedTest;
import me.djelectro.javaflasklike.enums.AccessLevel;
import org.junit.jupiter.api.Test;
import org.zapodot.junit.db.annotations.EmbeddedDatabase;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;


public class UserTest extends DatabaseBasedTest {


  @Test
  public void testCommitUser() {
    // Test creation of user
    int actual = User.commitUser("Test ABC123", "test@example.com", "test");
    assertEquals(0, actual);

    // Test we can retrieve this user

    assertDoesNotThrow(() ->{
      User u = new User("test@example.com");
      assertEquals("Test ABC123", u.getFullName());
    });

  }





}
