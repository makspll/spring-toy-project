CREATE TABLE if NOT EXISTS task (
    id SERIAL PRIMARY KEY,
    accountId integer NOT NULL,
    script text NOT NULL,
    executionTime timestamp without time zone NOT NULL,
    workerStatus varchar(15) NOT NULL,
    statusCode integer
);
