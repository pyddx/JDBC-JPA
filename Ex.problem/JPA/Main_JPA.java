import models.Actor;
import repository.ActorRepository;
import java.time.Instant;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        ActorRepository repo = new ActorRepository();

        // ════════════════════════════════════
        //  1) ดึงข้อมูลทั้งหมด
        // ════════════════════════════════════
        System.out.println("=== 📋 All Actors ===");
        List<Actor> all = repo.findAll();
        all.forEach(System.out::println);
        System.out.println("Total: " + all.size() + "\n");

        // ════════════════════════════════════
        //  2) ค้นหาด้วย ID
        // ════════════════════════════════════
        System.out.println("=== 🔍 Find ID=1 ===");
        repo.findById(1).ifPresentOrElse(
            a  -> System.out.println("Found: " + a),
            () -> System.out.println("Not found!")
        );
        System.out.println();

        // ════════════════════════════════════
        //  3) เพิ่มข้อมูลใหม่
        // ════════════════════════════════════
        System.out.println("=== ➕ Save New Actor ===");
        Actor newActor = new Actor();
        newActor.setFirstName("JOHN");
        newActor.setLastName("DOE");
        newActor.setLastUpdate(Instant.now());
        Actor saved = repo.save(newActor);
        System.out.println("Saved with ID: " + saved.getId() + "\n");

        // ════════════════════════════════════
        //  4) แก้ไขข้อมูล
        // ════════════════════════════════════
        System.out.println("=== ✏️ Update Actor ID=1 ===");
        repo.findById(1).ifPresent(actor -> {
            actor.setFirstName("PENELOPE UPDATED");
            actor.setLastUpdate(Instant.now());
            repo.update(actor);
            System.out.println("Updated: " + actor);
        });
        System.out.println();

        // ════════════════════════════════════
        //  5) ลบข้อมูล (ลบที่เพิ่งสร้าง)
        // ════════════════════════════════════
        System.out.println("=== 🗑️ Delete Actor ===");
        repo.deleteById(saved.getId());
        System.out.println();
    }
}
