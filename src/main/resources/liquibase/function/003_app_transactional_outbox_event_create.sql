CREATE OR REPLACE FUNCTION app_transactional_outbox_event_create(_topic TEXT,
                                                                 _event TEXT,
                                                                 _key TEXT DEFAULT NULL)
    RETURNS VOID AS
$$
BEGIN
    INSERT INTO app_transactional_outbox_event
        (topic, event, key)
    VALUES (_topic, _event, _key);
END;
$$ LANGUAGE plpgsql;
