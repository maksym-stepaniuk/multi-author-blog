create table if not exists post (
  id bigserial primary key,
  title varchar(200) not null,
  content text not null,
  created_at timestamp not null default now(),
  updated_at timestamp not null default now()
);

create index if not exists idx_post_created_at on post(created_at desc);

create table if not exists post_author (
  post_id bigint not null references post(id) on delete cascade,
  user_id bigint not null references app_user(id) on delete cascade,
  primary key (post_id, user_id)
);

create index if not exists idx_post_author_user_id on post_author(user_id);

create table if not exists comment (
  id bigserial primary key,
  post_id bigint not null references post(id) on delete cascade,
  author_id bigint not null references app_user(id) on delete cascade,
  content text not null,
  created_at timestamp not null default now()
);

create index if not exists idx_comment_post_id on comment(post_id);
create index if not exists idx_comment_created_at on comment(created_at desc);

create table if not exists post_rating (
  id bigserial primary key,
  post_id bigint not null references post(id) on delete cascade,
  user_id bigint not null references app_user(id) on delete cascade,
  value smallint not null,
  created_at timestamp not null default now(),
  constraint chk_post_rating_value check (value between 1 and 5),
  constraint uq_post_rating_post_user unique (post_id, user_id)
);

create index if not exists idx_post_rating_post_id on post_rating(post_id);

create table if not exists message (
  id bigserial primary key,
  sender_id bigint not null references app_user(id) on delete cascade,
  recipient_id bigint not null references app_user(id) on delete cascade,
  content text not null,
  created_at timestamp not null default now()
);

create index if not exists idx_message_sender_id on message(sender_id);
create index if not exists idx_message_recipient_id on message(recipient_id);
create index if not exists idx_message_created_at on message(created_at desc);
