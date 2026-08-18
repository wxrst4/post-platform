create schema if not exists social;

create table if not exists social.subscriptions
(
    id         uuid default pg_catalog.gen_random_uuid()primary key,
    user_id    uuid      not null,
    channel_id uuid      not null,
    created_at timestamp not null default now(),

    constraint uk_subscription_user_channel unique (user_id, channel_id)
);

create index if not exists idx_subscriptions_channel_id on social.subscriptions (channel_id);
create index if not exists idx_subscriptions_user_id on social.subscriptions (user_id);

create table if not exists social.likes
(
    id         uuid default pg_catalog.gen_random_uuid()primary key,
    user_id    uuid      not null,
    post_id    uuid      not null,
    created_at timestamp not null default now(),

    constraint uk_like_user_post unique (user_id, post_id)
);

create index if not exists idx_likes_post_id on social.likes (post_id);
create index if not exists idx_likes_user_id on social.likes (user_id);

create table if not exists social.comments
(
    id         uuid default pg_catalog.gen_random_uuid()primary key,
    user_id    uuid      not null,
    post_id    uuid      not null,
    content    text      not null,
    created_at timestamp not null default now(),
    updated_at timestamp not null default now()
);

create index if not exists idx_comments_post_id on social.comments (post_id);
create index if not exists idx_comments_user_id on social.comments (user_id);