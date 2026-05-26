package Repository;

import DOA.ConnectionFactory;
import DOA.JdbcDAO;
import Model.Actor;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ActorDAO implements JdbcDAO<Actor, Integer> {

    // ╔══════════════════════════════════════════════════════════╗
    // ║  🔧 แก้ตรงนี้ถ้าโจทย์ให้ตารางอื่น                      ║
    // ║  → เปลี่ยนชื่อตาราง และชื่อคอลัมน์ให้ตรงกับ DB         ║
    // ╚══════════════════════════════════════════════════════════╝
    private static final String FIND_ALL   = "SELECT * FROM actor";
    private static final String FIND_BY_ID = "SELECT * FROM actor WHERE actor_id = ?";
    //                                                                 ↑ ชื่อคอลัมน์ PK
    private static final String INSERT     =
        "INSERT INTO actor (first_name, last_name, last_update) VALUES (?, ?, ?)";
    //                      ↑ คอลัมน์ที่จะ insert (ไม่ใส่ PK เพราะ auto increment)
    private static final String UPDATE     =
        "UPDATE actor SET first_name = ?, last_name = ?, last_update = ? WHERE actor_id = ?";
    //                     ↑ คอลัมน์ที่จะ update                              ↑ PK
    private static final String DELETE     = "DELETE FROM actor WHERE actor_id = ?";
    //                                                                ↑ PK

    // ══════════════════════════════════════════════════════════════
    //  Helper: แปลง ResultSet (1 แถว) → Actor object
    //  🔧 แก้ชื่อคอลัมน์ใน rs.getXxx("...") ให้ตรงกับ DB
    // ══════════════════════════════════════════════════════════════
    private Actor mapRow(ResultSet rs) throws SQLException {
        Actor actor = new Actor();
        actor.setActorId(rs.getInt("actor_id"));                                    // ← ชื่อคอลัมน์ PK
        actor.setFirstName(rs.getString("first_name"));                             // ← ชื่อคอลัมน์
        actor.setLastName(rs.getString("last_name"));                               // ← ชื่อคอลัมน์
        actor.setLastUpdate(rs.getObject("last_update", LocalDateTime.class));      // ← ชื่อคอลัมน์
        return actor;
    }

    // ══════════════════════════════════════════════════════════════
    //  findAll() — ดึงทุกแถว
    // ══════════════════════════════════════════════════════════════
    @Override
    public List<Actor> findAll() {
        List<Actor> list = new ArrayList<>();
        try (Connection con = ConnectionFactory.getConnection();          // เปิด connection
             Statement  st  = con.createStatement();                      // สร้าง statement
             ResultSet  rs  = st.executeQuery(FIND_ALL)) {                // รัน SQL

            while (rs.next()) {          // วนทีละแถว
                list.add(mapRow(rs));    // แปลงเป็น Object แล้วใส่ list
            }

        } catch (SQLException e) {
            throw new RuntimeException("findAll error: " + e.getMessage(), e);
        }
        return list;
    }

    // ══════════════════════════════════════════════════════════════
    //  findById() — ค้นหาด้วย ID → return Optional (ปลอดภัย ไม่ null)
    // ══════════════════════════════════════════════════════════════
    @Override
    public Optional<Actor> findById(Integer id) {
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(FIND_BY_ID)) {

            ps.setInt(1, id);                        // ใส่ค่า id แทน ?

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {                     // เจอ → ห่อด้วย Optional
                    return Optional.of(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("findById error: " + e.getMessage(), e);
        }
        return Optional.empty();                     // ไม่เจอ → ส่ง empty กลับ
    }

    // ══════════════════════════════════════════════════════════════
    //  save() — INSERT ข้อมูลใหม่
    //  🔧 แก้ลำดับ setXxx() ให้ตรงกับลำดับ ? ใน INSERT SQL
    // ══════════════════════════════════════════════════════════════
    @Override
    public void save(Actor actor) {
        try (Connection con = ConnectionFactory.getConnection()) {
            con.setAutoCommit(false);               // ปิด auto-commit → จัดการ transaction เอง

            try (PreparedStatement ps = con.prepareStatement(INSERT)) {
                //  ? ที่ 1           ? ที่ 2            ? ที่ 3
                ps.setString(1, actor.getFirstName()); // ← first_name
                ps.setString(2, actor.getLastName());  // ← last_name
                ps.setObject(3, LocalDateTime.now());  // ← last_update (ใช้เวลาปัจจุบัน)

                ps.executeUpdate();   // รัน INSERT
                con.commit();         // ✅ บันทึกสำเร็จ
                System.out.println("✅ Save success: " + actor.getFirstName());

            } catch (SQLException e) {
                con.rollback();       // ❌ ถ้าผิดพลาด → ย้อนกลับ
                throw new RuntimeException("save error: " + e.getMessage(), e);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Connection error: " + e.getMessage(), e);
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  update() — แก้ไขข้อมูล (ใช้ actorId เป็น WHERE)
    //  🔧 แก้ลำดับ setXxx() ให้ตรงกับลำดับ ? ใน UPDATE SQL
    // ══════════════════════════════════════════════════════════════
    @Override
    public void update(Actor actor) {
        try (Connection con = ConnectionFactory.getConnection()) {
            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement(UPDATE)) {
                //  ? ที่ 1                  ? ที่ 2                 ? ที่ 3                  ? ที่ 4 (WHERE)
                ps.setString(1, actor.getFirstName()); // ← first_name
                ps.setString(2, actor.getLastName());  // ← last_name
                ps.setObject(3, LocalDateTime.now());  // ← last_update
                ps.setInt(4, actor.getActorId());      // ← WHERE actor_id = ?

                int rows = ps.executeUpdate();
                con.commit();
                System.out.println("✅ Updated " + rows + " row(s), ID=" + actor.getActorId());

            } catch (SQLException e) {
                con.rollback();
                throw new RuntimeException("update error: " + e.getMessage(), e);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Connection error: " + e.getMessage(), e);
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  delete() — ลบด้วย ID
    // ══════════════════════════════════════════════════════════════
    @Override
    public void delete(Integer id) {
        try (Connection con = ConnectionFactory.getConnection()) {
            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement(DELETE)) {
                ps.setInt(1, id);         // ← WHERE actor_id = ?
                int rows = ps.executeUpdate();
                con.commit();
                System.out.println("✅ Deleted " + rows + " row(s), ID=" + id);

            } catch (SQLException e) {
                con.rollback();
                throw new RuntimeException("delete error: " + e.getMessage(), e);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Connection error: " + e.getMessage(), e);
        }
    }
}
