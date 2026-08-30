create table government_services (
    id uuid primary key,
    owning_entity_id uuid not null,
    slug varchar(160) not null,
    official_name_ar varchar(255) not null,
    official_name_en varchar(255),
    summary_ar varchar(1000),
    description_ar text,
    fees_ar varchar(2000),
    processing_time_ar varchar(1000),
    lifecycle_status varchar(20) not null,
    verification_status varchar(20) not null default 'UNVERIFIED',
    provenance_source_type varchar(40),
    provenance_source_reference varchar(1000),
    verified_at timestamp with time zone,
    verified_by bigint,
    first_published_at timestamp with time zone,
    last_published_at timestamp with time zone,
    archived_at timestamp with time zone,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    created_by bigint,
    updated_by bigint,
    version bigint not null default 0,
    constraint fk_government_services_owner foreign key (owning_entity_id)
        references government_entities (id),
    constraint fk_government_services_verified_by foreign key (verified_by) references users (id),
    constraint fk_government_services_created_by foreign key (created_by) references users (id),
    constraint fk_government_services_updated_by foreign key (updated_by) references users (id),
    constraint uq_government_services_slug unique (slug),
    constraint ck_government_services_slug check (
        slug = lower(slug) and slug not like '%/%'
    ),
    constraint ck_government_services_lifecycle check (
        lifecycle_status in ('DRAFT', 'PUBLISHED', 'ARCHIVED')
    ),
    constraint ck_government_services_verification_status check (
        verification_status in ('UNVERIFIED', 'VERIFIED', 'REJECTED')
    ),
    constraint ck_government_services_source_type check (
        provenance_source_type is null
        or provenance_source_type in (
            'OFFICIAL_MANUAL_ENTRY', 'OFFICIAL_SOURCE_REFERENCE', 'APPROVED_IMPORT'
        )
    ),
    constraint ck_government_services_verification_fields check (
        (
            verification_status = 'VERIFIED'
            and lifecycle_status = 'PUBLISHED'
            and provenance_source_type is not null
            and provenance_source_reference is not null
            and char_length(trim(provenance_source_reference)) > 0
            and verified_at is not null
            and verified_by is not null
        )
        or (
            verification_status in ('UNVERIFIED', 'REJECTED')
            and provenance_source_type is null
            and provenance_source_reference is null
            and verified_at is null
            and verified_by is null
        )
    )
);

create index idx_government_services_public
    on government_services (lifecycle_status, verification_status, owning_entity_id);
create index idx_government_services_owner_name
    on government_services (owning_entity_id, official_name_ar, id);

create table government_service_detail_items (
    id uuid primary key,
    government_service_id uuid not null,
    section_type varchar(20) not null,
    display_order integer not null,
    title_ar varchar(500) not null,
    description_ar text,
    constraint fk_service_detail_item_service foreign key (government_service_id)
        references government_services (id) on delete cascade,
    constraint uq_service_detail_item_order unique (
        government_service_id, section_type, display_order
    ),
    constraint ck_service_detail_item_section check (
        section_type in ('ELIGIBILITY', 'REQUIREMENT', 'STEP')
    ),
    constraint ck_service_detail_item_order check (display_order > 0)
);

create index idx_service_detail_item_lookup
    on government_service_detail_items (government_service_id, section_type, display_order);

create table government_service_channels (
    id uuid primary key,
    government_service_id uuid not null,
    channel_type varchar(20) not null,
    display_order integer not null,
    label_ar varchar(255),
    action_url varchar(1000),
    instructions_ar varchar(2000),
    constraint fk_service_channel_service foreign key (government_service_id)
        references government_services (id) on delete cascade,
    constraint uq_service_channel_order unique (government_service_id, display_order),
    constraint ck_service_channel_type check (
        channel_type in ('ONLINE', 'IN_PERSON', 'PHONE')
    ),
    constraint ck_service_channel_order check (display_order > 0)
);

create index idx_service_channel_lookup
    on government_service_channels (government_service_id, display_order);
