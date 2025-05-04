-- 修改 media 表的 type 列，添加 IMAGE 类型
ALTER TABLE media MODIFY COLUMN type ENUM('AUDIO', 'DOCUMENT', 'VIDEO', 'IMAGE');
