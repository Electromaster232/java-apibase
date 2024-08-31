package me.djelectro.javaflasklike;

import io.javalin.Javalin;
import io.javalin.plugin.bundled.CorsPluginConfig;
import me.djelectro.javaflasklike.routes.Routes;
import me.djelectro.javaflasklike.types.Types;
import me.djelectro.javaflasklike.config.Config;
import me.djelectro.javaflasklike.config.ConfigEnvWriter;
import me.djelectro.javaflasklike.utils.Database;
import me.djelectro.javaflasklike.utils.PasswordFactory;
import me.djelectro.javaflasklike.utils.Redis;
import me.djelectro.javaflasklike.utils.Version;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static org.reflections.scanners.Scanners.SubTypes;

public class HelloWorld {
  private static final Logger logger = LoggerFactory.getLogger(HelloWorld.class);

  private static void printUsage(){
    System.out.println("Start the Java-FlaskLike dashboard backend.");
    System.out.println("Usage:\n");
    System.out.println("dashboard-BETA.jar <configFile>");
    System.out.println("Where configFile is the file to read for configuration options");
    System.out.println("\nTo use config writer, run 'dashboard-BETA.jar config'");
  }

  public static void main(String[] args) throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
    // Read in params, figure out if we need to branch. If we ever add more than just two launch modes, we will need to split this into multiple classes.
    if (args.length < 1){
      printUsage();
      System.exit(1);
    }

    // Branch to config if config option specified
    if(Objects.equals(args[0], "config")){
      ConfigEnvWriter.main(args);
      System.exit(0);
    }


    // ================ START DASHBOARD APPLICATION =================
    // Read config
    Config config = null;
    try {
      config = new Config(args[0]);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    if (config.getConfigSize() < 13) {
      logger.error("ERROR: Not enough config parameters specified.");
      System.exit(1);
    }

    // Assign global config instance to the Config we just created
    Config.instance = config;

    // Initialize program, read version information

    System.out.println("=======================================");
    System.out.println("Welcome to Java FlaskLike" + Version.getReleaseInformation());
    System.out.println("Implementing API Version: " + Version.API_REVISION);
    System.out.println("Copyright © 2019-2024 Adam Gilbert and Endless Group. All Rights Reserved.");
    System.out.println("=======================================");

    Javalin app = null;
    boolean devMode = Objects.equals(config.getConfigValue("devMode"), "true");
    int port = Integer.parseInt(config.getConfigValue("serverPort"));

    try {
      // Check for dev mode
      if (devMode) {
        logger.warn("CAUTION: Development mode enabled. This will result in the use and creation of INSECURE TOKENS.\nDO NOT USE IN PRODUCTION!");
        app = Javalin.create(Jconfig ->
        {
          Jconfig.staticFiles.add(staticFileConfig -> {
            staticFileConfig.hostedPath = "/static";
            staticFileConfig.directory = "/public";
          });
          Jconfig.plugins.enableCors(cors -> {
            //replacement for enableCorsForAllOrigins()
            cors.add(CorsPluginConfig::anyHost);
          });
          Jconfig.plugins.enableDevLogging();
        }).start(port);
      } else {
        app = Javalin.create(Jconfig ->
        {
          Jconfig.staticFiles.add(staticFileConfig -> {
            staticFileConfig.hostedPath = "/static";
            staticFileConfig.directory = "/public";
          });
          Jconfig.plugins.enableCors(cors -> {
            //replacement for enableCorsForAllOrigins()
            cors.add(CorsPluginConfig::anyHost);
          });
        }).start(port);
      }
    } catch (io.javalin.util.JavalinBindException e) {
      if (devMode) {
        logger.error("Port " + port + " appears to already be in use, and development mode is enabled. Assuming you are already running the backend elsewhere. Exiting gracefully.");
        System.exit(0);
      } else {
        logger.error("Port " + port + " appears to already be in use. Program exiting. Please check configuration.");
        System.exit(1);
      }
    }
    // This should never be needed but is here just in case we somehow get through the catch above.
    if (app == null)
      System.exit(2);

    // Start loading tasks
    app.get("/", ctx -> ctx.header("Content-Type", "application/json").result(Version.getReleaseInformationMapAsJsonString()));

    // Prepare to reflect across all classes in the routes package
    List<ClassLoader> classLoadersList = new LinkedList<>();
    classLoadersList.add(ClasspathHelper.contextClassLoader());
    classLoadersList.add(ClasspathHelper.staticClassLoader());

    Reflections reflections = new Reflections(new ConfigurationBuilder().setUrls(ClasspathHelper.forClassLoader(classLoadersList.toArray(new ClassLoader[0]))).forPackage("org.raisable.dashboard").filterInputsBy(new FilterBuilder().includePackage("org.raisable.dashboard.routes")).setScanners(SubTypes.filterResultsBy(c -> true)));


    Database dbConn = new Database(config.getConfigValue("mySQLHost"),
      config.getConfigValue("mySQLPort"), config.getConfigValue("mySQLDB"),
      config.getConfigValue("mySQLUser"), config.getConfigValue("mySQLPass"), devMode);

    Redis jedis = new Redis(config.getConfigValue("redisHost"), config.getConfigValue("redisPort"), config.getConfigValue("redisPass"), Integer.parseInt(config.getConfigValue("redisUserTable")), Integer.parseInt(config.getConfigValue("redisCampaignTable")));
    Types.setJedisConn(jedis);
    Types.setDatabase(dbConn);


    // Insert post boot tasks here
    PasswordFactory.initFactory(config.getConfigValue("passwordSalt"));

    // ALL STARTUP TASKS MUST GO BEFORE WE LOAD ROUTES. ONCE ROUTES ARE LOADED THEY WILL IMMEDIATELY BEGIN ACCEPTING REQUESTS
    // ======================================================================================================================

    // After searching for all routes in the routes package, try to instantiate it if it is a subclass of routes.
    // We want to ignore the abstract Routes class because it cannot be instantiated (abstract and no param-less constructor)
    for (Class<?> classes : reflections.get(SubTypes.of(Object.class).asClass())) {
      if (classes.getSuperclass() == Routes.class) {
        //if (!classes.getName().equals("org.raisable.dashboard.routes.Routes")) {
        Routes r = (Routes) classes.getDeclaredConstructor().newInstance();
        if (r.shouldActivate()) {
          r.registerRoutes(app, dbConn, jedis);
          logger.info("Activated " + r);
        }
        //}
      }
    }
  }
}
