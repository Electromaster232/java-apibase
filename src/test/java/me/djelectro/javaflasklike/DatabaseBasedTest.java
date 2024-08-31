package me.djelectro.javaflasklike;

import me.djelectro.javaflasklike.types.Types;
import me.djelectro.javaflasklike.utils.Database;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.zapodot.junit.db.annotations.EmbeddedDatabase;
import org.zapodot.junit.db.annotations.EmbeddedDatabaseTest;
import org.zapodot.junit.db.common.Engine;

import javax.sql.DataSource;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertTrue;

@EmbeddedDatabaseTest(
  engine = Engine.H2,
  initialSqlResources = "classpath:raisable.sql"

)
public abstract class DatabaseBasedTest {

  private boolean userPrepared = false;
  protected Database db;

  protected void prepareUserTests(DataSource db){
    if(!userPrepared){
      try {
        db.getConnection().prepareStatement("INSERT INTO users (id, email, password, name) VALUES (30, 'test123@example.com', 'test', 'Test User');").execute();
        userPrepared = true;
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @BeforeEach
  public void setUp(final @EmbeddedDatabase DataSource dc){
    if(db == null)
      db = new Database(dc);
    Types.setDatabase(db);
    prepareUserTests(dc);

  }


  @Test
  public void testDatabaseConnected(){
    assertTrue(Types.testDatabase());
  }


}
