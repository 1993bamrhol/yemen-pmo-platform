alter table if exists support_requests
    add column if not exists status varchar(30) not null default 'new';
