CREATE TABLE book_tbl(
   book_id INT NOT NULL AUTO_INCREMENT,
   book_title VARCHAR(100) NOT NULL,
   book_author VARCHAR(40) NOT NULL,
   book_submission_date DATE,
   PRIMARY KEY  (book_id)
);
