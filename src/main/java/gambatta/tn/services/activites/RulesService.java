package gambatta.tn.services.activites;

import gambatta.tn.entites.activites.rules;
import gambatta.tn.tools.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RulesService {

    Connection cnx;

    public RulesService() {
        cnx = MyDataBase.getInstance();
    }

    // ✅ CREATE
    public void add(rules r) {
        String sql = "INSERT INTO rules (activite_id, rule_description) VALUES (?, ?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, r.getActiviteId());
            ps.setString(2, r.getRuleDescription());
            ps.executeUpdate();
            System.out.println("✅ Rule added");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // ✅ READ
    public List<rules> getAll() {
        List<rules> list = new ArrayList<>();
        String sql = "SELECT * FROM rules";
        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                rules r = new rules();
                r.setId(rs.getInt("id"));
                r.setActiviteId(rs.getInt("activite_id"));
                r.setRuleDescription(rs.getString("rule_description"));
                list.add(r);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return list;
    }

    // ✅ DELETE
    public void delete(int id) {
        String sql = "DELETE FROM rules WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("🗑 Rule deleted");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // ✅ UPDATE
    public void update(rules r) {
        String sql = "UPDATE rules SET activite_id=?, rule_description=? WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, r.getActiviteId());
            ps.setString(2, r.getRuleDescription());
            ps.setInt(3, r.getId());
            ps.executeUpdate();
            System.out.println("✏️ Rule updated");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
