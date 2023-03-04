CREATE OR REPLACE FUNCTION app_user_update_status(_id BIGINT, _status TEXT)
    RETURNS app_user AS
$$
DECLARE
    _result app_user;
BEGIN
    UPDATE app_user
    SET status = _status
    WHERE id = _id
    RETURNING * INTO _result;

    IF NOT FOUND THEN
        RAISE 'code=USER_NOT_FOUND, message=User with id  % not found', _id;
    END IF;

    RETURN _result;
END;
$$ LANGUAGE plpgsql;
