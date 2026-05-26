package Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

// ╔══════════════════════════════════════════════════════╗
// ║  🔧 ถ้าโจทย์ให้ตารางอื่น แก้ตรงนี้                 ║
// ║  1. ชื่อ class  → ชื่อ Entity เช่น Film, City       ║
// ║  2. field       → ตรงกับคอลัมน์ในตาราง DB           ║
// ╚══════════════════════════════════════════════════════╝

@Data               // สร้าง getter + setter + toString ให้อัตโนมัติ
@AllArgsConstructor // constructor ใส่ทุก field
@NoArgsConstructor  // constructor ว่าง (JPA/JDBC ต้องการ)
public class Actor {

    // ── field ตรงกับคอลัมน์ใน DB ──────────────────────
    private Integer       actorId;    // actor_id    (PK)
    private String        firstName;  // first_name
    private String        lastName;   // last_name
    private LocalDateTime lastUpdate; // last_update
}
