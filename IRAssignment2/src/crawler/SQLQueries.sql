--SQLITE STUFF:
CREATE TABLE Pages (
  PageID INTEGER PRIMARY KEY,
  Url varchar(500) NOT NULL,
  Subdomain varchar(500),
  Content MEDIUMTEXT,
  HtmlContent MEDIUMTEXT,
  nWords int
);

CREATE TABLE PageLinks (
  PageID_From int NOT NULL,
  PageID_To int NOT NULL
);

-- MYSQL STUFF (NOT TO BE USED NOW):
CREATE SCHEMA IRTest

CREATE TABLE IRTest.Pages (
  PageID int NOT NULL AUTO_INCREMENT PRIMARY KEY,
  Url varchar(500) NOT NULL,
  Subdomain varchar(500),
  Content MEDIUMTEXT,
  HtmlContent MEDIUMTEXT,
  nWords int
);

CREATE TABLE IRTest.PageLinks (
  PageID_From int NOT NULL,
  PageID_To int NOT NULL
)

DROP procedure IF exists IRTest.usp_InsertEntryToPages;
DELIMITER $$

CREATE PROCEDURE IRTest.usp_InsertEntryToPages (
  IN Url varchar(500),
  IN Content MEDIUMTEXT,
  IN HtmlContent MEDIUMTEXT,
  IN Subdomain varchar(500),
  IN nWords int
  )
BEGIN
DECLARE CountRows int;

SET CountRows = (SELECT COUNT(1)
FROM IRTest.pages P
WHERE P.Url = Url);

IF CountRows < 1 THEN BEGIN
INSERT INTO Pages (
  Url, Subdomain, Content, HtmlContent, nWords
) VALUES (
  Url, Subdomain, Content, HtmlContent, nWords
);
END;
ELSE BEGIN
UPDATE IRTest.Pages P
SET P.Content = Content, P.HtmlContent = HtmlContent, P.Subdomain = Subdomain, P.nWords = nWords
WHERE P.Url = Url;
END;
END IF;

END$$
DELIMITER ;


DROP procedure IF exists IRTest.usp_InsertEntryToLinks;
DELIMITER $$
CREATE PROCEDURE IRTest.usp_InsertEntryToLinks (
  IN Url_From varchar(500),
  IN Url_To varchar(500)
  )
BEGIN
  DECLARE PageIDFrom int;
  DECLARE PageIDTo int;
  DECLARE CountExistingRows int;
  
  SET PageIDFrom = (SELECT PageID
  FROM IRTest.Pages
  WHERE Url = Url_From);
  
  SET PageIDTo = (SELECT PageID
  FROM IRTest.Pages
  WHERE Url = Url_To);
  
  IF PageIDTo IS NULL THEN BEGIN
      CALL IRTest.usp_InsertEntryToPages (Url_To, NULL, NULL, NULL, NULL);
      SET PageIDTo = (SELECT PageID
      FROM IRTest.Pages
      WHERE Url = Url_To);
  END;
  END IF;
  
  SET CountExistingRows = (SELECT COUNT(1)
  FROM IRTest.PageLinks
  WHERE PageID_From = PageIDFrom AND PageID_To = PageIDTo);
  
  IF CountExistingRows < 1 THEN BEGIN
  INSERT INTO IRTest.PageLinks (
    PageID_From, PageID_To
  ) VALUES (
    PageIDFrom, PageIDTO
  );
  END;
  END IF;
END$$
DELIMITER ;