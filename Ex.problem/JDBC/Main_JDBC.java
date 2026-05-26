import Model.Actor;
import Repository.ActorDAO;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        ActorDAO dao = new ActorDAO();  // ← เปลี่ยนชื่อ DAO ตามโจทย์

        // ════════════════════════════════════
        //  1) ดึงข้อมูลทั้งหมด
        // ════════════════════════════════════
        System.out.println("=== 📋 All Actors ===");
        List<Actor> all = dao.findAll();
        all.forEach(System.out::println);
        System.out.println("Total: " + all.size() + " records\n");

        // ════════════════════════════════════
        //  2) ค้นหาด้วย ID
        // ════════════════════════════════════
        System.out.println("=== 🔍 Find ID = 1 ===");
        dao.findById(1).ifPresentOrElse(
            actor -> System.out.println("Found: " + actor),
            ()     -> System.out.println("Not found!")
        );
        System.out.println();

        // ════════════════════════════════════
        //  3) เพิ่มข้อมูลใหม่
        // ════════════════════════════════════
        System.out.println("=== ➕ Save New Actor ===");
        Actor newActor = new Actor();
        newActor.setFirstName("JOHN");    // ← ใส่ค่าที่ต้องการ
        newActor.setLastName("DOE");
        // lastUpdate จะถูก set อัตโนมัติใน save()
        dao.save(newActor);
        System.out.println();

        // ════════════════════════════════════
        //  4) แก้ไขข้อมูล (ต้อง find ก่อน แล้วค่อย update)
        // ════════════════════════════════════
        System.out.println("=== ✏️ Update Actor ID=1 ===");
        dao.findById(1).ifPresent(actor -> {
            actor.setFirstName("PENELOPE UPDATED");   // ← แก้ค่าที่ต้องการ
            actor.setLastName("GUINESS UPDATED");
            dao.update(actor);
        });
        System.out.println();

        // ════════════════════════════════════
        //  5) ลบข้อมูล
        // ════════════════════════════════════
        System.out.println("=== 🗑️ Delete Actor ID=205 ===");
        dao.delete(205);    // ← ใส่ ID ที่ต้องการลบ
        System.out.println();

        // ════════════════════════════════════
        //  ตรวจสอบหลัง update/delete
        // ════════════════════════════════════
        System.out.println("=== 📋 All Actors After Changes ===");
        dao.findAll().forEach(System.out::println);
    }
}
