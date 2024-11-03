create table if not exists DOCUMENTS (
    id integer identity primary key,
    url varchar(1000) not null,
    content varchar(1000000) not null,
    downloaded_at timestamp not null,
    created_at timestamp
);

create table if not exists AUTHORS (
    id integer identity primary key,
    name varchar(100) not null
);

create table if not exists DOCUMENTS_TO_AUTHORS (
    id integer identity primary key,
    document_id integer not null,
    author_id integer not null,
    constraint fk_dta_document_id foreign key (document_id) references DOCUMENTS(id),
    constraint fk_dta_author_id foreign key (author_id) references AUTHORS(id)
);

create unique index if not exists DOCUMENTS_TO_AUTHORS_IDX on DOCUMENTS_TO_AUTHORS(document_id, author_id);

create table if not exists CATEGORIES (
    id integer identity primary key,
    name varchar(100) not null
);

create table if not exists DOCUMENTS_TO_CATEGORIES (
    id integer identity primary key,
    document_id integer not null,
    category_id integer not null,
    constraint fk_dtc_document_id foreign key (document_id) references DOCUMENTS(id),
    constraint fk_dtc_category_id foreign key (category_id) references CATEGORIES(id)
);

create unique index if not exists DOCUMENTS_TO_CATEGORIES_IDX on DOCUMENTS_TO_CATEGORIES(document_id, category_id);

create table if not exists LINKS (
    id integer identity primary key,
    url varchar(1000) not null,
    skip boolean default false not null,
    downloaded boolean default false not null
);

create unique index if not exists LINKS_IDX on LINKS(url);

create table if not exists DOCUMENTS_TO_LINKS (
    id integer identity primary key,
    document_id integer not null,
    link_id integer not null,
    constraint fk_dtl_document_id foreign key (document_id) references DOCUMENTS(id),
    constraint fk_dtl_link_id foreign key (link_id) references  LINKS(id)
);

create unique index if not exists DOCUMENTS_TO_LINKS_IDX on DOCUMENTS_TO_LINKS(document_id, link_id);

create table if not exists IMAGES (
    id integer identity primary key,
    url varchar(1000) not null,
    filename varchar(1000),
    size integer,
    width integer,
    height integer,
    skip boolean default false not null,
    downloaded boolean default false not null
);

create table if not exists DOCUMENTS_TO_IMAGES (
    id integer identity primary key,
    document_id integer not null,
    image_id integer not null,
    constraint fk_dti_document_id foreign key (document_id) references DOCUMENTS(id),
    constraint fk_dti_image_id foreign key (image_id) references IMAGES(id)
);

create unique index if not exists DOCUMENTS_TO_IMAGES_IDX on DOCUMENTS_TO_IMAGES(document_id, image_id);
