CREATE TABLE IF NOT EXISTS `api_consumer` (
  `username` varchar(32) NOT NULL,
  `password_hash` varchar(120) NOT NULL,
  `roles` varchar(512) NOT NULL default '',
  PRIMARY KEY (`username`));

CREATE TABLE IF NOT EXISTS `api_consumer_configuration` (
  `id` int not null auto_increment primary key,
  `username` varchar(32) NOT NULL,
  `enc_key` varchar(512) NOT NULL,
  `active` BIT NOT NULL,
  `algorithm` varchar(30) NOT NULL,
  UNIQUE KEY `one_active_per_username_idx` (`username`,`active`),
  FOREIGN KEY (username) REFERENCES api_consumer(username) ON DELETE CASCADE,
  KEY `username_idx` (`username`));
