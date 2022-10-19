CREATE OR REPLACE FUNCTION app_user_update_status(_id BIGINT,
                                                  _status TEXT,
                                                  _modified_by TEXT)
    RETURNS app_user AS
$$
DECLARE
    _result app_user;
BEGIN
    PERFORM * FROM app_user WHERE id = _id FOR UPDATE;

    IF NOT FOUND THEN
        RAISE 'code=USER_NOT_FOUND, message=User with id  % not found', _id;
    END IF;

    UPDATE app_user SET status = _status WHERE id = _id RETURNING * INTO _result;

    INSERT INTO app_user_status_change_outbox
        (user_id, status, modified_by)
    VALUES (_id, _status, _modified_by);

    RETURN _result;
END;
$$ LANGUAGE plpgsql;
