CREATE TABLE `ratings` (
  `user_id` int(11) NOT NULL,
  `item_id` int(11) NOT NULL,
  `rating` float(1,1) DEFAULT '0.0',
  PRIMARY KEY (`user_id`,`item_id`)
) ENGINE=InnoDB;