create table if not exists content.post_media
(
    id            uuid primary key,
    post_id       uuid          not null,
    object_key    varchar(1000) not null,
    original_name varchar(500)  not null,
    content_type  varchar(255)  not null,
    size_bytes    bigint        not null,
    created_at    timestamp     not null default now()
);

create index if not exists idx_post_media_post_id on content.post_media (post_id);