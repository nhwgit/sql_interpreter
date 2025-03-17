# sql interpreter
메모장 기반 SQL 인터프리터를 개발했습니다. CREATE, ALTER, DROP, INSERT, UPDATE, DELETE, SELECT, FROM, WHERE(JOIN 포함), ORDER BY 등 핵심 SQL 명령어를 직접 구현했으며, NOT NULL, PRIMARY KEY, FOREIGN KEY 등의 제약 조건도 구현 했습니다.

입력 예시

create table City( 
    id number primary key, 
    name varchar(15) not null 
);

create table NewBook( 
    id number primary key, 
    name varchar(15) not null, 
    cid number foreign key references City on delete set null 
    on update cascade 
);

ALTER TABLE City 
    ADD COLUMN test INTEGER;

ALTER TABLE City 
    RENAME COLUMN test2 TO test3;

INSERT INTO City 
    VALUES (4, '서울') 
    VALUES (5, '부산') 
    VALUES (6, '인천');

INSERT INTO NewBook
    VALUES (1, '책1', 6) 
    VALUES (2, '책2', 1);

ALTER TABLE City 
    DROP COLUMN test;

UPDATE City 
    SET id = 7 
    WHERE name = '인천';

DELETE City 
    WHERE name = '인천';

SELECT id, name FROM City, newBook 
    WHERE id >= 4 and id < 8 
    and id < 8 ORDER BY name DESC;

SELECT id, name FROM city WHERE name LIKE '부산';

SELECT id, name FROM City, NewBook;

SELECT id, name FROM City, NewBook 
    WHERE City.id = NewBook.cid and City.id >= 7 
    ORDER BY City.name;
