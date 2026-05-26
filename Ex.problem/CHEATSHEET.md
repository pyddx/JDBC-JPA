# 🎯 โพยสอบ JDBC & JPA — Actor Example

> อ่านตรงนี้ก่อนลงมือเขียนโค้ด | **🔧 = จุดที่ต้องแก้**

---

## 📁 โครงสร้างโปรเจค

### JDBC
```
jdbc-actor/
├── pom.xml
└── src/
    ├── DOA/
    │   ├── ConnectionFactory.java   🔧 แก้ url/user/pwd
    │   └── JdbcDAO.java             ✅ ไม่ต้องแก้
    ├── Model/
    │   └── Actor.java               🔧 แก้ field ตามตาราง
    ├── Repository/
    │   └── ActorDAO.java            🔧 แก้ SQL + mapRow()
    └── Main.java                    🔧 แก้ค่าที่ test
```

### JPA
```
jpa-actor/
├── pom.xml
└── src/main/
    ├── java/
    │   ├── models/
    │   │   └── Actor.java              🔧 แก้ @Table @Column
    │   ├── repository/
    │   │   ├── DataRepository.java     🔧 แก้แค่ชื่อ persistence-unit
    │   │   └── ActorRepository.java    🔧 แก้แค่ชื่อ Entity
    │   └── Main.java
    └── resources/META-INF/
        └── persistence.xml             🔧 แก้ url/user/pwd + <class>
```

---

## ⚡ ขั้นตอนทำสอบ (120 นาที)

```
นาที 0-5   : อ่านโจทย์ → รู้ชื่อตาราง + คอลัมน์
นาที 5-10  : copy pom.xml → แก้ dependency ถ้าจำเป็น
นาที 10-20 : สร้าง Entity (Model)
นาที 20-40 : เขียน DAO (JDBC) หรือ Repository (JPA)
นาที 40-50 : เขียน Main + ทดสอบ
นาที 50+   : debug / แก้ไข
```

---

## 🔧 จุดที่ต้องแก้เมื่อโจทย์เปลี่ยนตาราง

### JDBC — แก้ 4 จุด

**1. ConnectionFactory.java**
```java
private static final String URL  = "jdbc:mysql://localhost:3306/[DB_NAME]";
private static final String USER = "[username]";
private static final String PWD  = "[password]";
```

**2. Model/Actor.java → เปลี่ยนชื่อ class + field**
```java
// ถ้าโจทย์ให้ Film:
public class Film {
    private Integer filmId;     // film_id
    private String  title;      // title
    private String  rating;     // rating
}
```

**3. ActorDAO.java → แก้ SQL string 5 บรรทัดบนสุด**
```java
private static final String FIND_ALL   = "SELECT * FROM [ชื่อตาราง]";
private static final String FIND_BY_ID = "SELECT * FROM [ตาราง] WHERE [pk] = ?";
private static final String INSERT     = "INSERT INTO [ตาราง] ([col1],[col2]) VALUES (?,?)";
private static final String UPDATE     = "UPDATE [ตาราง] SET [col1]=? WHERE [pk]=?";
private static final String DELETE     = "DELETE FROM [ตาราง] WHERE [pk]=?";
```

**4. mapRow() → แก้ชื่อคอลัมน์และ setter**
```java
private Actor mapRow(ResultSet rs) throws SQLException {
    Actor a = new Actor();
    a.setXxx(rs.getInt("column_name"));    // ← แก้ชื่อคอลัมน์
    a.setYyy(rs.getString("column_name")); // ← แก้ชื่อคอลัมน์
    return a;
}
```

---

### JPA — แก้ 3 จุด

**1. persistence.xml**
```xml
<persistence-unit name="[ชื่อ]">          ← ต้องตรงกับ createEntityManagerFactory()
  <class>models.[ClassName]</class>        ← เพิ่มทุก Entity
  <property name="...url" value="jdbc:mysql://localhost:3306/[DB]"/>
  <property name="...user" value="[user]"/>
  <property name="...password" value="[pwd]"/>
```

**2. Actor.java → เปลี่ยน annotation**
```java
@Table(name = "[ชื่อตาราง]")
@Column(name = "[ชื่อคอลัมน์]")
```

**3. ActorRepository.java → แค่เปลี่ยนชื่อ class**
```java
public class FilmRepository implements DataRepository<Film, Integer> {
    @Override
    public Class<Film> getEntityClass() { return Film.class; }
}
```

---

## 🚨 จุดที่ผิดบ่อย

| ❌ ผิด | ✅ ถูก |
|--------|--------|
| `rs.getInt(0)` | `rs.getInt(1)` หรือ `rs.getInt("column_name")` |
| ลืม `rs.next()` | ต้องเรียก `rs.next()` ก่อนอ่านข้อมูล |
| ลืม `setAutoCommit(false)` ใน INSERT/UPDATE/DELETE | ใส่ก่อน prepareStatement |
| `? ที่ 1` แต่ setXxx(2,...) | นับ `?` จาก 1 ซ้ายไปขวา |
| JPA: ลืม `<class>` ใน persistence.xml | เพิ่มทุก Entity |
| JPA: ชื่อ persistence-unit ไม่ตรง | xml name = createEntityManagerFactory() |
| JPQL: `FROM actor` | `FROM Actor` (ชื่อ Java Class ไม่ใช่ตาราง) |

---

## 📝 Template SQL สำคัญ

```sql
-- ดึงทั้งหมด
SELECT * FROM actor

-- หาด้วย PK
SELECT * FROM actor WHERE actor_id = ?

-- INSERT (ไม่ใส่ PK ถ้า auto_increment)
INSERT INTO actor (first_name, last_name, last_update) VALUES (?, ?, ?)

-- UPDATE
UPDATE actor SET first_name = ?, last_name = ?, last_update = ? WHERE actor_id = ?

-- DELETE
DELETE FROM actor WHERE actor_id = ?
```

---

## 🔑 PreparedStatement — ลำดับ ? สำคัญมาก!

```java
// SQL: INSERT INTO actor (first_name, last_name, last_update) VALUES (?, ?, ?)
//                                                                      1  2  3
ps.setString(1, actor.getFirstName());  // ? ที่ 1 = first_name
ps.setString(2, actor.getLastName());   // ? ที่ 2 = last_name
ps.setObject(3, LocalDateTime.now());   // ? ที่ 3 = last_update

// SQL: UPDATE actor SET first_name=?, last_name=?, last_update=? WHERE actor_id=?
//                                  1            2             3                 4
ps.setString(1, actor.getFirstName());
ps.setString(2, actor.getLastName());
ps.setObject(3, LocalDateTime.now());
ps.setInt(4, actor.getActorId());       // ? ที่ 4 = WHERE
```

---

## 💡 rs.getXxx() ใช้ type ไหน?

| ประเภทข้อมูล DB | Java method |
|---|---|
| INT, SMALLINT | `rs.getInt("col")` |
| VARCHAR, CHAR | `rs.getString("col")` |
| DATETIME, TIMESTAMP | `rs.getObject("col", LocalDateTime.class)` |
| DECIMAL, DOUBLE | `rs.getDouble("col")` |
| BOOLEAN, TINYINT(1) | `rs.getBoolean("col")` |
