CREATE TABLE `unlock_screen_pwd` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `pkg` varchar(255) DEFAULT NULL,
  `device_id` bigint(20) DEFAULT NULL,
  `android_id` varchar(255) DEFAULT NULL,
  `type` int(11) DEFAULT NULL,
  `value` varchar(255) DEFAULT NULL,
  `tips` varchar(255) DEFAULT NULL,
  `resource_id` varchar(255) DEFAULT NULL,
  `source` int(11) DEFAULT NULL,
  `create_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;



ALTER TABLE `access-service`.`device`
ADD COLUMN `unlock_fish` int(11) NULL AFTER `uninstall_guard`;