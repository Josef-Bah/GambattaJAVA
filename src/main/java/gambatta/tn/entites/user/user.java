package gambatta.tn.entites.user;

public class user {
    private int id;
    private String email;
    private String roles;
    private String password;
    private String firstName;
    private String lastName;
    private String numTel;
    private String profileImage; // ← AJOUTÉ

    public user() {}

    public user(String email, String roles, String password,
                String firstName, String lastName, String numTel) {
        this.email = email;
        this.roles = roles;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.numTel = numTel;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRoles() { return roles; }
    public void setRoles(String roles) { this.roles = roles; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getNumTel() { return numTel; }
    public void setNumTel(String numTel) { this.numTel = numTel; }
    public String getProfileImage() { return profileImage; }           // ← AJOUTÉ
    public void setProfileImage(String profileImage) {                 // ← AJOUTÉ
        this.profileImage = profileImage;
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", email='" + email + "', name='" +
                firstName + " " + lastName + "', roles='" + roles + "'}";
    }
}