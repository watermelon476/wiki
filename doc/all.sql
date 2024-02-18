drop table if exists `test`;
create table `test`(
    `id` bigint not null comment 'id',
    `name` varchar(50) comment '名称',
    `password` varchar(50) comment '密码',
    primary key (`id`)
)engine=innodb default character set =utf8mb4 comment ='测试';

INSERT INTO test (id, name, password)
VALUES (2, 'lili', '654321');

drop table if exists `demo`;
create table `demo`(
                       `id` bigint not null comment 'id',
                       `name` varchar(50) comment '名称',
                       primary key (`id`)
)engine=innodb default character set =utf8mb4 comment ='测试';

INSERT INTO demo (id, name)
VALUES (2, 'lili');
