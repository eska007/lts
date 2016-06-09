-- phpMyAdmin SQL Dump
-- version 4.0.10deb1
-- http://www.phpmyadmin.net
--
-- 호스트: localhost
-- 처리한 시간: 16-06-08 12:44
-- 서버 버전: 5.5.49-0ubuntu0.14.04.1
-- PHP 버전: 5.5.9-1ubuntu4.17

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- 데이터베이스: `LTS`
--

-- --------------------------------------------------------

--
-- 테이블 구조 `workitems`
--

CREATE TABLE IF NOT EXISTS `workitems` (
  `subject` text NOT NULL,
  `id` int(11) NOT NULL,
  `doc_type` text NOT NULL,
  `source_language` int(11) NOT NULL,
  `target_language` int(11) NOT NULL,
  `pages` text NOT NULL,
  `level` text NOT NULL,
  `biding` tinyint(1) NOT NULL,
  `cost` text NOT NULL,
  `is_paid` tinyint(1) NOT NULL,
  `is_reviewed` tinyint(1) NOT NULL,
  `source_doc_path` text NOT NULL,
  `request_date` datetime NOT NULL,
  `due_date` datetime NOT NULL,
  `requester_id` text NOT NULL,
  `translator_candidate_list` text NOT NULL,
  `reviewer_candidate_list` text NOT NULL,
  `translator_id` text NOT NULL,
  `reviewer_id` text NOT NULL,
  `translated_doc_path` text NOT NULL,
  `reviewed_doc_path` text NOT NULL,
  `final_doc_path` text NOT NULL,
  `translator_score` int(11) NOT NULL,
  `reviewer_score` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- 테이블의 덤프 데이터 `workitems`
--

INSERT INTO `workitems` (`subject`, `id`, `doc_type`, `source_language`, `target_language`, `pages`, `level`, `biding`, `cost`, `is_paid`, `is_reviewed`, `source_doc_path`, `request_date`, `due_date`, `requester_id`, `translator_candidate_list`, `reviewer_candidate_list`, `translator_id`, `reviewer_id`, `translated_doc_path`, `reviewed_doc_path`, `final_doc_path`, `translator_score`, `reviewer_score`) VALUES
('SE텍스트북', 1, 'Book', 1, 0, '900', '3', 0, '5000$', 0, 0, 'se_textbook.txt', '2016-06-04 07:44:00', '2016-07-04 07:44:00', 'req1', '', '', '', '', '', '', '', 0, 0),
('무역계약서', 2, 'Contract', 1, 0, '5', '1', 0, '10$', 0, 0, 'contract1.txt', '2016-06-08 02:44:00', '2016-06-10 07:44:00', 'req1', '', '', '', '', '2_IMG_8647.JPG', '', '', 0, 0);

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
