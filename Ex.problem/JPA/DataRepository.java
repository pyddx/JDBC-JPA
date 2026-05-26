package repository;

import jakarta.persistence.*;
import java.util.List;
import java.util.Optional;

// ไม่ต้องแก้ไฟล์นี้เลย! ยกเว้นบรรทัด createEntityManagerFactory
public interface DataRepository<E, T> {

    // ╔══════════════════════════════════════════════════════════╗
    // ║  🔧 แก้ "sakila" ให้ตรงกับ name ใน persistence.xml      ║
    // ╚══════════════════════════════════════════════════════════╝
    EntityManagerFactory emf =
        Persistence.createEntityManagerFactory("sakila"); // ← แก้ชื่อ persistence-unit

    Class<E> getEntityClass(); // แต่ละ Repository implement อันนี้อย่างเดียว

    default EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    // ── save ──────────────────────────────────────────────────
    default E save(E entity) {
        EntityManager em = getEntityManager();
        em.getTransaction().begin();
        em.persist(entity);             // INSERT
        em.getTransaction().commit();
        E managed = em.merge(entity);   // sync กลับมาให้มี id ที่ DB generate
        em.close();
        return managed;
    }

    // ── findAll ───────────────────────────────────────────────
    default List<E> findAll() {
        EntityManager em = getEntityManager();
        try {
            // JPQL: ใช้ชื่อ Class ไม่ใช่ชื่อตาราง!
            String jpql = "select e from " + getEntityClass().getSimpleName() + " e";
            return em.createQuery(jpql, getEntityClass()).getResultList();
        } finally {
            em.close();
        }
    }

    // ── findById ──────────────────────────────────────────────
    default Optional<E> findById(T id) {
        EntityManager em = getEntityManager();
        try {
            return Optional.ofNullable(em.find(getEntityClass(), id));
        } finally {
            em.close();
        }
    }

    // ── delete ────────────────────────────────────────────────
    default void delete(E entity) {
        EntityManager em = getEntityManager();
        em.getTransaction().begin();
        em.remove(em.merge(entity)); // merge ก่อน (detach → managed) แล้วค่อย remove
        em.getTransaction().commit();
        em.close();
    }

    default void deleteById(T id) {
        EntityManager em = getEntityManager();
        em.getTransaction().begin();
        E found = em.find(getEntityClass(), id);
        if (found != null) {
            em.remove(found);
            System.out.println("✅ Deleted ID=" + id);
        } else {
            System.out.println("❌ ID=" + id + " not found");
        }
        em.getTransaction().commit();
        em.close();
    }

    // ── update ────────────────────────────────────────────────
    default E update(E entity) {
        EntityManager em = getEntityManager();
        em.getTransaction().begin();
        E merged = em.merge(entity);  // merge = update ถ้า id มีอยู่แล้ว
        em.getTransaction().commit();
        em.close();
        return merged;
    }
}
