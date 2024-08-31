package me.djelectro.javaflasklike.types;

import me.djelectro.javaflasklike.enums.AccessLevel;
import me.djelectro.javaflasklike.utils.Func;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class User extends Types {

	// Static fields for this type

	// Local fields for this type
	private final int userId;
	private Map<Integer, AccessLevel> access;
	private Map<Integer, String> organizations;
	private String token;

  //cached fields
  private String fullName;
  private String email;


	// Get user from only their ID -- Default constructor
	public User(int userId){
		this.userId = userId;
	}

	public User(String email) throws Exception {
		int userid = getUserIdFromEmail(email);
    if(userid == 0){
      throw new RuntimeException("Invalid user!");
    }
		this.userId = userid;
	}

  public static int getUserIdFromEmail(String email) {
    Map<Integer, String[]> result = dbConn.executeAndReturnData("SELECT id FROM users WHERE email = ?", email);
    if(result.isEmpty()){
      return 0;
    }
    String[] user0data = result.get(result.entrySet().iterator().next().getKey());
    if(user0data == null)
      return 0;
    return Integer.parseInt(user0data[0]);
  }


  // This constructor is for use by Redis deseralizer
	public User(int userId, Map<Integer, AccessLevel> access, Map<Integer, String> orgs, String token){
		this.userId = userId;
		this.access = access;
		this.organizations = orgs;
		this.token = token;
	}

  public static int commitUser(String fullName, String email, String password){
    if(getUserIdFromEmail(email) != 0)
      return 2;

    boolean a = dbConn.executeUpdate("INSERT INTO users (email, password, name) VALUES (?,?,?)", email, password, fullName);
    if(a)
      return 0;
    return 1;

  }

	public String getFullName() {
    if (fullName == null) {
      fullName = dbConn.executeAndReturnData("SELECT name FROM users WHERE id = ?", userId).entrySet().iterator().next().getValue()[0];
    }
    return fullName;
  }
	public String getEmail() {
    if (email == null) {
      email = dbConn.executeAndReturnData("SELECT email FROM users WHERE id = ?", userId).entrySet().iterator().next().getValue()[0];
    }
    return email;
  }

	public Map<Integer, AccessLevel> getAccessMap(){
		if(this.access == null)
			refreshAccess();
		return access;
	}

	public Map<Integer, String> getOrganizationMap(){
		if(this.organizations == null)
			refreshOrganizations();
		return organizations;
	}

	public boolean hasCampaignAccess(int campaignId, AccessLevel accessLevel){

    // Check if campaign in list - this prevents NullPointerException if you ask for a campaign you are not in at all
    if(!getAccessMap().containsKey(campaignId)) {
      refreshAccess();
      // Check if STILL not in list
      if(!getAccessMap().containsKey(campaignId)) {
        return false;
      }
    }

    // If in list either before or after refresh, perform actual check...

		boolean result = switch (accessLevel) {
      case MEMBER -> Set.of(AccessLevel.MEMBER, AccessLevel.MANAGER, AccessLevel.ADMINISTRATOR).contains(getAccessMap().get(campaignId));
      case MANAGER -> Set.of(AccessLevel.MANAGER, AccessLevel.ADMINISTRATOR).contains(getAccessMap().get(campaignId));
      case ADMINISTRATOR -> Objects.equals(AccessLevel.ADMINISTRATOR, getAccessMap().get(campaignId));
    };
    if(!result){
			refreshAccess();
      switch (accessLevel) {
        case MEMBER-> result = Set.of(AccessLevel.MEMBER, AccessLevel.MANAGER, AccessLevel.ADMINISTRATOR).contains(getAccessMap().get(campaignId));
        case MANAGER -> result = Set.of(AccessLevel.MANAGER, AccessLevel.ADMINISTRATOR).contains(getAccessMap().get(campaignId));
        case ADMINISTRATOR -> result = Objects.equals(AccessLevel.ADMINISTRATOR, getAccessMap().get(campaignId));
      }
		}
		return result;
	}

  public Integer getBlestaClientId(){
    try {
      Map<Integer, String[]> dbRes = dbConn.executeAndReturnData("SELECT support_id FROM users WHERE id = ?", this.userId);
      if(dbRes.isEmpty())
        return null;
      return Integer.parseInt(dbRes.get(1)[0]);
    }catch (NumberFormatException e){
      logger.error(e.getMessage());
      return null;
    }
  }

	public void refreshAccess() {
		this.access = new HashMap<>();
		//access = dbConn.executeAndReturnData("SELECT email FROM users WHERE id = ?", userId).entrySet().iterator().next().getValue()[1];
		for(Map.Entry<Integer, String[]> i : dbConn.executeAndReturnData("SELECT * FROM campaign_associations WHERE userid = ?", userId).entrySet()){
			this.access.put(Integer.parseInt(i.getValue()[2]), AccessLevel.valueOf(i.getValue()[3].toUpperCase()));
		}
		if(this.token != null)
			jedisConn.storeUser(this, this.token);
	}

	public void refreshOrganizations() {
		this.organizations = new HashMap<>();
		//access = dbConn.executeAndReturnData("SELECT email FROM users WHERE id = ?", userId).entrySet().iterator().next().getValue()[1];
		for(Map.Entry<Integer, String[]> i : dbConn.executeAndReturnData("SELECT * FROM organization_associations WHERE userid = ?", userId).entrySet()){
			this.organizations.put(Integer.parseInt(i.getValue()[2]), i.getValue()[3]);
		}
		if(this.token != null)
			jedisConn.storeUser(this, this.token);
	}

	public int getUserId() {return userId;}
	public String getHashedPassword() {
		return dbConn.executeAndReturnData("SELECT password FROM users WHERE id = ?", userId).entrySet().iterator().next().getValue()[0];
	}

  public Integer getCampaignGoal(int campaignId){
    return Func.intParse(dbConn.executeAndReturnData("SELECT goal FROM campaign_associations WHERE userid = ? AND campaignid = ?", userId, campaignId).entrySet().iterator().next().getValue()[0]);
  }

  public boolean updateFullName(String newName){
    return dbConn.executeUpdate("UPDATE users SET name = ? WHERE id = ?", newName, userId);
  }

  public boolean updateEmail(String newEmail){
    return dbConn.executeUpdate("UPDATE users SET email = ? WHERE id = ?", newEmail, userId);
  }


}
