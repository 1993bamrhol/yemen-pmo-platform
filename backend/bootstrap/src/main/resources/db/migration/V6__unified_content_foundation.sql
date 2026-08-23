create table content_items (
    id uuid primary key,
    content_type varchar(40) not null,
    primary_entity_id uuid not null,
    slug varchar(180) not null,
    locale varchar(12) not null default 'ar',
    status varchar(30) not null default 'DRAFT',
    current_revision_id uuid,
    published_revision_id uuid,
    display_metadata text,
    first_published_at timestamp with time zone,
    last_published_at timestamp with time zone,
    archived_at timestamp with time zone,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    created_by bigint,
    updated_by bigint,
    version bigint not null default 0,
    constraint fk_content_items_primary_entity foreign key (primary_entity_id)
        references government_entities (id),
    constraint fk_content_items_created_by foreign key (created_by) references users (id),
    constraint fk_content_items_updated_by foreign key (updated_by) references users (id),
    constraint uq_content_items_canonical_slug unique (content_type, locale, slug),
    constraint ck_content_items_type check (
        content_type in ('NEWS', 'ANNOUNCEMENT', 'DECISION', 'DOCUMENT')
    ),
    constraint ck_content_items_status check (
        status in ('DRAFT', 'IN_REVIEW', 'APPROVED', 'PUBLISHED', 'ARCHIVED')
    ),
    constraint ck_content_items_published_revision check (
        status <> 'PUBLISHED' or published_revision_id is not null
    ),
    constraint ck_content_items_publication_dates check (
        last_published_at is null
        or first_published_at is null
        or last_published_at >= first_published_at
    ),
    constraint ck_content_items_version check (version >= 0)
);

create index idx_content_items_entity_status
    on content_items (primary_entity_id, status);
create index idx_content_items_type_status_published
    on content_items (content_type, status, last_published_at);

create table content_revisions (
    id uuid primary key,
    content_item_id uuid not null,
    revision_number integer not null,
    title varchar(255) not null,
    summary varchar(2000),
    body text not null,
    byline varchar(255),
    change_note varchar(1000),
    created_at timestamp with time zone not null,
    created_by bigint,
    constraint fk_content_revisions_item foreign key (content_item_id)
        references content_items (id) on delete cascade,
    constraint fk_content_revisions_created_by foreign key (created_by) references users (id),
    constraint uq_content_revisions_number unique (content_item_id, revision_number),
    constraint ck_content_revisions_number check (revision_number > 0)
);

create index idx_content_revisions_item_created
    on content_revisions (content_item_id, created_at);

alter table content_items add constraint fk_content_items_current_revision
    foreign key (current_revision_id) references content_revisions (id) on delete set null;
alter table content_items add constraint fk_content_items_published_revision
    foreign key (published_revision_id) references content_revisions (id) on delete set null;

create table content_entity_links (
    content_item_id uuid not null,
    government_entity_id uuid not null,
    link_role varchar(30) not null,
    created_at timestamp with time zone not null,
    created_by bigint,
    primary key (content_item_id, government_entity_id, link_role),
    constraint fk_content_entity_links_item foreign key (content_item_id)
        references content_items (id) on delete cascade,
    constraint fk_content_entity_links_entity foreign key (government_entity_id)
        references government_entities (id),
    constraint fk_content_entity_links_created_by foreign key (created_by) references users (id),
    constraint ck_content_entity_links_role check (
        link_role in ('CO_PUBLISHER', 'SUBJECT', 'RELATED')
    )
);

create index idx_content_entity_links_entity
    on content_entity_links (government_entity_id, link_role);

create table taxonomy_terms (
    id uuid primary key,
    taxonomy_code varchar(50) not null,
    slug varchar(120) not null,
    label_ar varchar(180) not null,
    parent_term_id uuid,
    active boolean not null default true,
    created_at timestamp with time zone not null,
    constraint fk_taxonomy_terms_parent foreign key (parent_term_id)
        references taxonomy_terms (id),
    constraint uq_taxonomy_terms_slug unique (taxonomy_code, slug),
    constraint ck_taxonomy_terms_code check (taxonomy_code in ('CONTENT_CATEGORY', 'TOPIC')),
    constraint ck_taxonomy_terms_not_own_parent check (parent_term_id is null or parent_term_id <> id)
);

create index idx_taxonomy_terms_parent on taxonomy_terms (parent_term_id);

create table content_taxonomy_assignments (
    content_item_id uuid not null,
    taxonomy_term_id uuid not null,
    created_at timestamp with time zone not null,
    created_by bigint,
    primary key (content_item_id, taxonomy_term_id),
    constraint fk_content_taxonomy_item foreign key (content_item_id)
        references content_items (id) on delete cascade,
    constraint fk_content_taxonomy_term foreign key (taxonomy_term_id)
        references taxonomy_terms (id),
    constraint fk_content_taxonomy_created_by foreign key (created_by) references users (id)
);

create table content_attachments (
    id uuid primary key,
    content_item_id uuid not null,
    revision_id uuid,
    storage_provider varchar(60),
    storage_key varchar(500),
    external_url varchar(1000),
    original_filename varchar(255) not null,
    mime_type varchar(150) not null,
    size_bytes bigint,
    sha256 varchar(64),
    accessible_label_ar varchar(255) not null,
    sort_order integer not null default 0,
    public_visible boolean not null default false,
    created_at timestamp with time zone not null,
    created_by bigint,
    constraint fk_content_attachments_item foreign key (content_item_id)
        references content_items (id) on delete cascade,
    constraint fk_content_attachments_revision foreign key (revision_id)
        references content_revisions (id) on delete set null,
    constraint fk_content_attachments_created_by foreign key (created_by) references users (id),
    constraint ck_content_attachments_location check (
        storage_key is not null or external_url is not null
    ),
    constraint ck_content_attachments_size check (size_bytes is null or size_bytes >= 0),
    constraint ck_content_attachments_sort check (sort_order >= 0)
);

create index idx_content_attachments_item
    on content_attachments (content_item_id, sort_order);

create table decision_details (
    content_item_id uuid primary key,
    decision_number varchar(120),
    issued_on date,
    effective_on date,
    legal_status varchar(40),
    constraint fk_decision_details_item foreign key (content_item_id)
        references content_items (id) on delete cascade,
    constraint ck_decision_details_dates check (
        effective_on is null or issued_on is null or effective_on >= issued_on
    ),
    constraint ck_decision_details_legal_status check (
        legal_status is null or legal_status in ('ACTIVE', 'AMENDED', 'REPEALED', 'EXPIRED')
    )
);

create table document_details (
    content_item_id uuid primary key,
    document_number varchar(120),
    document_date date,
    primary_attachment_id uuid,
    constraint fk_document_details_item foreign key (content_item_id)
        references content_items (id) on delete cascade,
    constraint fk_document_details_attachment foreign key (primary_attachment_id)
        references content_attachments (id) on delete set null
);

create table content_slug_redirects (
    id uuid primary key,
    content_type varchar(40) not null,
    locale varchar(12) not null,
    old_slug varchar(180) not null,
    content_item_id uuid not null,
    created_at timestamp with time zone not null,
    created_by bigint,
    constraint fk_content_slug_redirects_item foreign key (content_item_id)
        references content_items (id) on delete cascade,
    constraint fk_content_slug_redirects_created_by foreign key (created_by) references users (id),
    constraint uq_content_slug_redirects_old_slug unique (content_type, locale, old_slug),
    constraint ck_content_slug_redirects_type check (
        content_type in ('NEWS', 'ANNOUNCEMENT', 'DECISION', 'DOCUMENT')
    )
);

create table legacy_content_mappings (
    id uuid primary key,
    source_system varchar(60) not null,
    source_type varchar(50) not null,
    legacy_id bigint not null,
    content_item_id uuid not null,
    created_at timestamp with time zone not null,
    constraint fk_legacy_content_mappings_item foreign key (content_item_id)
        references content_items (id) on delete cascade,
    constraint uq_legacy_content_source unique (source_system, source_type, legacy_id)
);

create index idx_legacy_content_item on legacy_content_mappings (content_item_id);
create sequence legacy_admin_content_id_seq start with 1000000 increment by 1;

create table content_transitions (
    id uuid primary key,
    content_item_id uuid not null,
    revision_id uuid,
    from_status varchar(30) not null,
    to_status varchar(30) not null,
    action varchar(40) not null,
    actor_user_id bigint,
    government_entity_id uuid not null,
    comment_text varchar(2000),
    correlation_id varchar(100),
    occurred_at timestamp with time zone not null,
    constraint fk_content_transitions_item foreign key (content_item_id)
        references content_items (id) on delete cascade,
    constraint fk_content_transitions_revision foreign key (revision_id)
        references content_revisions (id) on delete set null,
    constraint fk_content_transitions_actor foreign key (actor_user_id) references users (id),
    constraint fk_content_transitions_entity foreign key (government_entity_id)
        references government_entities (id),
    constraint ck_content_transitions_from_status check (
        from_status in ('DRAFT', 'IN_REVIEW', 'APPROVED', 'PUBLISHED', 'ARCHIVED')
    ),
    constraint ck_content_transitions_to_status check (
        to_status in ('DRAFT', 'IN_REVIEW', 'APPROVED', 'PUBLISHED', 'ARCHIVED')
    ),
    constraint ck_content_transitions_action check (
        action in ('SUBMIT_REVIEW', 'REQUEST_CHANGES', 'APPROVE', 'PUBLISH', 'ARCHIVE', 'RESTORE')
    ),
    constraint ck_content_transitions_changed check (from_status <> to_status)
);

create index idx_content_transitions_item_time
    on content_transitions (content_item_id, occurred_at);
create index idx_content_transitions_entity_time
    on content_transitions (government_entity_id, occurred_at);
