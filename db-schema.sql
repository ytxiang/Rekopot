CREATE TABLE RekopotDB.Account (
  `id` int(16) NOT NULL AUTO_INCREMENT,
  `userName` varchar(128) BINARY NOT NULL,
  `password` varchar(128) BINARY NOT NULL,
  `fullName` varchar(128) NOT NULL,
  `nick` varchar(32) default NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE RekopotDB.CloudImages (
  `id` int(16) NOT NULL AUTO_INCREMENT,
  `fileName` varchar(128) BINARY NOT NULL,
  `createdTime` Timestamp NOT NULL,
  `modifiedTime` Timestamp NOT NULL,
  `notes` varchar(256) BINARY default NULL,
  `size` varchar(25) NOT NULL,
  `path` varchar(256) BINARY NOT NULL,
  `userId` int(16),
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  FOREIGN KEY (userId) REFERENCES Account(id)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
