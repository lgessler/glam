// user
CREATE CONSTRAINT user_unique_uuid ON (u:User) ASSERT u.uuid IS UNIQUE;
CREATE CONSTRAINT unique_name ON (u:User) ASSERT u.name IS UNIQUE;
CREATE CONSTRAINT unique_email ON (u:User) ASSERT u.email IS UNIQUE;

// project
CREATE CONSTRAINT project_unique_uuid ON (p:Project) ASSERT p.uuid IS UNIQUE;
CREATE CONSTRAINT project_unique_name ON (p:Project) ASSERT p.name IS UNIQUE;

//document
CREATE CONSTRAINT document_unique_uuid ON (d:Document) ASSERT d.uuid IS UNIQUE;
