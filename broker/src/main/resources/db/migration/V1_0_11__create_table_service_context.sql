DROP PROCEDURE IF EXISTS create_table_service_context;
DELIMITER //

CREATE PROCEDURE create_table_service_context()
  BEGIN

    CREATE TABLE service_context
    (
      id                  BIGINT                      NOT NULL AUTO_INCREMENT PRIMARY KEY,
      `_key`              VARCHAR(255)                NOT NULL,
      `_value`            VARCHAR(255) DEFAULT 'NULL' NULL,
      service_instance_id BIGINT                      NOT NULL,
      CONSTRAINT FK_CONTEXT_SERVICE_INSTANCE FOREIGN KEY (service_instance_id) REFERENCES service_instance (id)
    );

    CREATE INDEX FK_CONTEXT_SERVICE_INSTANCE
      ON service_context (service_instance_id);

    ALTER TABLE service_context
      ADD CONSTRAINT service_context__key__value_service_instance_id_pk UNIQUE (`_key`, `_value`, service_instance_id);

  END//

DELIMITER ;
CALL create_table_service_context();

/*** MIGRATE CLOUD FOUNDRY CONTEXT SCRIPT ***/
DROP PROCEDURE IF EXISTS migrate_cf_context;
DELIMITER //
CREATE PROCEDURE migrate_cf_context()
  BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE service_instance_id INT;
    DECLARE org_id, space_id VARCHAR(255);
    DECLARE cur1 CURSOR FOR SELECT
                              si.id,
                              si.org,
                              si.space
                            FROM service_instance si
                            WHERE si.org IS NOT NULL AND si.id NOT IN (SELECT sc.service_instance_id
                                                                       FROM service_context sc);
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    OPEN cur1;

    read_loop: LOOP
      FETCH cur1
      INTO service_instance_id, org_id, space_id;

      IF done
      THEN
        LEAVE read_loop;
      END IF;


      INSERT INTO service_context (`_key`, `_value`, service_instance_id)
      VALUES ('platform', 'cloudfoundry', service_instance_id);
      INSERT INTO service_context (`_key`, `_value`, service_instance_id)
      VALUES ('organization_guid', org_id, service_instance_id);
      INSERT INTO service_context (`_key`, `_value`, service_instance_id)
      VALUES ('space_guid', space_id, service_instance_id);

    END LOOP;

    COMMIT;
    CLOSE cur1;

  END//

DELIMITER ;
CALL migrate_cf_context();