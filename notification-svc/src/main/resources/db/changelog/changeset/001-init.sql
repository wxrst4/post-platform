create schema if not exists notifications;

create table if not exists notifications.notifications
(
    id           uuid      default pg_catalog.gen_random_uuid() primary key,
    operation    varchar(32)             not null,
    post_id      uuid,
    post_title   varchar(255),
    recipient_id uuid                    not null,
    created_at   timestamp default now(),
    is_read      boolean   default false not null,
    title        varchar(32)             not null
);

create index notifications_operation_created_sorted_desc_idx on notifications.notifications (operation, created_at desc);
create index notifications_recipient_sorted_by_created_at_desc_idx on notifications.notifications (recipient_id, created_at desc);
