create table if not exists Script (
    id int auto_increment primary key,
    body varchar(5000) not null,
    status enum('queued', 'executing', 'completed', 'failed', 'interrupted') not null default 'queued',
    output varchar(5000),
    errors varchar(5000),
    sched_time timestamp,
    exec_time timestamp
);
