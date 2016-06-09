-- phpMyAdmin SQL Dump
-- version 4.0.10deb1
-- http://www.phpmyadmin.net
--
-- 호스트: localhost
-- 처리한 시간: 16-06-08 12:19
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
-- 테이블 구조 `members`
--

CREATE TABLE IF NOT EXISTS `members` (
  `id` varchar(32) NOT NULL,
  `password` text NOT NULL,
  `first_name` text NOT NULL,
  `family_name` text NOT NULL,
  `email` text NOT NULL,
  `phone` text NOT NULL,
  `country` text NOT NULL,
  `address` text NOT NULL,
  `sex` tinyint(1) NOT NULL,
  `birthday` date NOT NULL,
  `user_mode` tinyint(4) NOT NULL,
  `degree` text NOT NULL,
  `college` text NOT NULL,
  `graduate` text NOT NULL,
  `certification` text NOT NULL,
  `resume` text NOT NULL,
  `account` text NOT NULL,
  `worklist` text NOT NULL COMMENT '고용되어 작업 진행중이거나 완료된 리퀘스트들.',
  `new_request` text NOT NULL COMMENT '이 멤버에게 전달된 모든 리퀘스트들(모바일앱 통해 아직 알림이 전달되지 않은 것들도 포함)',
  `language` int(11) NOT NULL,
  `_notified_new_request` text NOT NULL COMMENT '이 멤버에게 전달된 리퀘스트들 중 모바일앱 통해 알림 전달이 완료된 것들.',
  `_applied_request` text NOT NULL COMMENT '전달된 리퀘스트들 중 지원(bid)했던 리퀘스트들',
  `score` float NOT NULL,
  `score_count` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='member profiles';

--
-- 테이블의 덤프 데이터 `members`
--

INSERT INTO `members` (`id`, `password`, `first_name`, `family_name`, `email`, `phone`, `country`, `address`, `sex`, `birthday`, `user_mode`, `degree`, `college`, `graduate`, `certification`, `resume`, `account`, `worklist`, `new_request`, `language`, `_notified_new_request`, `_applied_request`, `score`, `score_count`) VALUES
('req1', '6b86b273ff34fce19d6b804eff5a3f5747ada4eaa22f1d49c01e52ddb7875b4b', '의뢰', '김', 'kim@naver.com', '', '', '', 0, '1923-12-01', 0, '', '', '', '', '', '', ';1;2', '0', 0, '0', '', 0, 0),
('rev1', '6b86b273ff34fce19d6b804eff5a3f5747ada4eaa22f1d49c01e52ddb7875b4b', '감수', '박', 'park@naver.com', '', '', '', 0, '2001-02-29', 2, '', '', '', '', '', '', '', '', 1, '', '', 0, 0),
('tra1', '6b86b273ff34fce19d6b804eff5a3f5747ada4eaa22f1d49c01e52ddb7875b4b', '번역', '홍', 'hong@naver.com', '', '', '', 0, '1980-01-01', 1, '', '', '', '', '', '', '', ';1;2', 1, '', '', 0, 0),
('req2', '6b86b273ff34fce19d6b804eff5a3f5747ada4eaa22f1d49c01e52ddb7875b4b', '의뢰인', '김', 'kim2@naver.com', '', '', '', 0, '1923-12-01', 0, '', '', '', '', '', '', '', '0', 0, '0', '', 0, 0),
('rev2', '6b86b273ff34fce19d6b804eff5a3f5747ada4eaa22f1d49c01e52ddb7875b4b', '감수자', '이', 'park2@naver.com', '', '', '', 0, '2001-02-29', 2, '', '', '', '', '', '', '', '', 1, '', '', 0, 0),
('tra2', '6b86b273ff34fce19d6b804eff5a3f5747ada4eaa22f1d49c01e52ddb7875b4b', '번역가', '왕', 'hong2@naver.com', '', '', '', 0, '1980-01-01', 1, '', '', '', '', '', '', '', ';1;2', 1, '', '', 0, 0),
('req3', '6b86b273ff34fce19d6b804eff5a3f5747ada4eaa22f1d49c01e52ddb7875b4b', '의뢰3', '김', 'kim3@naver.com', '', '', '', 0, '1923-12-01', 0, '', '', '', '', '', '', '', '0', 0, '0', '', 0, 0),
('rev3', '6b86b273ff34fce19d6b804eff5a3f5747ada4eaa22f1d49c01e52ddb7875b4b', '감수3', '박', 'park3@naver.com', '', '', '', 0, '2001-02-29', 2, '', '', '', '', '', '', '', '', 2, '', '', 0, 0),
('tra3', '6b86b273ff34fce19d6b804eff5a3f5747ada4eaa22f1d49c01e52ddb7875b4b', '번역3', '홍', 'hong3@naver.com', '', '', '', 0, '1980-01-01', 1, '', '', '', '', '', '', '', '', 2, '', '', 0, 0);

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
