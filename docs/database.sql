-- 区块链电子发票系统数据库脚本
-- 创建数据库
CREATE DATABASE IF NOT EXISTS invoice_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE invoice_db;

-- 发票主表
CREATE TABLE IF NOT EXISTS `t_invoice` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `invoice_uuid` CHAR(32) NOT NULL COMMENT '内部唯一标识UUID',
  `invoice_code` VARCHAR(20) NOT NULL COMMENT '发票代码',
  `invoice_number` VARCHAR(20) NOT NULL COMMENT '发票号码',
  `check_code` VARCHAR(6) NOT NULL COMMENT '校验码（后6位）',
  `buyer_name` VARCHAR(100) NOT NULL COMMENT '购买方名称',
  `buyer_tax_id` VARCHAR(50) DEFAULT NULL COMMENT '购买方税号',
  `buyer_address_phone` VARCHAR(200) DEFAULT NULL COMMENT '购买方地址电话',
  `buyer_bank_account` VARCHAR(100) DEFAULT NULL COMMENT '购买方开户行账号',
  `seller_name` VARCHAR(100) NOT NULL COMMENT '销售方名称',
  `seller_tax_id` VARCHAR(50) DEFAULT NULL COMMENT '销售方税号',
  `seller_address_phone` VARCHAR(200) DEFAULT NULL COMMENT '销售方地址电话',
  `seller_bank_account` VARCHAR(100) DEFAULT NULL COMMENT '销售方开户行账号',
  `total_amount` DECIMAL(12,2) NOT NULL COMMENT '总金额（含税）',
  `tax_amount` DECIMAL(12,2) NOT NULL COMMENT '税额',
  `net_amount` DECIMAL(12,2) NOT NULL COMMENT '不含税金额',
  `status` TINYINT NOT NULL DEFAULT '0' COMMENT '状态：0-待签发，1-已签发，2-已作废',
  `pdf_url` VARCHAR(255) DEFAULT NULL COMMENT 'PDF文件路径',
  `tx_hash` CHAR(66) DEFAULT NULL COMMENT '区块链交易哈希',
  `block_height` BIGINT DEFAULT NULL COMMENT '区块高度',
  `chain_proof_exists` TINYINT NOT NULL DEFAULT '0' COMMENT '区块链存证是否存在：0-否，1-是',
  `invoice_date` DATE NOT NULL COMMENT '开票日期',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_invoice_uuid` (`invoice_uuid`),
  UNIQUE KEY `uk_invoice_code_number` (`invoice_code`, `invoice_number`),
  KEY `idx_buyer_name` (`buyer_name`),
  KEY `idx_seller_name` (`seller_name`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='发票主表';

-- 发票明细表
CREATE TABLE IF NOT EXISTS `t_invoice_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `invoice_id` BIGINT NOT NULL COMMENT '发票主表ID',
  `item_name` VARCHAR(200) NOT NULL COMMENT '商品或服务名称',
  `specification` VARCHAR(100) DEFAULT NULL COMMENT '规格型号',
  `unit` VARCHAR(20) DEFAULT NULL COMMENT '单位',
  `quantity` DECIMAL(10,2) NOT NULL COMMENT '数量',
  `unit_price` DECIMAL(12,2) NOT NULL COMMENT '单价',
  `amount` DECIMAL(12,2) NOT NULL COMMENT '金额',
  `tax_rate` DECIMAL(5,4) NOT NULL COMMENT '税率',
  `tax_amount` DECIMAL(12,2) NOT NULL COMMENT '税额',
  PRIMARY KEY (`id`),
  KEY `idx_invoice_id` (`invoice_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='发票明细表';

-- 插入测试数据
INSERT INTO `t_invoice` (
  `invoice_uuid`, `invoice_code`, `invoice_number`, `check_code`,
  `buyer_name`, `buyer_tax_id`, `buyer_address_phone`, `buyer_bank_account`,
  `seller_name`, `seller_tax_id`, `seller_address_phone`, `seller_bank_account`,
  `total_amount`, `tax_amount`, `net_amount`, `status`, `chain_proof_exists`,
  `invoice_date`
) VALUES (
  'a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6', '011001900111', '12345678', 'ABC123',
  '测试购买方有限公司', '91110000123456789X', '北京市朝阳区测试路123号 010-12345678', '中国工商银行测试支行 123456789012',
  '测试销售方有限公司', '91110000987654321X', '北京市海淀区测试街456号 010-98765432', '中国建设银行测试分行 987654321098',
  11300.00, 1300.00, 10000.00, 1, 1,
  '2024-01-15'
);

-- 插入发票明细
INSERT INTO `t_invoice_item` (
  `invoice_id`, `item_name`, `specification`, `unit`, `quantity`,
  `unit_price`, `amount`, `tax_rate`, `tax_amount`
) VALUES (
  1, '测试商品A', '规格A', '件', 100,
  100.00, 10000.00, 0.1300, 1300.00
);
