create table if not exists PICTURES (
    ID integer identity primary key,
    FILENAME varchar(1000) not null,
    HTML_FILENAME varchar(1000),
    STATE varchar(20) not null
);

create unique index if not exists PICTURE_IDX on PICTURES(FILENAME);
