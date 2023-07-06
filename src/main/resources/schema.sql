create table if not exists Scripts (
    id bigint auto_increment primary key,
    body varchar(5000) not null,
    status enum('queued', 'executing', 'completed', 'failed', 'interrupted') not null default 'queued',
    output varchar(10000),
    pub_time timestamp(3) not null default current_timestamp(3),
    sched_time timestamp(3),
    exec_time INT
);
