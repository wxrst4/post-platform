create schema if not exists content;

create table if not exists content.channels
(
    id          uuid primary key,
    owner_id    uuid         not null,
    name        varchar(100) not null,
    description text,
    created_at  timestamp    not null default current_timestamp,

    constraint uk_channels_name unique (name)
);

create table if not exists content.posts
(
    id           uuid primary key,
    channel_id   uuid         not null,
    author_id    uuid         not null,
    title        varchar(255) not null,
    content      text         not null,
    status       varchar(50)  not null,
    created_at   timestamp    not null default current_timestamp,
    updated_at   timestamp    not null default current_timestamp,
    published_at timestamp
);

create index if not exists idx_posts_channel_id on content.posts (channel_id);
create index if not exists idx_posts_author_id on content.posts (author_id);
create index if not exists idx_posts_channel_status on content.posts (channel_id, status);