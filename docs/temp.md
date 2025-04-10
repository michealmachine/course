create table categories
(
    id          bigint auto_increment
        primary key,
    created_at  datetime(6)  not null,
    updated_at  datetime(6)  not null,
    code        varchar(50)  not null,
    description varchar(500) null,
    enabled     bit          null,
    icon        varchar(255) null,
    level       int          null,
    name        varchar(100) not null,
    order_index int          null,
    parent_id   bigint       null,
    constraint UKiwylx6fb2dqdw8kfc31vaiiyp
        unique (code),
    constraint FKsaok720gsu4u2wrgbk10b5n8d
        foreign key (parent_id) references categories (id)
);

create table institution_applications
(
    id             bigint auto_increment
        primary key,
    created_at     datetime(6)  not null,
    updated_at     datetime(6)  not null,
    address        varchar(255) null,
    application_id varchar(20)  null,
    contact_email  varchar(255) null,
    contact_person varchar(50)  null,
    contact_phone  varchar(20)  null,
    description    varchar(500) null,
    institution_id bigint       null,
    logo           varchar(255) null,
    name           varchar(100) not null,
    review_comment varchar(500) null,
    reviewed_at    datetime(6)  null,
    reviewer_id    bigint       null,
    status         int          null,
    constraint UKfmio3in8eu0ti4aqj7e2xcly7
        unique (application_id)
);

create table institutions
(
    id             bigint auto_increment
        primary key,
    created_at     datetime(6)  not null,
    updated_at     datetime(6)  not null,
    address        varchar(255) null,
    contact_email  varchar(255) null,
    contact_person varchar(50)  null,
    contact_phone  varchar(20)  null,
    description    varchar(500) null,
    logo           varchar(255) null,
    name           varchar(100) not null,
    register_code  varchar(20)  null,
    status         int          null,
    constraint UK2ln76jm0oqtdqa3cl1eke25x9
        unique (register_code)
);

create table courses
(
    id                   bigint auto_increment
        primary key,
    created_at           datetime(6)    not null,
    updated_at           datetime(6)    not null,
    average_rating       float          null,
    cover_image          varchar(255)   null,
    creator_id           bigint         null,
    data_version         int            null,
    description          varchar(2000)  null,
    difficulty           int            null,
    discount_price       decimal(10, 2) null,
    is_published_version bit            null,
    learning_objectives  varchar(1000)  null,
    payment_type         int            null,
    price                decimal(10, 2) null,
    published_version_id bigint         null,
    rating_count         int            null,
    review_comment       varchar(1000)  null,
    review_status        int            null,
    reviewed_at          datetime(6)    null,
    reviewer_id          bigint         null,
    status               int            null,
    student_count        int            null,
    target_audience      varchar(1000)  null,
    title                varchar(200)   not null,
    total_duration       int            null,
    total_lessons        int            null,
    version              int            null,
    version_type         int            null,
    category_id          bigint         null,
    institution_id       bigint         not null,
    constraint FK72l5dj585nq7i6xxv1vj51lyn
        foreign key (category_id) references categories (id),
    constraint FK9j9pt3rv7axxvf4l2svqpwus3
        foreign key (institution_id) references institutions (id)
);

create table chapters
(
    id                bigint auto_increment
        primary key,
    created_at        datetime(6)   not null,
    updated_at        datetime(6)   not null,
    access_type       int           null,
    description       varchar(1000) null,
    estimated_minutes int           null,
    order_index       int           null,
    title             varchar(200)  not null,
    course_id         bigint        not null,
    constraint FK6h1m0nrtdwj37570c0sp2tdcs
        foreign key (course_id) references courses (id)
);

create table course_reviews
(
    id           bigint auto_increment
        primary key,
    created_at   datetime(6)   not null,
    updated_at   datetime(6)   not null,
    content      varchar(1000) null,
    data_version int           null,
    like_count   int           null,
    rating       int           not null,
    user_id      bigint        not null,
    course_id    bigint        not null,
    constraint FK799g8dfcye3g51ru63bfdhyb1
        foreign key (course_id) references courses (id)
);

create table media
(
    id                bigint auto_increment
        primary key,
    description       varchar(255)                                            null,
    last_access_time  datetime(6)                                             null,
    original_filename varchar(255)                                            null,
    size              bigint                                                  null,
    status            enum ('COMPLETED', 'FAILED', 'PROCESSING', 'UPLOADING') null,
    storage_path      varchar(255)                                            null,
    title             varchar(255)                                            null,
    type              enum ('AUDIO', 'DOCUMENT', 'VIDEO')                     null,
    upload_time       datetime(6)                                             null,
    uploader_id       bigint                                                  null,
    institution_id    bigint                                                  not null,
    constraint FK210yh9xojbh62sl08re1e4t3p
        foreign key (institution_id) references institutions (id)
);

create table permissions
(
    id          bigint auto_increment
        primary key,
    created_at  datetime(6)  not null,
    updated_at  datetime(6)  not null,
    code        varchar(50)  not null,
    description varchar(255) null,
    method      varchar(10)  null,
    name        varchar(50)  not null,
    url         varchar(255) null,
    constraint UK7lcb6glmvwlro3p2w2cewxtvd
        unique (code)
);

create table question_groups
(
    id             bigint auto_increment
        primary key,
    created_at     datetime(6)  not null,
    updated_at     datetime(6)  not null,
    created_time   datetime(6)  null,
    creator_id     bigint       null,
    creator_name   varchar(100) null,
    description    varchar(500) null,
    name           varchar(100) not null,
    updated_time   datetime(6)  null,
    institution_id bigint       not null,
    constraint FK2iravndvd8npjgpfog5uq68ci
        foreign key (institution_id) references institutions (id)
);

create table question_tags
(
    id             bigint auto_increment
        primary key,
    created_at     datetime(6) not null,
    updated_at     datetime(6) not null,
    created_time   datetime(6) null,
    creator_id     bigint      null,
    name           varchar(50) not null,
    updated_time   datetime(6) null,
    institution_id bigint      not null,
    constraint FKirws2b5y0esidxsbuc2apw5r1
        foreign key (institution_id) references institutions (id)
);

create table questions
(
    id             bigint auto_increment
        primary key,
    created_at     datetime(6)   not null,
    updated_at     datetime(6)   not null,
    analysis       varchar(2000) null,
    answer         varchar(2000) null,
    content        varchar(2000) not null,
    created_time   datetime(6)   null,
    creator_id     bigint        null,
    creator_name   varchar(100)  null,
    difficulty     int           not null,
    score          int           not null,
    title          varchar(200)  not null,
    type           int           not null,
    updated_time   datetime(6)   null,
    institution_id bigint        not null,
    constraint FKjy2buwxjtffasvmvtd3e7fhht
        foreign key (institution_id) references institutions (id)
);

create table question_group_items
(
    id          bigint auto_increment
        primary key,
    created_at  datetime(6) not null,
    updated_at  datetime(6) not null,
    difficulty  int         null,
    order_index int         not null,
    score       int         null,
    group_id    bigint      not null,
    question_id bigint      not null,
    constraint UK76mg872l8v3hktweyhw9gvcns
        unique (group_id, question_id),
    constraint FKbt1rmakb22i49kmc6hlk48aic
        foreign key (group_id) references question_groups (id),
    constraint FKcl6d8rja1j6own3g7k29mo4v8
        foreign key (question_id) references questions (id)
);

create table question_options
(
    id          bigint auto_increment
        primary key,
    created_at  datetime(6)   not null,
    updated_at  datetime(6)   not null,
    content     varchar(1000) not null,
    is_correct  bit           not null,
    order_index int           not null,
    question_id bigint        not null,
    constraint FKsb9v00wdrgc9qojtjkv7e1gkp
        foreign key (question_id) references questions (id)
);

create table question_tag_mappings
(
    id          bigint auto_increment
        primary key,
    created_at  datetime(6) not null,
    updated_at  datetime(6) not null,
    question_id bigint      not null,
    tag_id      bigint      not null,
    constraint UKlehro5ik9bfo73a526v43iugt
        unique (question_id, tag_id),
    constraint FK4vtih5gmcio9sye7to8ycj5aj
        foreign key (tag_id) references question_tags (id),
    constraint FKr5km3yf4wb61igmmsdup16y5n
        foreign key (question_id) references questions (id)
);

create table roles
(
    id          bigint auto_increment
        primary key,
    created_at  datetime(6)  not null,
    updated_at  datetime(6)  not null,
    code        varchar(50)  not null,
    description varchar(255) null,
    name        varchar(50)  not null,
    constraint UKch1113horj4qr56f91omojv8
        unique (code),
    constraint UKofx66keruapi6vyqpv6f2or37
        unique (name)
);

create table role_permissions
(
    role_id       bigint not null,
    permission_id bigint not null,
    primary key (role_id, permission_id),
    constraint FKegdk29eiy7mdtefy5c7eirr6e
        foreign key (permission_id) references permissions (id),
    constraint FKn5fotdgk8d1xvo8nav9uv3muc
        foreign key (role_id) references roles (id)
);

create table sections
(
    id                          bigint auto_increment
        primary key,
    created_at                  datetime(6)   not null,
    updated_at                  datetime(6)   not null,
    content_type                varchar(20)   null,
    description                 varchar(1000) null,
    media_resource_type         varchar(20)   null,
    order_by_difficulty         bit           null,
    order_index                 int           null,
    random_order                bit           null,
    resource_type_discriminator varchar(20)   null,
    show_analysis               bit           null,
    title                       varchar(200)  not null,
    chapter_id                  bigint        not null,
    media_id                    bigint        null,
    question_group_id           bigint        null,
    constraint FKh122o2lhxoxwp5rn7a43tkmhp
        foreign key (question_group_id) references question_groups (id),
    constraint FKineufi8sef3eu04xqvpiikrmo
        foreign key (media_id) references media (id),
    constraint FKtosurpbts8twe8loj39roj4c2
        foreign key (chapter_id) references chapters (id)
);

create table storage_quota
(
    id             bigint auto_increment
        primary key,
    created_at     datetime(6)                         null,
    enabled        bit                                 null,
    expires_at     datetime(6)                         null,
    total_quota    bigint                              null,
    type           enum ('DOCUMENT', 'TOTAL', 'VIDEO') null,
    updated_at     datetime(6)                         null,
    used_quota     bigint                              null,
    institution_id bigint                              not null,
    constraint FK50q7itdoluwemgojkq8ji2ry0
        foreign key (institution_id) references institutions (id)
);

create table tags
(
    id          bigint auto_increment
        primary key,
    created_at  datetime(6)  not null,
    updated_at  datetime(6)  not null,
    description varchar(255) null,
    name        varchar(50)  not null,
    use_count   int          null,
    constraint UKt48xdq560gs3gap9g7jg36kgc
        unique (name)
);

create table course_tags
(
    course_id bigint not null,
    tag_id    bigint not null,
    primary key (course_id, tag_id),
    constraint FKjqwlxw962j7q9wdogwnrctc2p
        foreign key (course_id) references courses (id),
    constraint FKle4e0o8293pd96wrrfl77ij42
        foreign key (tag_id) references tags (id)
);

create table users
(
    id             bigint auto_increment
        primary key,
    created_at     datetime(6)  not null,
    updated_at     datetime(6)  not null,
    avatar         varchar(255) null,
    email          varchar(255) null,
    last_login_at  datetime(6)  null,
    name           varchar(50)  null,
    nickname       varchar(50)  null,
    password       varchar(255) not null,
    phone          varchar(255) null,
    status         int          null,
    username       varchar(50)  not null,
    institution_id bigint       null,
    constraint UK6dotkott2kjsp8vw4d0m25fb7
        unique (email),
    constraint UKdu5v5sr43g5bfnji4vb8hg5s3
        unique (phone),
    constraint UKr43af9ap4edm43mmtq01oddj6
        unique (username),
    constraint FK2qqjpih9isqcs22710v8lef9w
        foreign key (institution_id) references institutions (id)
);

create table orders
(
    id                    bigint auto_increment
        primary key,
    created_at            datetime(6)    not null,
    updated_at            datetime(6)    not null,
    amount                decimal(10, 2) not null,
    description           varchar(500)   null,
    order_no              varchar(64)    not null,
    paid_at               datetime(6)    null,
    refund_amount         decimal(10, 2) null,
    refund_reason         varchar(500)   null,
    refund_trade_no       varchar(64)    null,
    refunded_at           datetime(6)    null,
    status                int            not null,
    title                 varchar(200)   not null,
    trade_no              varchar(64)    null,
    course_id             bigint         not null,
    institution_id        bigint         not null,
    user_id               bigint         not null,
    payment_method        int            null,
    refund_transaction_id varchar(64)    null,
    transaction_id        varchar(64)    null,
    constraint UKg8pohnngqi5x1nask7nff2u7w
        unique (order_no),
    constraint FK32ql8ubntj5uh44ph9659tiih
        foreign key (user_id) references users (id),
    constraint FK5msj6k8isqx58wdpi2jqvwgn6
        foreign key (institution_id) references institutions (id),
    constraint FK68snkj0g5gsjxllhjc3v5lm0r
        foreign key (course_id) references courses (id)
);

create table user_courses
(
    id                       bigint auto_increment
        primary key,
    created_at               datetime(6) not null,
    updated_at               datetime(6) not null,
    expire_at                datetime(6) null,
    last_learn_at            datetime(6) null,
    learn_duration           int         null,
    progress                 int         null,
    purchased_at             datetime(6) not null,
    status                   int         null,
    course_id                bigint      not null,
    order_id                 bigint      null,
    user_id                  bigint      not null,
    version                  int         null,
    current_chapter_id       bigint      null,
    current_section_id       bigint      null,
    current_section_progress int         null,
    constraint UK45tnm2hodvjnotdksjkopbb0g
        unique (user_id, course_id),
    constraint UKm4nu8jrvyyccg47rw43p2mx5m
        unique (order_id),
    constraint FK5i2mwg17kvpk92fy6cdii93da
        foreign key (user_id) references users (id),
    constraint FKb84hga2qpwc4vv44lmyb8mwux
        foreign key (course_id) references courses (id),
    constraint FKfqhjop6kqsi4vpk79k5ll25yl
        foreign key (order_id) references orders (id)
);

create table user_favorites
(
    id            bigint auto_increment
        primary key,
    created_at    datetime(6) not null,
    updated_at    datetime(6) not null,
    favorite_time datetime(6) null,
    course_id     bigint      not null,
    user_id       bigint      not null,
    constraint UKk2fc7xakct69ntjclfv74vnw7
        unique (user_id, course_id),
    constraint FK2dpfhhttgf27wrr4t1x6pet6p
        foreign key (course_id) references courses (id),
    constraint FK4sv7b9w9adr0fjnc4u10exlwm
        foreign key (user_id) references users (id)
);

create table user_roles
(
    user_id bigint not null,
    role_id bigint not null,
    primary key (user_id, role_id),
    constraint FKh8ciramu9cc9q3qcqiv4ue8a6
        foreign key (role_id) references roles (id),
    constraint FKhfh9dx7w3ubf1co1vdev94g3f
        foreign key (user_id) references users (id)
);

create table user_wrong_questions
(
    id              bigint auto_increment
        primary key,
    correct_answers text         null,
    created_at      datetime(6)  null,
    question_id     bigint       null,
    question_title  varchar(500) null,
    question_type   varchar(50)  null,
    section_id      bigint       null,
    status          int          null,
    updated_at      datetime(6)  null,
    user_answer     text         null,
    course_id       bigint       null,
    user_id         bigint       null,
    constraint FK9fsnm91jic4kv6oe0shhk3kic
        foreign key (user_id) references users (id),
    constraint FKrwexx9wn23ynjlf0picthi4t4
        foreign key (course_id) references courses (id)
);

