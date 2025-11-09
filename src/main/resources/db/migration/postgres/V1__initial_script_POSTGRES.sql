create table if not exists DOCUMENTS (
    ID integer generated always as identity primary key,
    URL varchar(1000) not null,
    CONTENT text not null,
    DOWNLOAD_STARTED_AT timestamp not null,
    DOWNLOAD_STOPPED_AT timestamp not null,
    ANALYSIS_STARTED_AT timestamp,
    ANALYSIS_STOPPED_AT timestamp,
    TITLE varchar(1000),
    CREATED_AT timestamp,
    STATE varchar(20) not null
);

create table if not exists AUTHORS (
    ID integer generated always as identity primary key,
    NAME varchar(100) not null
);

create table if not exists DOCUMENTS_TO_AUTHORS (
    ID integer generated always as identity primary key,
    DOCUMENT_ID integer not null,
    AUTHOR_ID integer not null,
    constraint FK_DTA_DOCUMENT_ID foreign key (DOCUMENT_ID) references DOCUMENTS(ID),
    constraint FK_DTA_AUTHOR_ID foreign key (AUTHOR_ID) references AUTHORS(ID)
);

create unique index if not exists DOCUMENTS_TO_AUTHORS_IDX on DOCUMENTS_TO_AUTHORS(DOCUMENT_ID, AUTHOR_ID);

create table if not exists CATEGORIES (
    ID integer generated always as identity primary key,
    NAME varchar(100) not null
);

create table if not exists DOCUMENTS_TO_CATEGORIES (
    ID integer generated always as identity primary key,
    DOCUMENT_ID integer not null,
    CATEGORY_ID integer not null,
    constraint FK_DTC_DOCUMENT_ID foreign key (DOCUMENT_ID) references DOCUMENTS(ID),
    constraint FK_DTC_CATEGORY_ID foreign key (CATEGORY_ID) references CATEGORIES(ID)
);

create unique index if not exists DOCUMENTS_TO_CATEGORIES_IDX on DOCUMENTS_TO_CATEGORIES(DOCUMENT_ID, CATEGORY_ID);

create table if not exists LINKS (
    ID integer generated always as identity primary key,
    URL varchar(1000) not null,
    STATE varchar(20) not null,
    SKIPPED_AT timestamp,
    DOWNLOADED_AT timestamp
);

create unique index if not exists LINKS_IDX on LINKS(URL);

create table if not exists DOCUMENTS_TO_LINKS (
    ID integer generated always as identity primary key,
    DOCUMENT_ID integer not null,
    LINK_ID integer not null,
    constraint FK_DTL_DOCUMENT_ID foreign key (DOCUMENT_ID) references DOCUMENTS(ID),
    constraint FK_DTL_LINK_ID foreign key (LINK_ID) references LINKS(ID)
);

create unique index if not exists DOCUMENTS_TO_LINKS_IDX on DOCUMENTS_TO_LINKS(DOCUMENT_ID, LINK_ID);

create table if not exists IMAGES (
    ID integer generated always as identity primary key,
    URL varchar(1000) not null,
    STATE varchar(20) not null,
    FILENAME varchar(1000),
    SIZE integer,
    WIDTH integer,
    HEIGHT integer,
    HASH_VALUE varchar(32),
    DOWNLOAD_STARTED_AT timestamp,
    DOWNLOAD_FINISHED_AT timestamp,
    SKIPPED_AT timestamp
);

create table if not exists DOCUMENTS_TO_IMAGES (
    ID integer generated always as identity primary key,
    DOCUMENT_ID integer not null,
    IMAGE_ID integer not null,
    constraint FK_DTI_DOCUMENT_ID foreign key (DOCUMENT_ID) references DOCUMENTS(ID),
    constraint FK_DTI_IMAGE_ID foreign key (IMAGE_ID) references IMAGES(ID)
);

create unique index if not exists DOCUMENTS_TO_IMAGES_IDX on DOCUMENTS_TO_IMAGES(DOCUMENT_ID, IMAGE_ID);
