# 🎬 JPA Demo — Sakila Database

โปรเจกต์นี้เป็นตัวอย่างการใช้ **Jakarta Persistence API (JPA)** ร่วมกับ **Hibernate** เพื่อเชื่อมต่อและจัดการข้อมูลในฐานข้อมูล MySQL (Sakila) ผ่าน Java แบบ Console Application

---

## 📁 โครงสร้างโปรเจกต์

```
jpademo/
├── src/main/
│   ├── java/sit/int224/
│   │   ├── models/
│   │   │   ├── Actor.java              ← Entity แมปตาราง actor
│   │   │   └── Film.java               ← Entity แมปตาราง film
│   │   ├── repositories/
│   │   │   ├── DataRepository.java         ← Interface หลัก (มี EMF)
│   │   │   ├── DataRepositoryReuse.java    ← Interface แบบ Reusable + Paging
│   │   │   ├── ActorRepository.java        ← Repository ของ Actor
│   │   │   └── FilmRepository.java         ← Repository ของ Film
│   │   ├── EntityManagerFactory.java   ← โหลด config จาก .env
│   │   ├── Main.java                   ← ทดสอบดึง Actor
│   │   ├── TestActor.java              ← เมนู CRUD สำหรับ Actor
│   │   └── TestFilm.java               ← เมนู CRUD สำหรับ Film
│   └── resources/
│       ├── META-INF/persistence.xml    ← config JPA / การเชื่อมต่อ DB
│       └── application.env             ← (ถ้ามี) กำหนด persistence unit name
└── pom.xml
```

---

## ⚙️ Dependencies (pom.xml)

| Library | หน้าที่ |
|---|---|
| `lombok` | สร้าง getter/setter/toString อัตโนมัติ |
| `jakarta.persistence-api` | JPA API มาตรฐาน |
| `hibernate-core` | JPA Provider (ตัวจริงที่คุยกับ DB) |
| `commons-beanutils` | utility สำหรับจัดการ object |
| `mysql-connector-j` | JDBC Driver สำหรับ MySQL |

---

## 🛠️ วิธีตั้งค่าก่อนรัน

### 1. ติดตั้ง MySQL และ Import Sakila Database

```bash
# ดาวน์โหลด Sakila จาก https://dev.mysql.com/doc/index-other.html
mysql -u root -p < sakila-schema.sql
mysql -u root -p < sakila-data.sql
```

### 2. แก้ไข `persistence.xml`

ไฟล์อยู่ที่ `src/main/resources/META-INF/persistence.xml`

```xml
<property name="jakarta.persistence.jdbc.user" value="root"/>
<property name="jakarta.persistence.jdbc.password" value="รหัสผ่านของคุณ"/>
<property name="jakarta.persistence.jdbc.url" value="jdbc:mysql://localhost:3306/sakila"/>
```

> ⚠️ **เปลี่ยน `value` ให้ตรงกับ MySQL ของตัวเอง** โดยเฉพาะ `password` และ `url`

### 3. (ถ้าใช้) ไฟล์ `application.env`

สร้างไฟล์ `src/main/resources/application.env` แล้วใส่:

```
PERSISTENCE_UNIT_NAME=default
```

ถ้าไม่มีไฟล์นี้ โปรแกรมจะ print `Cannot load application.env` แต่ยังทำงานได้ตามปกติ

---

## 🏃 วิธีรัน

รันได้ 3 Entry Point:

| คลาส | ทำอะไร |
|---|---|
| `Main` | ดึง Actor id=2 มาแสดง (ทดสอบเบื้องต้น) |
| `TestActor` | เมนู CRUD เต็มรูปแบบสำหรับ Actor |
| `TestFilm` | เมนู CRUD เต็มรูปแบบสำหรับ Film (มี Paging) |

รันผ่าน IntelliJ: **Right-click คลาส → Run**

---

## 🧩 อธิบายโค้ดส่วนสำคัญ

### Entity (`Actor.java`, `Film.java`)

```java
@Entity
@Table(name = "actor")   // ← บอกว่า class นี้แมปกับตาราง "actor"
public class Actor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // ← auto increment
    @Column(name = "actor_id", ...)
    private Integer id;
    ...
}
```

- `@Entity` บอก JPA ว่า class นี้คือตารางในฐานข้อมูล
- `@Column` ระบุชื่อคอลัมน์จริงใน DB
- Lombok `@Getter @Setter` สร้าง getter/setter ให้อัตโนมัติ ไม่ต้องเขียนเอง

---

### Repository Pattern

โปรเจกต์มี **2 แบบ** ของ Repository:

#### แบบที่ 1 — `DataRepository` (Interface + `ActorRepository`)

```java
// ActorRepository implement แต่ละ method เอง
public Actor save(Actor entity) { ... }
public List<Actor> findAll() { ... }
```

เหมาะถ้าต้องการ **custom logic เฉพาะ Entity นั้น**

#### แบบที่ 2 — `DataRepositoryReuse` (Interface มี default method)

```java
// FilmRepository แค่ implement getEntityClass() พอ
public Class<Film> getEntityClass() { return Film.class; }
```

Method อื่นๆ ทั้งหมด (`save`, `findAll`, `findById`, `delete`) ใช้ **default method** ที่เขียนไว้ใน interface
→ **ไม่ต้องเขียนซ้ำ** เหมาะสำหรับ Entity ที่ logic ไม่ต่างกัน

---

### Paging (`DataRepositoryReuse`)

```java
// ดึงข้อมูลแบบแบ่งหน้า
default List<E> findAll(int startPosition, int maxRecords) {
    query.setFirstResult(startPosition);  // เริ่มที่แถวไหน (offset)
    query.setMaxResults(maxRecords);       // ดึงกี่แถว (limit)
    return query.getResultList();
}
```

ใน `TestFilm` จะวน loop ดึงทีละ 10 รายการจนหมด:

```java
int maxRecords = 10;
int startPosition = 0;
while (true) {
    List<Film> filmList = filmRepository.findAll(startPosition, maxRecords);
    if (filmList.isEmpty()) break;
    startPosition += filmList.size();
}
```

---

### EntityManagerFactory (Singleton)

```java
// สร้าง EMF ครั้งเดียว แชร์ใช้ทั้ง app
private static final jakarta.persistence.EntityManagerFactory emf =
        Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);

public static EntityManager getEntityManager() {
    return emf.createEntityManager();  // สร้าง EM ใหม่ทุกครั้งที่เรียก
}
```

> `EntityManagerFactory` สร้างแพงมาก → สร้างครั้งเดียว  
> `EntityManager` สร้างถูก → สร้างใหม่ทุก operation แล้วปิดทันที

---

## 🔄 Flow การทำงาน CRUD

```
เรียก Repository method
    → getEntityManager()  (สร้าง EM ใหม่)
    → begin transaction (ถ้าเป็น write)
    → ทำ operation (persist / find / remove)
    → commit
    → em.close()  ← สำคัญมาก! ป้องกัน memory leak
```

---

## 💡 จุดที่แก้ได้ง่าย

| จุด | วิธีแก้ |
|---|---|
| เปลี่ยน DB connection | แก้ที่ `persistence.xml` |
| เพิ่ม Entity ใหม่ | สร้าง class + `@Entity` แล้วเพิ่มใน `<class>` ใน `persistence.xml` |
| เพิ่ม Repository ใหม่ | implement `DataRepositoryReuse` แล้ว override แค่ `getEntityClass()` |
| เปลี่ยน Paging size | แก้ `maxRecords` ใน `TestFilm.java` |

---

## 📦 Tech Stack

- Java 24
- Maven
- Jakarta EE 3.2 (JPA)
- Hibernate 7.3
- MySQL 8+ (Sakila Database)
- Lombok

# JPA Demo — Source Code ทั้งหมด

---

## pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>sit.int224</groupId>
    <artifactId>jpademo</artifactId>
    <version>1.0-SNAPSHOT</version>
    <properties>
        <maven.compiler.source>24</maven.compiler.source>
        <maven.compiler.target>24</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>1.18.42</version>
        </dependency>
        <dependency>
            <groupId>jakarta.persistence</groupId>
            <artifactId>jakarta.persistence-api</artifactId>
            <version>3.2.0</version>
        </dependency>
        <dependency>
            <groupId>org.hibernate</groupId>
            <artifactId>hibernate-core</artifactId>
            <version>7.3.0.Final</version>
        </dependency>
        <dependency>
            <groupId>commons-beanutils</groupId>
            <artifactId>commons-beanutils</artifactId>
            <version>1.11.0</version>
        </dependency>
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <version>9.4.0</version>
        </dependency>
    </dependencies>
</project>
```

---

## src/main/resources/META-INF/persistence.xml

```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<persistence xmlns="https://jakarta.ee/xml/ns/persistence"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="https://jakarta.ee/xml/ns/persistence https://jakarta.ee/xml/ns/persistence/persistence_3_2.xsd"
             version="3.2">
  <persistence-unit name="default" transaction-type="RESOURCE_LOCAL">
    <provider>org.hibernate.jpa.HibernatePersistenceProvider</provider>
    <class>sit.int224.models.Actor</class>
    <class>sit.int224.models.Film</class>
    <properties>
      <property name="jakarta.persistence.jdbc.driver" value="com.mysql.cj.jdbc.Driver"/>
      <property name="jakarta.persistence.jdbc.user" value="root"/>
      <property name="jakarta.persistence.jdbc.password" value="mysql@sit"/>
      <property name="jakarta.persistence.jdbc.url" value="jdbc:mysql://localhost:3306/sakila"/>
    </properties>
  </persistence-unit>
</persistence>
```

---

## src/main/resources/application.env

```
PERSISTENCE_UNIT_NAME=default
```

---

## src/main/java/sit/int224/models/Actor.java

```java
package sit.int224.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "actor")
@ToString(exclude = "lastUpdate")
public class Actor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "actor_id", columnDefinition = "smallint UNSIGNED not null")
    private Integer id;

    @Column(name = "first_name", nullable = false, length = 45)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 45)
    private String lastName;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "last_update", nullable = false)
    private Instant lastUpdate;
}
```

---

## src/main/java/sit/int224/models/Film.java

```java
package sit.int224.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Setter
@Entity
@Table(name = "film")
public class Film {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "film_id", columnDefinition = "smallint UNSIGNED not null")
    private Integer id;

    @Column(name = "title", nullable = false, length = 128)
    private String title;

    @Column(name = "release_year")
    private Integer releaseYear;

    @Column(name = "language_id", nullable = false)
    private String languageId;

    @ColumnDefault("'G'")
    @Lob
    @Column(name = "rating")
    private String rating;

    public String toString() {
        return String.format(
                "%d %-25s %-10s %-10s %s",
                id, title, releaseYear, rating, languageId);
    }
}
```

---

## src/main/java/sit/int224/repositories/DataRepository.java

```java
package sit.int224.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.List;
import java.util.Optional;

public interface DataRepository<E, T> {
    EntityManagerFactory emf = Persistence.createEntityManagerFactory("default");

    E save(E entity);
    List<E> findAll();
    Optional<E> findById(T id);
    void delete(E entity);
    void deleteById(T id);

    default EntityManager getEntityManager() {
        return emf.createEntityManager();
    }
}
```

---

## src/main/java/sit/int224/repositories/DataRepositoryReuse.java

```java
package sit.int224.repositories;

import jakarta.persistence.EntityManager;
import sit.int224.EntityManagerFactory;
import lombok.NonNull;
import java.util.List;
import java.util.Optional;

public interface DataRepositoryReuse<E, T> {
    Class<E> getEntityClass();

    default EntityManager getEntityManager() {
        return EntityManagerFactory.getEntityManager();
    }

    default E save(E entity) {
        EntityManager em = getEntityManager();
        em.getTransaction().begin();
        em.persist(entity);
        em.getTransaction().commit();
        E managed = em.merge(entity);
        em.close();
        return managed;
    }

    default List<E> findAll() {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                    "select e from " + getEntityClass().getSimpleName() + " e",
                    getEntityClass()
            ).getResultList();
        } finally {
            em.close();
        }
    }

    // findAll แบบ Paging — ดึงข้อมูลแบบแบ่งหน้า
    default List<E> findAll(int startPosition, int maxRecords) {
        EntityManager em = getEntityManager();
        try {
            var query = em.createQuery(
                    "select e from " + getEntityClass().getSimpleName() + " e",
                    getEntityClass()
            );
            query.setFirstResult(startPosition); // offset
            query.setMaxResults(maxRecords);      // limit
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    default Optional<E> findById(T id) {
        EntityManager em = getEntityManager();
        try {
            return Optional.ofNullable(em.find(getEntityClass(), id));
        } finally {
            em.close();
        }
    }

    default void delete(@NonNull E entity) {
        EntityManager em = getEntityManager();
        em.getTransaction().begin();
        em.remove(em.merge(entity));
        em.getTransaction().commit();
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

## src/main/java/sit/int224/repositories/ActorRepository.java

```java
package sit.int224.repositories;

import jakarta.persistence.EntityManager;
import sit.int224.models.Actor;
import java.util.List;
import java.util.Optional;

public class ActorRepository implements DataRepository<Actor, Integer> {
    @Override
    public Actor save(Actor entity) {
        EntityManager em = getEntityManager();
        em.getTransaction().begin();
        em.persist(entity);
        em.getTransaction().commit();
        Actor managed = em.merge(entity);
        em.close();
        return managed;
    }

    @Override
    public List<Actor> findAll() {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery("select a from Actor a", Actor.class).getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Actor> findById(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return Optional.ofNullable(em.find(Actor.class, id));
        } finally {
            em.close();
        }
    }

    @Override
    public void delete(Actor entity) {
        EntityManager em = getEntityManager();
        em.getTransaction().begin();
        em.remove(em.merge(entity));
        em.getTransaction().commit();
    }

    @Override
    public void deleteById(Integer id) {
        EntityManager em = getEntityManager();
        em.getTransaction().begin();
        em.remove(em.find(Actor.class, id));
        em.getTransaction().commit();
        em.close();
    }
}
```

---

## src/main/java/sit/int224/repositories/FilmRepository.java

```java
package sit.int224.repositories;

import sit.int224.models.Film;

public class FilmRepository implements DataRepositoryReuse<Film, Integer> {
    @Override
    public Class<Film> getEntityClass() {
        return Film.class;
    }
}
```

---

## src/main/java/sit/int224/EntityManagerFactory.java

```java
package sit.int224;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Persistence;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class EntityManagerFactory {
    private static String PERSISTENCE_UNIT_NAME = "default";

    private static final jakarta.persistence.EntityManagerFactory emf =
            Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);

    static {
        Properties prop = new Properties();
        try (FileInputStream input = new FileInputStream("src/main/resources/application.env")) {
            prop.load(input);
            PERSISTENCE_UNIT_NAME = prop.getProperty("PERSISTENCE_UNIT_NAME");
        } catch (IOException ex) {
            System.out.println("Cannot load application.env");
        }
        System.out.println("Use Persistence Unit Name: " + PERSISTENCE_UNIT_NAME);
    }

    public static EntityManager getEntityManager() {
        return emf.createEntityManager();
    }
}
```

---

## src/main/java/sit/int224/Main.java

```java
package sit.int224;

import sit.int224.models.Actor;
import sit.int224.repositories.ActorRepository;

public class Main {
    public static void main(String[] args) {
        ActorRepository actorRepository = new ActorRepository();
        Actor actor = actorRepository.findById(2).orElseThrow(
                () -> new RuntimeException("Actor Not Found")
        );
        System.out.println(actor);
    }
}
```

---

## src/main/java/sit/int224/TestActor.java

```java
package sit.int224;

import sit.int224.models.Actor;
import sit.int224.repositories.ActorRepository;
import java.time.Instant;
import java.util.List;
import java.util.Scanner;

public class TestActor {
    public static void main(String[] args) {
        ActorRepository actorRepository = new ActorRepository();
        while (true) {
            System.out.println("--Main Menu----------");
            System.out.println("1) Create new actor");
            System.out.println("2) Find actor by id");
            System.out.println("3) Delete actor by id");
            System.out.println("4) List all actor");
            System.out.println("0) Exit");
            System.out.println("---------------------");
            System.out.print("Enter your choice: ");
            Scanner scanner = new Scanner(System.in);
            Integer choice = scanner.nextInt();
            System.out.println("----------------");
            switch (choice) {
                case 0 -> System.exit(0);
                case 1 -> createNewActor(actorRepository);
                case 2 -> findActorById(actorRepository);
                case 3 -> deleteActorById(actorRepository);
                case 4 -> listAllActor(actorRepository);
                default -> System.err.println("Invalid choice");
            }
            System.out.println("\n\n");
        }
    }

    private static void listAllActor(ActorRepository actorRepository) {
        List<Actor> actorList = actorRepository.findAll();
        for (Actor actor : actorList) {
            System.out.println(actor);
        }
    }

    private static void createNewActor(ActorRepository actorRepository) {
        Actor newActor = new Actor();
        newActor.setFirstName("John");
        newActor.setLastName("Doe");
        newActor.setLastUpdate(Instant.now());
        newActor = actorRepository.save(newActor);
        System.out.println(newActor);
    }

    private static void deleteActorById(ActorRepository actorRepository) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter actor id to delete: ");
        Integer id = scanner.nextInt();
        Actor actor = actorRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Actor id " + id + " not found"));
        actorRepository.delete(actor);
        System.out.println("Actor id: " + id + " was deleted");
    }

    private static void findActorById(ActorRepository actorRepository) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter actor id : ");
        Integer id = scanner.nextInt();
        Actor actor = actorRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Actor id " + id + " not found"));
        System.out.println(actor);
    }
}
```

---

## src/main/java/sit/int224/TestFilm.java

```java
package sit.int224;

import sit.int224.models.Film;
import sit.int224.repositories.FilmRepository;
import java.util.List;
import java.util.Scanner;

public class TestFilm {
    public static void main(String[] args) {
        FilmRepository filmRepository = new FilmRepository();
        while (true) {
            System.out.println("--Main Menu----------");
            System.out.println("1) Create new film");
            System.out.println("2) Find film by id");
            System.out.println("3) Delete film by id");
            System.out.println("4) List all film");
            System.out.println("0) Exit");
            System.out.println("---------------------");
            System.out.print("Enter your choice: ");
            Scanner scanner = new Scanner(System.in);
            Integer choice = scanner.nextInt();
            System.out.println("----------------");
            switch (choice) {
                case 0 -> System.exit(0);
                case 1 -> createNewFilm(filmRepository);
                case 2 -> findFilmById(filmRepository);
                case 3 -> deleteFilmById(filmRepository);
                case 4 -> listAllFilm(filmRepository);
                default -> System.err.println("Invalid choice");
            }
            System.out.println("\n\n");
        }
    }

    private static void listAllFilm(FilmRepository filmRepository) {
        int maxRecords = 10;
        int startPosition = 0;
        while (true) {
            List<Film> filmList = filmRepository.findAll(startPosition, maxRecords);
            if (filmList.isEmpty()) break;
            for (Film film : filmList) {
                System.out.println(film);
            }
            startPosition = startPosition + filmList.size();
            System.out.println("---------------------");
        }
    }

    private static void createNewFilm(FilmRepository filmRepository) {
        Film newFilm = new Film();
        newFilm.setTitle("Inception");
        newFilm.setReleaseYear(2010);
        newFilm.setRating("PG-13");
        newFilm.setLanguageId("1");
        newFilm = filmRepository.save(newFilm);
        System.out.println(newFilm);
    }

    private static void deleteFilmById(FilmRepository filmRepository) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter film id to delete: ");
        Integer id = scanner.nextInt();
        Film film = filmRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Film id " + id + " not found"));
        filmRepository.delete(film);
        System.out.println("Film id: " + id + " was deleted");
    }

    private static void findFilmById(FilmRepository filmRepository) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter film id : ");
        Integer id = scanner.nextInt();
        Film film = filmRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Film id " + id + " not found"));
        System.out.println(film);
    }
}
```
