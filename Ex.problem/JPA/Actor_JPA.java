package models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.time.Instant;

// ╔══════════════════════════════════════════════════════════╗
// ║  🔧 แก้ตรงนี้ถ้าโจทย์ให้ตารางอื่น                      ║
// ║  @Table(name=...)     → ชื่อตารางใน DB                  ║
// ║  @Column(name=...)    → ชื่อคอลัมน์ใน DB               ║
// ║  ชื่อ field (Java)   → ตั้งชื่ออะไรก็ได้               ║
// ╚══════════════════════════════════════════════════════════╝

@Getter
@Setter
@ToString
@Entity                          // ← บอก JPA ว่า class นี้ = ตารางใน DB
@Table(name = "actor")           // ← แก้ชื่อตาราง
public class Actor {

    @Id                                                           // ← Primary Key
    @GeneratedValue(strategy = GenerationType.IDENTITY)          // ← Auto increment
    @Column(name = "actor_id")                                   // ← ชื่อคอลัมน์ PK
    private Integer id;

    // ← แก้ @Column(name=...) ให้ตรงกับชื่อคอลัมน์ใน DB
    @Column(name = "first_name", nullable = false, length = 45)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 45)
    private String lastName;

    @Column(name = "last_update", nullable = false)
    private Instant lastUpdate;
}
