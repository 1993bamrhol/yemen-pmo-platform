alter table content_items add column editorial_verification_status varchar(20) not null default 'UNVERIFIED';
alter table content_items add column editorial_verified_revision_id uuid;
alter table content_items add column provenance_source_type varchar(40);
alter table content_items add column provenance_source_reference varchar(1000);
alter table content_items add column editorial_verified_at timestamp with time zone;
alter table content_items add column editorial_verified_by bigint;

alter table content_items add constraint fk_content_items_editorial_verified_revision
    foreign key (editorial_verified_revision_id) references content_revisions (id);
alter table content_items add constraint fk_content_items_editorial_verified_by
    foreign key (editorial_verified_by) references users (id);
alter table content_items add constraint ck_content_items_editorial_verification_status check (
    editorial_verification_status in ('UNVERIFIED', 'VERIFIED', 'REJECTED')
);
alter table content_items add constraint ck_content_items_provenance_source_type check (
    provenance_source_type is null
    or provenance_source_type in (
        'OFFICIAL_MANUAL_ENTRY', 'OFFICIAL_SOURCE_REFERENCE', 'APPROVED_IMPORT'
    )
);
alter table content_items add constraint ck_content_items_editorial_verification_fields check (
    (
        editorial_verification_status = 'VERIFIED'
        and editorial_verified_revision_id is not null
        and editorial_verified_revision_id = published_revision_id
        and provenance_source_type is not null
        and provenance_source_reference is not null
        and char_length(trim(provenance_source_reference)) > 0
        and editorial_verified_at is not null
        and editorial_verified_by is not null
    )
    or (
        editorial_verification_status in ('UNVERIFIED', 'REJECTED')
        and editorial_verified_revision_id is null
        and provenance_source_type is null
        and provenance_source_reference is null
        and editorial_verified_at is null
        and editorial_verified_by is null
    )
);

create index idx_content_items_public_verified
    on content_items (editorial_verification_status, content_type, last_published_at);
