-- ============================================================
-- اسکیمای کامل و نهایی بازاریار (شامل جدول‌های فاز ۱ و ۲)
-- کل این فایل رو تو SQL Editor پروژه Supabase اجرا کن
-- ============================================================

drop table if exists public.vendor_transactions cascade;
drop table if exists public.vendor_contacts cascade;
drop table if exists public.vendor_posts cascade;
drop table if exists public.vendor_social_links cascade;
drop table if exists public.discounts cascade;
drop table if exists public.customer_vendor_follows cascade;
drop table if exists public.payments cascade;
drop table if exists public.vendors cascade;
drop table if exists public.marketers cascade;
drop table if exists public.profiles cascade;

-- ------------------------------------------------------------
-- پروفایل پایه هر کاربر
-- ------------------------------------------------------------
create table public.profiles (
  id uuid primary key references auth.users (id) on delete cascade,
  first_name text,
  last_name text,
  full_name text,
  phone text,
  role text not null check (role in ('admin','marketer','vendor','customer')),
  no_criminal_record boolean default false,
  approval_status text not null default 'approved'
      check (approval_status in ('pending','approved','rejected')),
  account_status text not null default 'active'
      check (account_status in ('active','suspended')),
  created_at timestamptz default now()
);

create table public.marketers (
  id uuid primary key references public.profiles (id) on delete cascade,
  referral_code text unique not null,
  commission_percent numeric default 40,
  created_at timestamptz default now()
);

create table public.vendors (
  id uuid primary key references public.profiles (id) on delete cascade,
  business_name text not null default '',
  address text,
  description text,
  marketer_id uuid references public.marketers (id),
  referral_code_used text,
  plan_price bigint not null default 500000,
  marketer_commission bigint not null default 200000,
  subscription_status text not null default 'pending'
      check (subscription_status in ('pending','active','expired','canceled')),
  subscription_start date,
  subscription_end date,
  created_at timestamptz default now()
);

create table public.payments (
  id uuid primary key default gen_random_uuid(),
  vendor_id uuid not null references public.vendors (id),
  amount bigint not null,
  marketer_share bigint not null,
  admin_share bigint not null,
  period_start date not null,
  period_end date not null,
  status text not null default 'unpaid' check (status in ('unpaid','paid')),
  paid_at timestamptz,
  created_at timestamptz default now()
);

-- مشتری‌هایی که یک کاسب را مارک/دنبال کرده‌اند
create table public.customer_vendor_follows (
  customer_id uuid not null references public.profiles (id) on delete cascade,
  vendor_id uuid not null references public.vendors (id) on delete cascade,
  created_at timestamptz default now(),
  primary key (customer_id, vendor_id)
);

-- تخفیف‌های هر کاسب (در پنل مشتری دیده می‌شود)
create table public.discounts (
  id uuid primary key default gen_random_uuid(),
  vendor_id uuid not null references public.vendors (id) on delete cascade,
  title text not null,
  description text,
  percent numeric,
  valid_until date,
  is_active boolean default true,
  created_at timestamptz default now()
);

-- لینک‌های فضای مجازی کاسب (در پنل مشتری دیده می‌شود)
create table public.vendor_social_links (
  id uuid primary key default gen_random_uuid(),
  vendor_id uuid not null references public.vendors (id) on delete cascade,
  platform_label text not null,
  url text not null,
  created_at timestamptz default now()
);

-- تبلیغات/محتوای کاسب (در پنل مشتری دیده می‌شود)
create table public.vendor_posts (
  id uuid primary key default gen_random_uuid(),
  vendor_id uuid not null references public.vendors (id) on delete cascade,
  title text not null,
  content text,
  created_at timestamptz default now()
);

-- دفترچه تلفن دستی کاسب (فقط خودش و ادمین می‌بینند - شماره و لینک شخصی مشتری)
create table public.vendor_contacts (
  id uuid primary key default gen_random_uuid(),
  vendor_id uuid not null references public.vendors (id) on delete cascade,
  full_name text not null,
  phone text,
  social_link text,
  notes text,
  created_at timestamptz default now()
);

-- حسابداری ساده کاسب (فقط خودش و ادمین می‌بینند)
create table public.vendor_transactions (
  id uuid primary key default gen_random_uuid(),
  vendor_id uuid not null references public.vendors (id) on delete cascade,
  type text not null check (type in ('income','expense')),
  amount bigint not null,
  description text,
  occurred_at date not null default current_date,
  created_at timestamptz default now()
);

-- ------------------------------------------------------------
-- ساخت خودکار پروفایل هنگام ثبت‌نام
-- ------------------------------------------------------------
create or replace function public.handle_new_user()
returns trigger as $$
declare
  meta jsonb := new.raw_user_meta_data;
  user_role text := coalesce(meta->>'role', 'customer');
begin
  insert into public.profiles
    (id, first_name, last_name, full_name, role, no_criminal_record, approval_status)
  values (
    new.id,
    meta->>'first_name',
    meta->>'last_name',
    trim(coalesce(meta->>'first_name','') || ' ' || coalesce(meta->>'last_name','')),
    user_role,
    coalesce((meta->>'no_criminal_record')::boolean, false),
    case when user_role = 'customer' then 'approved' else 'pending' end
  );
  return new;
end;
$$ language plpgsql security definer;

drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
  after insert on auth.users
  for each row execute procedure public.handle_new_user();

-- ============================================================
-- Row Level Security
-- ============================================================
alter table public.profiles enable row level security;
alter table public.marketers enable row level security;
alter table public.vendors enable row level security;
alter table public.payments enable row level security;
alter table public.customer_vendor_follows enable row level security;
alter table public.discounts enable row level security;
alter table public.vendor_social_links enable row level security;
alter table public.vendor_posts enable row level security;
alter table public.vendor_contacts enable row level security;
alter table public.vendor_transactions enable row level security;

create or replace function public.current_role()
returns text as $$
  select role from public.profiles where id = auth.uid();
$$ language sql stable security definer;

-- profiles
create policy "profiles_select_own_or_admin"
  on public.profiles for select
  using (id = auth.uid() or public.current_role() = 'admin');

create policy "profiles_update_own"
  on public.profiles for update using (id = auth.uid());

create policy "profiles_update_admin"
  on public.profiles for update using (public.current_role() = 'admin');

-- marketers
create policy "marketers_select"
  on public.marketers for select
  using (id = auth.uid() or public.current_role() = 'admin');

create policy "marketers_insert_admin"
  on public.marketers for insert with check (public.current_role() = 'admin');

-- vendors: خودش، بازاریابش، ادمین -- و هر مشتری فقط کاسب‌های فعال را می‌بیند (دایرکتوری عمومی)
create policy "vendors_select_owner_marketer_admin"
  on public.vendors for select
  using (id = auth.uid() or marketer_id = auth.uid() or public.current_role() = 'admin');

create policy "vendors_select_public_for_customers"
  on public.vendors for select
  using (public.current_role() = 'customer' and subscription_status = 'active');

create policy "vendors_insert_admin"
  on public.vendors for insert with check (public.current_role() = 'admin');

create policy "vendors_update_admin"
  on public.vendors for update using (public.current_role() = 'admin');

-- payments
create policy "payments_select"
  on public.payments for select
  using (
    vendor_id = auth.uid()
    or vendor_id in (select id from public.vendors where marketer_id = auth.uid())
    or public.current_role() = 'admin'
  );

create policy "payments_insert_admin"
  on public.payments for insert with check (public.current_role() = 'admin');

-- customer_vendor_follows: مشتری فقط مال خودش را می‌بیند/می‌سازد؛ کاسب می‌تواند دنبال‌کننده‌های خودش را ببیند
create policy "follows_select_own_customer"
  on public.customer_vendor_follows for select
  using (customer_id = auth.uid() or vendor_id = auth.uid() or public.current_role() = 'admin');

create policy "follows_insert_own_customer"
  on public.customer_vendor_follows for insert
  with check (customer_id = auth.uid());

create policy "follows_delete_own_customer"
  on public.customer_vendor_follows for delete
  using (customer_id = auth.uid());

-- discounts / social links / posts: خود کاسب مدیریت می‌کند؛ برای مشتری‌ها فقط کاسب‌های فعال دیده می‌شود
create policy "discounts_select"
  on public.discounts for select
  using (
    vendor_id = auth.uid() or public.current_role() = 'admin'
    or vendor_id in (select id from public.vendors where subscription_status = 'active')
  );
create policy "discounts_manage_owner"
  on public.discounts for all
  using (vendor_id = auth.uid()) with check (vendor_id = auth.uid());

create policy "social_links_select"
  on public.vendor_social_links for select
  using (
    vendor_id = auth.uid() or public.current_role() = 'admin'
    or vendor_id in (select id from public.vendors where subscription_status = 'active')
  );
create policy "social_links_manage_owner"
  on public.vendor_social_links for all
  using (vendor_id = auth.uid()) with check (vendor_id = auth.uid());

create policy "posts_select"
  on public.vendor_posts for select
  using (
    vendor_id = auth.uid() or public.current_role() = 'admin'
    or vendor_id in (select id from public.vendors where subscription_status = 'active')
  );
create policy "posts_manage_owner"
  on public.vendor_posts for all
  using (vendor_id = auth.uid()) with check (vendor_id = auth.uid());

-- vendor_contacts و vendor_transactions: فقط خود کاسب و ادمین (هرگز مشتری یا بازاریاب)
create policy "contacts_owner_admin"
  on public.vendor_contacts for all
  using (vendor_id = auth.uid() or public.current_role() = 'admin')
  with check (vendor_id = auth.uid());

create policy "transactions_owner_admin"
  on public.vendor_transactions for all
  using (vendor_id = auth.uid() or public.current_role() = 'admin')
  with check (vendor_id = auth.uid());
