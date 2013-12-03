-- phpMyAdmin SQL Dump
-- version 3.5.8.1
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: Dec 03, 2013 at 09:49 AM
-- Server version: 5.1.69
-- PHP Version: 5.3.3

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `ping`
--

-- --------------------------------------------------------

--
-- Table structure for table `pings`
--

CREATE TABLE IF NOT EXISTS `pings` (
  `ping_id` varchar(36) NOT NULL,
  `creator_id` varchar(36) NOT NULL,
  `create_date` bigint(14) NOT NULL,
  `latitude` float DEFAULT NULL,
  `longitude` float DEFAULT NULL,
  `has_image` tinyint(1) NOT NULL,
  `rating` int(11) NOT NULL,
  `message` text,
  `b64image` text,
  PRIMARY KEY (`ping_id`),
  KEY `latitude` (`latitude`),
  KEY `longitude` (`longitude`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `pings`
--

INSERT INTO `pings` (`ping_id`, `creator_id`, `create_date`, `latitude`, `longitude`, `has_image`, `rating`, `message`, `b64image`) VALUES
('5', '5', 5, 40.6941, -73.9869, 5, 0, '5', '5'),
('4fde9cd4-594a-11e3-8ee0-d8ba1e65a2fa', '6', 6, 40.7299, -73.9977, 0, 2, '2', '2'),
('529d703df1235', '52980ecae75b8', 2147483647, 40.6922, -73.9803, 0, 0, 'GERONIMO', NULL);

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE IF NOT EXISTS `users` (
  `user_id` varchar(36) NOT NULL,
  `name` varchar(32) NOT NULL,
  `password` varchar(64) DEFAULT NULL,
  `salt` varchar(64) DEFAULT NULL,
  `radius` int(11) NOT NULL,
  `remaining_pings` int(11) NOT NULL,
  `auth` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`user_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`user_id`, `name`, `password`, `salt`, `radius`, `remaining_pings`, `auth`) VALUES
('52980ecae75b8', 'simon', '34d15e5fde0d9419199b339dc94f616f320cd5b58f8166d7d9a3be3e156a6e80', '5a5d151a38956ce71e51', 0, 5, 'f5ab753c1a5fb9c44f65df9551d43741'),
('5297fd91927ef', 'lolimakesmehard', '8f5af43ae680ea8e36962e69779937c129634bb5d9fd313c8917b804e9f188e1', 'be15cbd320a967480e0a', 0, 5, 'b71adccd5ade6c747f63740b3fbe095a'),
('5297fc2a57c43', 'lolimakesmehorny', '719b39011811f08ae60522cf61fbc065c3a3066ac6dd32ea3a32307ad70d3c6d', '759d87c19ba7d5fc60e8', 0, 5, '1c54ce8f04f28d1183595223d3dd7fcc');

-- --------------------------------------------------------

--
-- Table structure for table `votes`
--

CREATE TABLE IF NOT EXISTS `votes` (
  `user_id` varchar(36) NOT NULL,
  `ping_id` varchar(36) NOT NULL,
  `vote` int(1) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
