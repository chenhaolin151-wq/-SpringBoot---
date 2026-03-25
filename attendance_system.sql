-- MySQL dump 10.13  Distrib 8.0.19, for Win64 (x86_64)
--
-- Host: localhost    Database: attendance_system
-- ------------------------------------------------------
-- Server version	8.0.45

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `attendance_config`
--

DROP TABLE IF EXISTS `attendance_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `attendance_config` (
  `id` int NOT NULL AUTO_INCREMENT,
  `config_key` varchar(50) NOT NULL COMMENT '配置项名称(如: office_ip)',
  `config_value` varchar(255) NOT NULL COMMENT '配置值(具体IP地址)',
  `remark` varchar(100) DEFAULT NULL COMMENT '备注(如: 杭州总部外网IP)',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='考勤系统配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `attendance_config`
--

LOCK TABLES `attendance_config` WRITE;
/*!40000 ALTER TABLE `attendance_config` DISABLE KEYS */;
INSERT INTO `attendance_config` VALUES (1,'OFFICE_IP','127.0.0.1','默认办公地点','2026-03-19 21:34:12');
/*!40000 ALTER TABLE `attendance_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `attendance_record`
--

DROP TABLE IF EXISTS `attendance_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `attendance_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '关联用户ID',
  `punch_date` date NOT NULL COMMENT '打卡日期',
  `clock_in` datetime DEFAULT NULL COMMENT '上班打卡时间',
  `clock_out` datetime DEFAULT NULL COMMENT '下班打卡时间',
  `status` tinyint DEFAULT '0' COMMENT '状态: 0-正常, 1-迟到, 2-早退',
  `is_correction` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uid_date` (`user_id`,`punch_date`)
) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='考勤记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `attendance_record`
--

LOCK TABLES `attendance_record` WRITE;
/*!40000 ALTER TABLE `attendance_record` DISABLE KEYS */;
INSERT INTO `attendance_record` VALUES (9,1,'2026-03-13','2026-03-13 23:10:40','2026-03-13 23:10:43',1,0),(11,3,'2026-03-14','2026-03-14 00:09:27',NULL,0,0),(12,2,'2026-03-14','2026-03-14 11:41:46','2026-03-14 11:41:53',2,0),(13,1,'2026-03-14','2026-03-14 12:26:44','2026-03-14 12:26:47',2,0),(14,2,'2026-03-15','2026-03-15 02:02:13','2026-03-15 02:02:18',2,0),(15,1,'2026-03-15','2026-03-15 15:23:27','2026-03-15 15:23:28',2,0),(18,3,'2026-03-16','2026-03-16 17:16:46','2026-03-16 17:16:49',1,0),(20,2,'2026-03-16','2026-03-16 17:26:04','2026-03-16 17:26:05',3,0),(21,1,'2026-03-16','2026-03-16 09:00:00',NULL,0,0),(22,2,'2026-03-17','2026-03-17 13:14:13','2026-03-17 13:14:14',1,0),(23,2,'2026-03-13','2026-03-13 09:00:00','2026-03-13 10:00:00',0,1),(24,2,'2026-03-18','2026-03-18 09:00:00','2026-03-18 18:00:00',0,0),(25,2,'2026-03-12','2026-03-12 09:00:00',NULL,0,1),(26,2,'2026-02-11','2026-02-11 08:00:00',NULL,0,1),(27,1,'2026-03-19','2026-03-19 19:11:31',NULL,1,0),(28,3,'2026-03-19','2026-03-19 19:25:18','2026-03-19 19:25:42',1,0);
/*!40000 ALTER TABLE `attendance_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `correction_apply`
--

DROP TABLE IF EXISTS `correction_apply`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `correction_apply` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '申请人ID',
  `apply_date` date NOT NULL COMMENT '补签哪一天的日期',
  `type` varchar(10) NOT NULL COMMENT '类型：IN(补上班), OUT(补下班)',
  `reason` varchar(255) DEFAULT NULL COMMENT '补签原因',
  `status` int DEFAULT '0' COMMENT '审批状态：0-待审批, 1-已通过, 2-已驳回',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='补签申请表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `correction_apply`
--

LOCK TABLES `correction_apply` WRITE;
/*!40000 ALTER TABLE `correction_apply` DISABLE KEYS */;
INSERT INTO `correction_apply` VALUES (1,1,'2026-03-16','OUT','忘记了',0,'2026-03-16 21:00:28'),(2,1,'2026-03-16','OUT','忘记了',0,'2026-03-16 21:05:29'),(3,1,'2026-03-16','OUT','忘记了',2,'2026-03-16 21:05:34'),(4,1,'2026-03-16','OUT','忘记了',1,'2026-03-16 21:09:15'),(5,1,'2026-03-16','IN','测试',1,'2026-03-16 23:50:23'),(6,2,'2026-03-13','IN','测试',1,'2026-03-17 23:59:34'),(7,2,'2026-03-18','IN','test',1,'2026-03-18 13:43:02'),(8,2,'2026-03-18','OUT','test',1,'2026-03-18 13:43:35'),(9,2,'2026-03-12','IN','测试',1,'2026-03-18 13:56:46'),(10,2,'2026-02-11','IN','test',1,'2026-03-18 14:38:27'),(11,2,'2026-03-13','OUT','我',1,'2026-03-18 15:07:13'),(12,2,'2026-03-11','IN','无',0,'2026-03-19 15:07:39');
/*!40000 ALTER TABLE `correction_apply` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `holiday_config`
--

DROP TABLE IF EXISTS `holiday_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `holiday_config` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `holiday_date` date NOT NULL,
  `name` varchar(50) NOT NULL,
  `is_work_day` tinyint DEFAULT '0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `holiday_date` (`holiday_date`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `holiday_config`
--

LOCK TABLES `holiday_config` WRITE;
/*!40000 ALTER TABLE `holiday_config` DISABLE KEYS */;
INSERT INTO `holiday_config` VALUES (1,'2026-03-03','春节',0,'2026-03-18 22:38:30'),(5,'2026-04-10','法定节假日',0,'2026-03-19 00:50:56'),(8,'2026-02-05','春节',0,'2026-03-19 03:11:49'),(12,'2026-03-05','愚人节',0,'2026-03-19 13:50:15'),(13,'2026-03-06','数值解',0,'2026-03-19 14:08:08');
/*!40000 ALTER TABLE `holiday_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `leave_apply`
--

DROP TABLE IF EXISTS `leave_apply`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `leave_apply` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '申请人ID',
  `start_date` date NOT NULL COMMENT '开始日期',
  `end_date` date NOT NULL COMMENT '结束日期',
  `reason` varchar(255) DEFAULT NULL COMMENT '请假理由',
  `status` int DEFAULT '0' COMMENT '状态：0待审批，1已通过，2已拒绝',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  `attachment` varchar(255) DEFAULT NULL COMMENT '附件图片地址',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `leave_apply`
--

LOCK TABLES `leave_apply` WRITE;
/*!40000 ALTER TABLE `leave_apply` DISABLE KEYS */;
INSERT INTO `leave_apply` VALUES (1,2,'2026-03-13','2026-04-23','生病\n',1,'2026-03-14 23:30:41',NULL),(2,2,'2026-03-12','2026-04-20','吃饭',0,'2026-03-15 02:02:58',NULL),(3,2,'2026-03-03','2026-03-12','回家',0,'2026-03-15 15:40:24',''),(4,2,'2026-03-24','2026-04-28','不想干了',0,'2026-03-15 17:58:41','http://localhost:8080/files/1773567264881_屏幕截图 2025-07-02 145800.png'),(5,2,'2026-03-12','2026-04-21','还没想到原因',0,'2026-03-15 19:41:22','http://localhost:8080/files/1773574868214_屏幕截图 2025-07-02 145800.png'),(6,2,'2026-04-24','2026-04-24','无',1,'2026-03-18 03:00:08','');
/*!40000 ALTER TABLE `leave_apply` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `overtime_apply`
--

DROP TABLE IF EXISTS `overtime_apply`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `overtime_apply` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `overtime_date` date NOT NULL COMMENT '加班日期',
  `duration` double(4,1) NOT NULL COMMENT '加班时长(小时)',
  `reason` varchar(255) DEFAULT NULL COMMENT '加班原因',
  `status` int DEFAULT '0' COMMENT '状态：0待审批，1已批准，2已拒绝',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `overtime_apply`
--

LOCK TABLES `overtime_apply` WRITE;
/*!40000 ALTER TABLE `overtime_apply` DISABLE KEYS */;
INSERT INTO `overtime_apply` VALUES (1,2,'2026-03-16',1.0,'工作太多',1,'2026-03-15 15:07:22'),(2,1,'2026-03-18',1.0,'',0,'2026-03-18 13:03:15');
/*!40000 ALTER TABLE `overtime_apply` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `schedule`
--

DROP TABLE IF EXISTS `schedule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `schedule` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `work_date` date NOT NULL COMMENT '排班日期',
  `shift_id` bigint NOT NULL COMMENT '关联班次ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=35 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `schedule`
--

LOCK TABLES `schedule` WRITE;
/*!40000 ALTER TABLE `schedule` DISABLE KEYS */;
INSERT INTO `schedule` VALUES (2,2,'2026-03-15',3),(3,3,'2026-03-15',2),(4,2,'2026-03-16',2),(5,1,'2026-03-16',2),(6,3,'2026-03-16',1),(7,1,'2026-03-17',1),(8,2,'2026-03-17',3),(9,2,'2026-03-14',1),(10,2,'2026-03-18',2),(12,2,'2026-03-12',1),(14,2,'2026-03-13',1),(23,2,'2026-04-09',1),(24,2,'2026-04-21',2),(25,2,'2026-02-11',1),(26,4,'2026-03-19',1),(27,4,'2026-03-20',1),(28,4,'2026-03-21',1),(29,1,'2026-03-22',1),(30,1,'2026-03-23',1),(31,2,'2026-03-11',1),(33,1,'2026-03-19',2),(34,3,'2026-03-19',3);
/*!40000 ALTER TABLE `schedule` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) NOT NULL COMMENT '账号',
  `password` varchar(100) NOT NULL COMMENT '密码',
  `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `role` varchar(20) DEFAULT 'EMPLOYEE' COMMENT '角色: ADMIN-管理员, EMPLOYEE-员工',
  `phone` varchar(20) DEFAULT NULL,
  `avatar` varchar(255) DEFAULT NULL,
  `info` text,
  `status` int DEFAULT '0' COMMENT '用户状态: 0-在职, 1-离职',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'test01','123456','张三','EMPLOYEE',NULL,NULL,NULL,0),(2,'admin','admin123','系统管理员','ADMIN_HR','12345','http://localhost:8080/files/1773477481401_屏幕截图 2025-07-02 145800.png','',0),(3,'leader01','123456','李组长','ADMIN_NORMAL',NULL,NULL,NULL,0),(4,'test02','123456','李四','ADMIN_NORMAL','',NULL,NULL,1);
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `work_shift`
--

DROP TABLE IF EXISTS `work_shift`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `work_shift` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `shift_name` varchar(50) NOT NULL COMMENT '班次名称：如早班、晚班',
  `start_time` time NOT NULL COMMENT '上班时间',
  `end_time` time NOT NULL COMMENT '下班时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `work_shift`
--

LOCK TABLES `work_shift` WRITE;
/*!40000 ALTER TABLE `work_shift` DISABLE KEYS */;
INSERT INTO `work_shift` VALUES (1,'早班','08:00:00','10:00:00'),(2,'晚班','13:00:00','22:00:00'),(3,'午班','10:00:00','13:00:00');
/*!40000 ALTER TABLE `work_shift` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'attendance_system'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-03-20  0:23:35
