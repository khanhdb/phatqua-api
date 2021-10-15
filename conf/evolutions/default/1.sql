CREATE TABLE `officer` (
                           `username` varchar(100) NOT NULL,
                           `password` varchar(100) NOT NULL,
                           `full_name` varchar(255) NOT NULL,
                           PRIMARY KEY (`username`)
);

CREATE TABLE `package` (    `id` INT NOT NULL AUTO_INCREMENT,
                           `name` varchar(255) NOT NULL,
                           `phone` varchar(20),
                           year_of_birth INT,
                           address TEXT,
                           `note` TEXT,
                           `verify_code` varchar(30),
                           `status` INT NOT NULL DEFAULT 0,
                           `created_at` DATETIME NOT NULL DEFAULT NOW(),
                           `updated_at` DATETIME,
                           `updated_by` varchar(255),
                           `campaign_id` INT NOT NULL,
                           PRIMARY KEY (`id`)
);

CREATE TABLE `campaign` (
                            `id` INT NOT NULL AUTO_INCREMENT,
                            `name` varchar(255) NOT NULL,
                            `location` TEXT NOT NULL,
                            PRIMARY KEY (`id`)
);

ALTER TABLE `package` ADD CONSTRAINT `package_fk0` FOREIGN KEY (`updated_by`) REFERENCES `officer`(`username`);

ALTER TABLE `package` ADD CONSTRAINT `package_fk1` FOREIGN KEY (`campaign_id`) REFERENCES `campaign`(`id`);