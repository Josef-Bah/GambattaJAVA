package gambatta.tn.services.user;

import gambatta.tn.entites.user.user;
import gambatta.tn.services.IService;
import gambatta.tn.tools.MyDataBase;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService implements IService<user> {

    private final Connection cnx = MyDataBase.getInstance();

    @Override
    public void ajouter(user u) {
        String sql = "INSERT INTO user (email, roles, password, first_name, last_name, num_tel) VALUES (?,?,?,?,?,?)";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            String passwordToStore = u.getPassword();

            if (passwordToStore != null && !passwordToStore.isBlank() && !isBCryptHash(passwordToStore)) {
                passwordToStore = BCrypt.hashpw(passwordToStore, BCrypt.gensalt());
            }

            ps.setString(1, u.getEmail());
            ps.setString(2, u.getRoles());
            ps.setString(3, passwordToStore);
            ps.setString(4, u.getFirstName());
            ps.setString(5, u.getLastName());
            ps.setString(6, u.getNumTel());
            ps.executeUpdate();

            System.out.println("✅ Utilisateur ajouté avec succès.");

        } catch (SQLException e) {
            throw new RuntimeException("Erreur ajouter utilisateur : " + e.getMessage(), e);
        }
    }

    @Override
    public void modifier(user u) {
        String sql = "UPDATE user SET email=?, roles=?, password=?, first_name=?, last_name=?, num_tel=? WHERE id=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            String passwordToStore = u.getPassword();

            if (passwordToStore != null && !passwordToStore.isBlank() && !isBCryptHash(passwordToStore)) {
                passwordToStore = BCrypt.hashpw(passwordToStore, BCrypt.gensalt());
            }

            ps.setString(1, u.getEmail());
            ps.setString(2, u.getRoles());
            ps.setString(3, passwordToStore);
            ps.setString(4, u.getFirstName());
            ps.setString(5, u.getLastName());
            ps.setString(6, u.getNumTel());
            ps.setInt(7, u.getId());
            ps.executeUpdate();

            System.out.println("✅ Utilisateur modifié avec succès.");

        } catch (SQLException e) {
            throw new RuntimeException("Erreur modifier utilisateur : " + e.getMessage(), e);
        }
    }

    @Override
    public void supprimer(int id) {
        String sql = "DELETE FROM user WHERE id=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("✅ Utilisateur supprimé avec succès.");
        } catch (SQLException e) {
            throw new RuntimeException("Erreur supprimer utilisateur : " + e.getMessage(), e);
        }
    }

    @Override
    public List<user> afficher() {
        List<user> liste = new ArrayList<>();
        String sql = "SELECT * FROM user ORDER BY id DESC";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                liste.add(mapRow(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur afficher utilisateurs : " + e.getMessage(), e);
        }

        return liste;
    }

    @Override
    public user getById(int id) {
        String sql = "SELECT * FROM user WHERE id=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapRow(rs);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur getById : " + e.getMessage(), e);
        }

        return null;
    }

    public user getByEmail(String email) {
        String sql = "SELECT * FROM user WHERE email=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapRow(rs);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur getByEmail : " + e.getMessage(), e);
        }

        return null;
    }

    public boolean emailExiste(String email) {
        return getByEmail(email) != null;
    }

    public user login(String email, String password) {
        String sql = "SELECT * FROM user WHERE email=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                user u = mapRow(rs);
                String storedPassword = u.getPassword();

                if (storedPassword == null || storedPassword.isBlank()) {
                    return null;
                }

                // Cas 1 : mot de passe déjà hashé BCrypt
                if (isBCryptHash(storedPassword)) {
                    if (BCrypt.checkpw(password, storedPassword)) {
                        return u;
                    }
                    return null;
                }

                // Cas 2 : ancien mot de passe en texte brut
                if (password.equals(storedPassword)) {
                    // migration automatique vers BCrypt
                    String hashed = BCrypt.hashpw(password, BCrypt.gensalt());
                    u.setPassword(hashed);
                    modifier(u);
                    return u;
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur login : " + e.getMessage(), e);
        }

        return null;
    }

    public void logLogin(user u) {
        String sql = "INSERT INTO login_audit (user_id, email) VALUES (?, ?)";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, u.getId());
            ps.setString(2, u.getEmail());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur journal login : " + e.getMessage(), e);
        }
    }

    public int countUsers() {
        return countQuery("SELECT COUNT(*) FROM user");
    }

    public int countAdmins() {
        return countQuery("SELECT COUNT(*) FROM user WHERE roles LIKE '%ADMIN%'");
    }

    public int countSimpleUsers() {
        return countQuery("SELECT COUNT(*) FROM user WHERE roles NOT LIKE '%ADMIN%'");
    }

    public int countLoginsToday() {
        return countQuery("SELECT COUNT(*) FROM login_audit WHERE DATE(login_time)=CURDATE()");
    }

    public int countAllLogins() {
        return countQuery("SELECT COUNT(*) FROM login_audit");
    }

    private int countQuery(String sql) {
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur stats : " + e.getMessage(), e);
        }

        return 0;
    }

    private boolean isBCryptHash(String value) {
        return value != null &&
                (value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$"));
    }

    private user mapRow(ResultSet rs) throws SQLException {
        user u = new user();
        u.setId(rs.getInt("id"));
        u.setEmail(rs.getString("email"));
        u.setRoles(rs.getString("roles"));
        u.setPassword(rs.getString("password"));
        u.setFirstName(rs.getString("first_name"));
        u.setLastName(rs.getString("last_name"));
        u.setNumTel(rs.getString("num_tel"));
        return u;
    }
    // Inscriptions par mois (6 derniers mois)
    public java.util.Map<String, Integer> countUsersParMois() {
        java.util.Map<String, Integer> result = new java.util.LinkedHashMap<>();
        String sql = "SELECT DATE_FORMAT(created_at, '%Y-%m') as mois, COUNT(*) as total " +
                "FROM user WHERE created_at IS NOT NULL " +
                "AND created_at >= DATE_SUB(NOW(), INTERVAL 6 MONTH) " +
                "GROUP BY mois ORDER BY mois ASC";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                result.put(rs.getString("mois"), rs.getInt("total"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur stats par mois : " + e.getMessage(), e);
        }
        return result;
    }

    // Répartition des rôles
    public java.util.Map<String, Integer> countParRole() {
        java.util.Map<String, Integer> result = new java.util.LinkedHashMap<>();
        String sql = "SELECT roles, COUNT(*) as total FROM user GROUP BY roles";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String role = rs.getString("roles")
                        .replace("[\"", "").replace("\"]", "");
                result.put(role, rs.getInt("total"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur stats rôles : " + e.getMessage(), e);
        }
        return result;
    }

    // Actifs
    public int countActifs() {
        return countQuery("SELECT COUNT(*) FROM user WHERE status = 'active'");
    }

    // Inactifs
    public int countInactifs() {
        return countQuery("SELECT COUNT(*) FROM user WHERE status != 'active' OR status IS NULL");
    }

}