create table if not exists app_user (
  id bigserial primary key,
  username varchar(50) not null unique,
  email varchar(255) not null unique,
  password varchar(100) not null,
  role varchar(20) not null,
  enabled boolean not null default true,
  created_at timestamp not null default now()
);
