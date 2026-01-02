insert into app_user (username, email, password, role, enabled)
values
  ('alice', 'alice@example.com', 'x', 'USER', true),
  ('bob', 'bob@example.com', 'x', 'USER', true),
  ('admin_db', 'admin_db@example.com', 'x', 'ADMIN', true)
on conflict (username) do nothing;
