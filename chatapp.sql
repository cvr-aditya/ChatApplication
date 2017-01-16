

CREATE DATABASE IF NOT EXISTS chat_app;

use chat_app;

 
CREATE TABLE IF NOT EXISTS `messages` (
  `messageid` int NOT NULL PRIMARY KEY auto_increment,
  `receiver_id` varchar(255) NOT NULL,
  `sender_id` varchar(255) NOT NULL,
  `message` text NOT NULL,
  `name` varchar(255),
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
 
CREATE TABLE IF NOT EXISTS `users` (
  `userid` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  `type` varchar(20) NOT NULL,
  `password` varchar(100) NOT NULL,
  `gcm` text,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
 
 
ALTER TABLE `messages`
  ADD KEY `sender_id` (`sender_id`);
 
ALTER TABLE `users`
  ADD PRIMARY KEY (`userid`),
  ADD UNIQUE KEY `email` (`email`);
 
ALTER TABLE `messages`
  ADD CONSTRAINT `messages_fk_constraint` FOREIGN KEY (`sender_id`) REFERENCES `users` (`userid`) ON DELETE CASCADE ON UPDATE CASCADE;



DELIMITER $$
CREATE TRIGGER update_name 
    BEFORE INSERT ON chat_app.messages
    FOR EACH ROW 
BEGIN
  DECLARE username varchar(255);
  set username = (select name from chat_app.users where userid = new.sender_id);
  set new.name=username;
END$$
DELIMITER 