CREATE OR REPLACE FUNCTION app_user_create(_email TEXT,
                                           _created_by TEXT)
    RETURNS app_user AS
$$
DECLARE
    _user app_user;
BEGIN
    INSERT INTO app_user (email)
    VALUES (_email)
    RETURNING * INTO _user;

    INSERT INTO app_user_status_change_outbox
        (user_id, status, modified_by)
    VALUES (_user.id, _user.status, _created_by);

    RETURN _user;
END;
$$ LANGUAGE plpgsql;
