# 🎬 Sakila Actor Management (JDBC vs JPA)

โปรเจกต์สาธิตการเขียนโปรแกรมด้วยภาษา Java เพื่อจัดการข้อมูลในตาราง `actor` ของฐานข้อมูล **sakila** โดยเปรียบเทียบการทำงานระหว่าง **JDBC (Raw SQL)** และ **JPA (Hibernate)** ---

## 📋 ฟีเจอร์การทำงาน (Features)
ระบบรองรับฟังก์ชันพื้นฐาน (CRUD Operations) ทั้งหมด 5 รูปแบบ:
1. 🔍 **Read All** - ดึงข้อมูล Actor ทั้งหมด
2. 🆔 **Read by ID** - ค้นหา Actor ด้วย ID
3. ➕ **Create** - เพิ่ม Actor ใหม่
4. ✏️ **Update** - แก้ไขชื่อและนามสกุล Actor
5. ❌ **Delete** - ลบ Actor ด้วย ID

---

## 📊 โครงสร้างตาราง Database (`actor`)
| คอลัมน์ (Column) | ประเภทข้อมูล (Type) | คำอธิบาย (Description) |
| :--- | :--- | :--- |
| `actor_id` | `smallint` | รหัสพนักงานแสดง (Primary Key, Auto Increment) |
| `first_name` | `varchar(45)` | ชื่อจริง |
| `last_name` | `varchar(45)` | นามสกุล |
| `last_update` | `timestamp` | วันเวลาที่อัปเดตข้อมูลล่าสุด |

---

## 📦 สรุปโครงสร้างไฟล์ในโปรเจกต์

โปรเจกต์นี้ถูกแบ่งออกเป็น 2 เวอร์ชัน เพื่อให้เห็นความแตกต่างในการพัฒนา

### 🔌 1. เวอร์ชัน JDBC (5 ไฟล์หลัก)
เน้นการจัดการ Connection เอง และเขียนคำสั่ง SQL ตรงๆ

| ชื่อไฟล์ | จุดที่ต้องแก้ไข / หน้าที่ของไฟล์ |
| :--- | :--- |
| 📄 `ConnectionFactory.java` | ตั้งค่าฐานข้อมูล (`url` / `user` / `password`) |
| 📄 `Actor_JDBC.java` | คลาส Model (กำหนดชื่อ class และ fields ให้ตรงตามตาราง) |
| 📄 `ActorDAO.java` | จัดการคำสั่ง SQL 5 บรรทัดด้านบน + ฟังก์ชัน `mapRow()` |
| 📄 `Main_JDBC.java` | คลาสหลักสำหรับใส่ค่าที่ใช้ทดสอบระบบ |
| 📄 `pom_JDBC.xml` | ไฟล์สำหรับจัดการ Dependency (พร้อมใช้งาน ไม่ต้องแก้ไข) |

### 🗄️ 2. เวอร์ชัน JPA (5 ไฟล์หลัก)
เน้นการใช้ Object-Relational Mapping (ORM) เพื่อลดการเขียน SQL

| ชื่อไฟล์ | จุดที่ต้องแก้ไข / หน้าที่ของไฟล์ |
| :--- | :--- |
| 📄 `persistence.xml` | ตั้งค่าฐานข้อมูล (`url` / `user` / `pwd`) และระบุแท็ก `<class>` |
| 📄 `Actor_JPA.java` | คลาส Entity (ใส่ Annotation mapping เช่น `@Table`, `@Column`) |
| 📄 `DataRepository.java` | เรียกใช้และกำหนดชื่อ `persistence-unit` (แก้จุดเดียว) |
| 📄 `ActorRepository.java` | คลาสจัดการคำสั่ง CRUD (แก้ไขชื่อ Entity เพียง 2 จุด) |
| 📄 `Main_JPA.java` | คลาสหลักสำหรับใส่ค่าที่ใช้ทดสอบระบบ |

---

## 🚀 ขั้นตอนการติดตั้งและรันโปรเจกต์ (Getting Started)

1. **เตรียมฐานข้อมูล:** ตรวจสอบให้แน่ใจว่าติดตั้งฐานข้อมูล `sakila` เรียบร้อยแล้ว
2. **ตั้งค่า Connection:** แก้ไขไฟล์ `ConnectionFactory.java` (สำหรับ JDBC) หรือ `persistence.xml` (สำหรับ JPA) ให้ตรงกับเครื่องของคุณ
3. **ทดสอบระบบ:** * หากต้องการรันเวอร์ชัน JDBC ให้รันไฟล์ `Main_JDBC.java`
   * หากต้องการรันเวอร์ชัน JPA ให้รันไฟล์ `Main_JPA.java`

---
💡 *โปรเจกต์นี้ทำขึ้นเพื่อเปรียบเทียบความแตกต่างระหว่างการเขียนดึงข้อมูลแบบดั้งเดิม (JDBC) กับการใช้เครื่องมือสมัยใหม่ (JPA/Hibernate)*