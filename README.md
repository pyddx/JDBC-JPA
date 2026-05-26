# JDBC-JPA
# 📚 Java Database — โพย JDBC & JPA สอบ INT242

> ✅ ดูตรงนี้ก่อนทำข้อสอบ | แก้ไขแค่ **ส่วนที่มีลูกศร ←** เท่านั้น

---

## 📋 สารบัญ
- [ส่วนที่ 1: JDBC (เขียน SQL เอง)](#-ส่วนที่-1-jdbc)
- [ส่วนที่ 2: JPA (ไม่ต้องเขียน SQL)](#-ส่วนที่-2-jpa)
- [ส่วนที่ 3: pom.xml dependencies](#-ส่วนที่-3-pomxml)
- [ส่วนที่ 4: โครงสร้างโปรเจค](#-ส่วนที่-4-โครงสร้างโปรเจค)
- [ส่วนที่ 5: เปรียบเทียบ JDBC vs JPA](#-ส่วนที่-5-เปรียบเทียบ)

---

## 🔌 ส่วนที่ 1: JDBC

### โครงสร้างไฟล์ JDBC
```
src/
├── DOA/
│   ├── connectionFactory.java   ← เชื่อม DB
│   └── JDBCDOA.java             ← Interface กำหนด method
├── Model/
│   └── City.java                ← Entity (แก้ชื่อ/field ตามโจทย์)
├── Repository/
│   └── CityDAO.java             ← เขียน SQL จริงๆ
└── Test.java                    ← ทดสอบ
```

---

### 1️⃣ connectionFactory.java
> **แก้:** url, user, pwd ตามโจทย์

```java
package DOA;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class connectionFactory {
    //  ← แก้ชื่อ DB ตามโจทย์ (เช่น sakila, demo_db)
    private static String url = "jdbc:mysql://localhost:3306/sakila";
    private static String user = "root";       // ← แก้ user
    private static String pwd  = "yourpassword"; // ← แก้ password

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver not found", e);
        }
        return DriverManager.getConnection(url, user, pwd);
    }
}
```

---

### 2️⃣ JDBCDAO.java (Interface)
> **แก้:** ชื่อ Entity และ method ตามที่โจทย์กำหนด

```java
package DOA;
import java.util.List;
import java.util.Optional;

//        ← T = ชื่อ Entity เช่น City, Film, Product
//        ← I = ประเภท Primary Key เช่น Integer
public interface JDBCDAO<T, I> {
    Optional<T> find(I id);     // หาด้วย ID
    List<T>     getAll();       // ดึงทั้งหมด
    void        save(T entity); // บันทึกใหม่
    void        update(T entity); // แก้ไข
    void        delete(I id);   // ลบ
}
```

---

### 3️⃣ City.java (Model/Entity)
> **แก้:** ชื่อ class, ชื่อ field ตามตารางใน DB

```java
package Model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data               // getter + setter + toString อัตโนมัติ
@AllArgsConstructor // constructor ครบทุก field
@NoArgsConstructor  // constructor ว่าง
public class City {
    // ← แก้ field ตามคอลัมน์ในตาราง DB
    private int    IDCity;      // city_id
    private String City;        // city
    private int    IDCountry;   // country_id
    private java.time.LocalDateTime lastUpdate; // last_update
}
```

**ถ้าโจทย์ให้ตาราง Film ก็เปลี่ยนเป็น:**
```java
public class Film {
    private int    id;          // film_id
    private String title;       // title
    private String releaseYear; // release_year
    private String rating;      // rating
}
```

---

### 4️⃣ CityDAO.java (Repository — ส่วนสำคัญที่สุด)
> **แก้:** ชื่อตาราง, ชื่อคอลัมน์, ชื่อ field ใน setter

```java
package Repository;
import DOA.JDBCDAO;
import DOA.connectionFactory;
import Model.City;
import java.sql.*;
import java.util.*;

public class CityDAO implements JDBCDAO<City, Integer> {

    // ← แก้ชื่อตาราง และคอลัมน์ตามโจทย์
    private static final String SELECT_ALL   = "SELECT * FROM city";
    private static final String SELECT_BY_ID = "SELECT * FROM city WHERE city_id = ?";
    private static final String INSERT       = "INSERT INTO city (city, country_id, last_update) VALUES (?, ?, ?)";
    private static final String UPDATE       = "UPDATE city SET city=?, country_id=? WHERE city_id=?";
    private static final String DELETE       = "DELETE FROM city WHERE city_id=?";

    // Helper: แปลง ResultSet → Object
    // ← แก้ชื่อคอลัมน์ และ setter ให้ตรงกับ Entity
    private City mapRow(ResultSet rs) throws SQLException {
        City city = new City();
        city.setIDCity(rs.getInt("city_id"));
        city.setCity(rs.getString("city"));
        city.setIDCountry(rs.getInt("country_id"));
        city.setLastUpdate(rs.getObject("last_update", java.time.LocalDateTime.class));
        return city;
    }

    // ── findById ──────────────────────────────────
    @Override
    public Optional<City> find(Integer id) {
        try (Connection con = connectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_BY_ID)) {

            ps.setInt(1, id);                    // ← ใส่ค่า id
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs)); // เจอ → return
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();                 // ไม่เจอ → empty
    }

    // ── getAll ────────────────────────────────────
    @Override
    public List<City> getAll() {
        List<City> list = new ArrayList<>();
        try (Connection con = connectionFactory.getConnection();
             Statement  st  = con.createStatement();
             ResultSet  rs  = st.executeQuery(SELECT_ALL)) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    // ── save (INSERT) ─────────────────────────────
    @Override
    public void save(City city) {
        try (Connection con = connectionFactory.getConnection()) {
            con.setAutoCommit(false);            // เริ่ม transaction

            try (PreparedStatement ps = con.prepareStatement(INSERT)) {
                // ← แก้ลำดับ ? ให้ตรงกับ INSERT statement ด้านบน
                ps.setString(1, city.getCity());
                ps.setInt(2, city.getIDCountry());
                ps.setObject(3, java.time.LocalDateTime.now());
                ps.executeUpdate();

                con.commit();                    // บันทึกสำเร็จ
            } catch (SQLException e) {
                con.rollback();                  // ย้อนกลับถ้า error
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // ── update ────────────────────────────────────
    @Override
    public void update(City city) {
        try (Connection con = connectionFactory.getConnection()) {
            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement(UPDATE)) {
                // ← แก้ให้ตรงกับ UPDATE statement และ field ของ Entity
                ps.setString(1, city.getCity());
                ps.setInt(2, city.getIDCountry());
                ps.setInt(3, city.getIDCity());  // WHERE id=?
                ps.executeUpdate();

                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // ── delete ────────────────────────────────────
    @Override
    public void delete(Integer id) {
        try (Connection con = connectionFactory.getConnection()) {
            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement(DELETE)) {
                ps.setInt(1, id);
                ps.executeUpdate();
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
```

---

### 5️⃣ Test.java
```java
import Repository.CityDAO;

public class Test {
    public static void main(String[] args) {
        CityDAO dao = new CityDAO();

        // ดึงทั้งหมด
        System.out.println("=== All Cities ===");
        dao.getAll().forEach(System.out::println);

        // หาด้วย ID
        System.out.println("=== Find ID=5 ===");
        dao.find(5).ifPresent(System.out::println);

        // เพิ่มใหม่
        System.out.println("=== Save ===");
        City newCity = new City();
        newCity.setCity("Bangkok");
        newCity.setIDCountry(18);
        dao.save(newCity);

        // แก้ไข
        System.out.println("=== Update ===");
        dao.find(5).ifPresent(c -> {
            c.setCity("Updated City");
            dao.update(c);
        });

        // ลบ
        System.out.println("=== Delete ID=200 ===");
        dao.delete(200);
    }
}
```

---

## 🗄️ ส่วนที่ 2: JPA

### โครงสร้างไฟล์ JPA
```
src/
├── main/
│   ├── java/
│   │   ├── models/
│   │   │   └── Actor.java           ← Entity
│   │   ├── repository/
│   │   │   ├── DataRepository.java  ← Interface (default methods)
│   │   │   └── ActorRepository.java ← ใส่แค่ชื่อ class
│   │   └── Main.java
│   └── resources/
│       └── META-INF/
│           └── persistence.xml      ← Config DB
└── pom.xml
```

---

### 1️⃣ persistence.xml
> ไฟล์นี้อยู่ที่ `src/main/resources/META-INF/persistence.xml`
> **แก้:** ชื่อ persistence-unit, url, user, password, และ `<class>` ทุกอัน

```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<persistence xmlns="https://jakarta.ee/xml/ns/persistence"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="https://jakarta.ee/xml/ns/persistence
             https://jakarta.ee/xml/ns/persistence/persistence_3_2.xsd"
             version="3.2">

    <!-- ← แก้ name ให้ตรงกับที่ใช้ใน Persistence.createEntityManagerFactory("...") -->
    <persistence-unit name="sakila" transaction-type="RESOURCE_LOCAL">
        <provider>org.hibernate.jpa.HibernatePersistenceProvider</provider>

        <!-- ← เพิ่ม class ทุกตัวที่เป็น @Entity -->
        <class>models.Actor</class>
        <class>models.Film</class>

        <properties>
            <property name="jakarta.persistence.jdbc.driver"
                      value="com.mysql.cj.jdbc.Driver"/>
            <!-- ← แก้ชื่อ DB -->
            <property name="jakarta.persistence.jdbc.url"
                      value="jdbc:mysql://localhost:3306/sakila"/>
            <property name="jakarta.persistence.jdbc.user"   value="root"/>
            <!-- ← แก้ password -->
            <property name="jakarta.persistence.jdbc.password" value="yourpassword"/>
        </properties>
    </persistence-unit>
</persistence>
```

---

### 2️⃣ Actor.java (Entity)
> **แก้:** ชื่อ class, `@Table(name=...)`, ชื่อ field, `@Column(name=...)`

```java
package models;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Getter
@Setter
@ToString
@Entity
@Table(name = "actor")          // ← แก้ชื่อตาราง
public class Actor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "actor_id")  // ← แก้ชื่อคอลัมน์ PK
    private Integer id;

    // ← แก้ชื่อ field และ @Column(name=...) ให้ตรงกับคอลัมน์ DB
    @Column(name = "first_name", nullable = false, length = 45)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 45)
    private String lastName;

    @Column(name = "last_update", nullable = false)
    private Instant lastUpdate;
}
```

**ถ้าโจทย์ให้ Film:**
```java
@Entity
@Table(name = "film")
public class Film {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "film_id")
    private Integer id;

    @Column(name = "title")
    private String title;

    @Column(name = "release_year")
    private String releaseYear;

    @Column(name = "rating")
    private String rating;
}
```

---

### 3️⃣ DataRepository.java (Interface — Reusable)
> **ไม่ต้องแก้ไฟล์นี้เลย** — ใช้ได้กับทุก Entity

```java
package repository;
import jakarta.persistence.*;
import java.util.*;

public interface DataRepository<E, T> {
    // ← แก้ "sakila" ให้ตรงกับ name ใน persistence.xml
    EntityManagerFactory emf = Persistence.createEntityManagerFactory("sakila");

    Class<E> getEntityClass(); // แต่ละ Repository implement อันนี้

    default EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    // ── save ──────────────────────────────────────
    default E save(E entity) {
        EntityManager em = getEntityManager();
        em.getTransaction().begin();
        em.persist(entity);
        em.getTransaction().commit();
        E managed = em.merge(entity);
        em.close();
        return managed;
    }

    // ── findAll ───────────────────────────────────
    default List<E> findAll() {
        EntityManager em = getEntityManager();
        try {
            // JPQL: ใช้ชื่อ Class ไม่ใช่ชื่อตาราง
            return em.createQuery(
                "select e from " + getEntityClass().getSimpleName() + " e",
                getEntityClass()
            ).getResultList();
        } finally {
            em.close();
        }
    }

    // ── findById ──────────────────────────────────
    default Optional<E> findById(T id) {
        EntityManager em = getEntityManager();
        try {
            return Optional.ofNullable(em.find(getEntityClass(), id));
        } finally {
            em.close();
        }
    }

    // ── delete ────────────────────────────────────
    default void delete(E entity) {
        EntityManager em = getEntityManager();
        em.getTransaction().begin();
        em.remove(em.merge(entity)); // merge ก่อนถึงจะ remove ได้
        em.getTransaction().commit();
        em.close();
    }

    default void deleteById(T id) {
        EntityManager em = getEntityManager();
        em.getTransaction().begin();
        em.remove(em.find(getEntityClass(), id));
        em.getTransaction().commit();
        em.close();
    }
}
```

---

### 4️⃣ ActorRepository.java
> **แก้:** แค่ชื่อ class และ Entity type เท่านั้น

```java
package repository;
import models.Actor;

// ← แก้ Actor → ชื่อ Entity ที่โจทย์กำหนด
public class ActorRepository implements DataRepository<Actor, Integer> {
    @Override
    public Class<Actor> getEntityClass() {  // ← แก้ Actor
        return Actor.class;                 // ← แก้ Actor
    }
    // ไม่ต้องเขียน method อื่นเลย! default methods จัดการให้หมด
}
```

**ถ้าโจทย์ให้ Film:**
```java
public class FilmRepository implements DataRepository<Film, Integer> {
    @Override
    public Class<Film> getEntityClass() {
        return Film.class;
    }
}
```

---

### 5️⃣ Main.java (JPA)
```java
import models.Actor;
import repository.ActorRepository;
import java.time.Instant;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        ActorRepository repo = new ActorRepository();

        // ── findAll ──────────────────────────────
        System.out.println("=== All Actors ===");
        List<Actor> all = repo.findAll();
        all.forEach(System.out::println);

        // ── findById ─────────────────────────────
        System.out.println("=== Find ID=1 ===");
        repo.findById(1).ifPresent(System.out::println);

        // ── save ─────────────────────────────────
        System.out.println("=== Save ===");
        Actor newActor = new Actor();
        newActor.setFirstName("John");
        newActor.setLastName("Doe");
        newActor.setLastUpdate(Instant.now());
        Actor saved = repo.save(newActor);
        System.out.println("Saved: " + saved);

        // ── delete ───────────────────────────────
        System.out.println("=== Delete ===");
        repo.findById(saved.getId()).ifPresent(repo::delete);
    }
}
```

---

## 📦 ส่วนที่ 3: pom.xml

### JDBC only
```xml
<dependencies>
    <!-- MySQL Driver -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <version>9.3.0</version>
    </dependency>
    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>1.18.42</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

### JPA (เพิ่มจาก JDBC)
```xml
<dependencies>
    <!-- MySQL Driver -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <version>9.3.0</version>
    </dependency>
    <!-- JPA API -->
    <dependency>
        <groupId>jakarta.persistence</groupId>
        <artifactId>jakarta.persistence-api</artifactId>
        <version>3.2.0</version>
    </dependency>
    <!-- Hibernate (JPA Provider) -->
    <dependency>
        <groupId>org.hibernate</groupId>
        <artifactId>hibernate-core</artifactId>
        <version>7.3.0.Final</version>
    </dependency>
    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>1.18.42</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

---

## 📁 ส่วนที่ 4: โครงสร้างโปรเจค

### JDBC Project
```
MyProject/
├── pom.xml
└── src/main/java/
    ├── DOA/
    │   ├── connectionFactory.java
    │   └── JDBCDAO.java
    ├── Model/
    │   └── City.java          ← เปลี่ยนตามโจทย์
    ├── Repository/
    │   └── CityDAO.java       ← เปลี่ยนตามโจทย์
    └── Test.java
```

### JPA Project
```
MyProject/
├── pom.xml
└── src/main/
    ├── java/
    │   ├── models/
    │   │   └── Actor.java         ← เปลี่ยนตามโจทย์
    │   ├── repository/
    │   │   ├── DataRepository.java
    │   │   └── ActorRepository.java ← เปลี่ยนตามโจทย์
    │   └── Main.java
    └── resources/META-INF/
        └── persistence.xml        ← แก้ URL + class list
```

---

## ⚡ ส่วนที่ 5: เปรียบเทียบ JDBC vs JPA

| หัวข้อ | JDBC | JPA |
|---|---|---|
| เขียน SQL | ✅ เขียนเอง | ❌ ใช้ JPQL / ไม่ต้องเลย |
| Config | Connection URL | `persistence.xml` |
| ดึงข้อมูล | `ResultSet` + `while(rs.next())` | `em.find()` / `createQuery()` |
| บันทึก | `PreparedStatement` + `executeUpdate()` | `em.persist()` |
| Transaction | `setAutoCommit(false)` + `commit()` | `getTransaction().begin()` + `commit()` |
| Exception | `SQLException` (checked — ต้อง try-catch) | Unchecked (ไม่บังคับ) |
| ไฟล์ Config | ไม่มี | `persistence.xml` |

---

## 🚨 จุดที่มักผิดบ่อย

```
JDBC:
  ❌ ลืม rs.next() ก่อนอ่านข้อมูล
  ❌ ลำดับ ? ใน PreparedStatement ผิด (นับจาก 1 ไม่ใช่ 0)
  ❌ ลืม conn.close()
  ❌ ลืม setAutoCommit(false) ก่อน INSERT/UPDATE/DELETE

JPA:
  ❌ ลืมใส่ <class> ใน persistence.xml
  ❌ ชื่อ persistence-unit ใน XML ≠ ชื่อใน createEntityManagerFactory()
  ❌ ลืม @Entity หรือ @Id
  ❌ JPQL ใช้ชื่อ Class ไม่ใช่ชื่อตาราง: "select a from Actor a" ✅
```

---

## 🎯 เช็คลิสต์ก่อนส่งสอบ

### JDBC
- [ ] `connectionFactory` — url / user / password ถูกต้อง?
- [ ] `mapRow()` — ชื่อคอลัมน์ตรงกับ DB?
- [ ] SQL `?` — จำนวนตรงกับ `setString/setInt` ที่เรียก?
- [ ] `save/update/delete` — มี `setAutoCommit(false)` + `commit()`?

### JPA
- [ ] `persistence.xml` — name ตรงกับ `createEntityManagerFactory`?
- [ ] Entity ทุกตัวอยู่ใน `<class>` ใน xml?
- [ ] `@Entity`, `@Table`, `@Id`, `@Column` ครบ?
- [ ] `DataRepository` — แก้ชื่อ persistence-unit ในบรรทัด `emf =`?