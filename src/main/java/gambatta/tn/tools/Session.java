package gambatta.tn.tools;

import gambatta.tn.entites.user.user;

public class Session {
    private static user currentUser;

    public static void setCurrentUser(user user) {
        currentUser = user;
    }

    public static user getCurrentUser() {
        return currentUser;
    }

    public static void clear() {
        currentUser = null;
    }

    public static boolean isAdmin() {
        return currentUser != null
                && currentUser.getRoles() != null
                && currentUser.getRoles().contains("ADMIN");
    }
}