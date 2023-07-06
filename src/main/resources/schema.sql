create table if not exists Scripts (
    id int auto_increment primary key,
    body varchar(5000) not null,
    status enum('queued', 'executing', 'completed', 'failed', 'interrupted') not null default 'queued',
    output varchar(10000),
    sched_time timestamp,
    exec_time INT
);
