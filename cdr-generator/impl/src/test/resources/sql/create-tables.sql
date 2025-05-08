CREATE TABLE subscribers (
    id INT PRIMARY KEY,
    phone_number VARCHAR(10) NOT NULL
);

CREATE TABLE calls (
    id INT PRIMARY KEY,
    subscriber_1_id INT NOT NULL,
    subscriber_2_id INT NOT NULL,
    start TIMESTAMP NOT NULL,
    end TIMESTAMP NOT NULL,
    FOREIGN KEY (subscriber_1_id) REFERENCES subscribers(id),
    FOREIGN KEY (subscriber_2_id) REFERENCES subscribers(id)
);
