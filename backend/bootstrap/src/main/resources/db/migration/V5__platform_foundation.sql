create table entity_types (
    id bigint primary key,
    code varchar(50) not null unique,
    name_ar varchar(150) not null,
    public_path_segment varchar(80) not null unique,
    active boolean not null default true
);

create table government_entities (
    id uuid primary key,
    entity_type_id bigint not null,
    parent_entity_id uuid,
    official_name_ar varchar(255) not null,
    short_name_ar varchar(150),
    slug varchar(160) not null,
    status varchar(30) not null,
    description text,
    website_url varchar(500),
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    created_by bigint,
    updated_by bigint,
    constraint fk_government_entities_type foreign key (entity_type_id) references entity_types (id),
    constraint fk_government_entities_parent foreign key (parent_entity_id) references government_entities (id),
    constraint fk_government_entities_created_by foreign key (created_by) references users (id),
    constraint fk_government_entities_updated_by foreign key (updated_by) references users (id),
    constraint uq_government_entities_type_slug unique (entity_type_id, slug),
    constraint ck_government_entities_not_own_parent check (parent_entity_id is null or parent_entity_id <> id),
    constraint ck_government_entities_status check (status in ('DRAFT', 'ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

create index idx_government_entities_parent on government_entities (parent_entity_id);
create index idx_government_entities_type_status on government_entities (entity_type_id, status);

create table entity_relationships (
    id uuid primary key,
    source_entity_id uuid not null,
    target_entity_id uuid not null,
    relationship_type varchar(40) not null,
    valid_from date,
    valid_to date,
    created_at timestamp with time zone not null,
    created_by bigint,
    constraint fk_entity_relationship_source foreign key (source_entity_id) references government_entities (id),
    constraint fk_entity_relationship_target foreign key (target_entity_id) references government_entities (id),
    constraint fk_entity_relationship_created_by foreign key (created_by) references users (id),
    constraint uq_entity_relationship unique (source_entity_id, target_entity_id, relationship_type),
    constraint ck_entity_relationship_not_self check (source_entity_id <> target_entity_id),
    constraint ck_entity_relationship_dates check (valid_to is null or valid_from is null or valid_to >= valid_from),
    constraint ck_entity_relationship_type check (relationship_type in ('PARENT_OF', 'SUBORDINATE_TO', 'OVERSEEN_BY', 'AFFILIATED_WITH', 'SERVES_JURISDICTION'))
);

create index idx_entity_relationship_target on entity_relationships (target_entity_id);

create table role_assignments (
    id uuid primary key,
    user_id bigint not null,
    role_id bigint not null,
    scope_type varchar(20) not null,
    government_entity_id uuid,
    valid_from timestamp with time zone,
    valid_until timestamp with time zone,
    enabled boolean not null default true,
    granted_by bigint,
    created_at timestamp with time zone not null,
    constraint fk_role_assignments_user foreign key (user_id) references users (id) on delete cascade,
    constraint fk_role_assignments_role foreign key (role_id) references roles (id) on delete cascade,
    constraint fk_role_assignments_entity foreign key (government_entity_id) references government_entities (id) on delete cascade,
    constraint fk_role_assignments_granted_by foreign key (granted_by) references users (id),
    constraint ck_role_assignments_scope check (
        (scope_type = 'PLATFORM' and government_entity_id is null)
        or (scope_type = 'ENTITY' and government_entity_id is not null)
    ),
    constraint ck_role_assignments_dates check (valid_until is null or valid_from is null or valid_until >= valid_from)
);

create unique index uq_active_role_assignment
    on role_assignments (user_id, role_id, scope_type, government_entity_id);
create index idx_role_assignments_user_enabled on role_assignments (user_id, enabled);
create index idx_role_assignments_entity on role_assignments (government_entity_id);

create table audit_events (
    id uuid primary key,
    actor_user_id bigint,
    action varchar(120) not null,
    resource_type varchar(80) not null,
    resource_id varchar(120),
    government_entity_id uuid,
    outcome varchar(20) not null,
    correlation_id varchar(100),
    metadata text,
    occurred_at timestamp with time zone not null,
    constraint fk_audit_events_actor foreign key (actor_user_id) references users (id),
    constraint fk_audit_events_entity foreign key (government_entity_id) references government_entities (id),
    constraint ck_audit_events_outcome check (outcome in ('SUCCESS', 'DENIED', 'FAILURE'))
);

create index idx_audit_events_entity_time on audit_events (government_entity_id, occurred_at);
create index idx_audit_events_actor_time on audit_events (actor_user_id, occurred_at);

insert into entity_types (id, code, name_ar, public_path_segment, active) values
    (1, 'PRIME_MINISTERS_OFFICE', 'رئاسة مجلس الوزراء', 'prime-ministers-office', true),
    (2, 'MINISTRY', 'وزارة', 'ministries', true),
    (3, 'AUTHORITY', 'هيئة أو مؤسسة', 'authorities', true),
    (4, 'INDEPENDENT_ENTITY', 'جهة مستقلة', 'independent-entities', true),
    (5, 'GOVERNORATE', 'محافظة أو سلطة محلية', 'governorates', true);

insert into government_entities (
    id, entity_type_id, parent_entity_id, official_name_ar, short_name_ar, slug,
    status, description, website_url, created_at, updated_at
) values (
    '00000000-0000-0000-0000-000000000001',
    1,
    null,
    'رئاسة مجلس الوزراء اليمني',
    'رئاسة مجلس الوزراء',
    'prime-ministers-office',
    'ACTIVE',
    'الجهة التنفيذية المركزية المسؤولة عن تنسيق السياسات الحكومية ومتابعة أولويات الحكومة.',
    null,
    current_timestamp,
    current_timestamp
);
