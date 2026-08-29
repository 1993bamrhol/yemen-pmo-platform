alter table government_entities add column official_name_en varchar(255);
alter table government_entities add column mandate text;
alter table government_entities add column official_email varchar(320);
alter table government_entities add column official_phone varchar(80);
alter table government_entities add column official_address_ar varchar(1000);
alter table government_entities add column official_source_reference varchar(1000);

create table government_entity_slug_aliases (
    id uuid primary key,
    government_entity_id uuid not null,
    public_path_segment varchar(80) not null,
    slug varchar(160) not null,
    created_at timestamp with time zone not null,
    created_by bigint,
    constraint fk_entity_slug_alias_entity foreign key (government_entity_id)
        references government_entities (id) on delete cascade,
    constraint fk_entity_slug_alias_created_by foreign key (created_by) references users (id),
    constraint uq_entity_slug_alias_locator unique (public_path_segment, slug),
    constraint ck_entity_slug_alias_segment check (
        public_path_segment = lower(public_path_segment)
        and public_path_segment not like '%/%'
    ),
    constraint ck_entity_slug_alias_slug check (
        slug = lower(slug)
        and slug not like '%/%'
    )
);

create index idx_entity_slug_alias_entity
    on government_entity_slug_aliases (government_entity_id);
