package me.djelectro.javaflasklike.enums;

@Deprecated
public enum RouteAccess {

    /** Access is permitted for all users */
    ALL,
    /** Access is only permitted to users with a valid session token for ANY campaign (or no campaign!) */
    LOGGED_IN,
    /** Access is granted based on the result of the function specified in the accessMethod parameter */
    METHOD
}
