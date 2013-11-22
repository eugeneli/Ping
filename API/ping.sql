-- phpMyAdmin SQL Dump
-- version 3.5.8.1
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: Nov 22, 2013 at 10:12 AM
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
  `creator_id` int(36) NOT NULL,
  `create_date` int(11) NOT NULL,
  `latitude` float DEFAULT NULL,
  `longitude` float DEFAULT NULL,
  `has_image` tinyint(1) NOT NULL,
  `rating` int(11) NOT NULL,
  `message` text NOT NULL,
  `b64image` text NOT NULL,
  PRIMARY KEY (`ping_id`),
  KEY `latitude` (`latitude`),
  KEY `longitude` (`longitude`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

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
('528998d0e8333', 'asd', 'cd192bfb24a9f7c93a72ba62a5da02f54d345432b1e31e19078d3d351c71af80', '20', 0, 5, 'ddf7174d1c50a7b768ddfcd8213784d1'),
('528ef335a81d5', 'awsssd', 'b399957be000c0fede81495563351ecb0d38bb7c64a127337cb8a2b21b071289', '1dfcdfca9a34d77a9d6b', 0, 5, '909708b80e755aff7020daeee7cc0cba'),
('528ef3758bcc8', 'DFDFDFDFDF', '4911587e6cd0239c9f3687648d2b0d9396e1230bbeb5fd07bb0d071dd3288c44', '81385ae1a503670cf365', 0, 5, '45cdc35daeecdca621bf5f7d47057c09'),
('528ef3c977d98', 'DFDFdDFDFDF', '2bef8cb9d4a5293d19cd8b2afc212d29771f1a74d4e330313bbfd6a291badc68', '9ebfb09f91f86edf8f25', 0, 5, '59eda95dc51cb4207728e8e8d681c104');

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
