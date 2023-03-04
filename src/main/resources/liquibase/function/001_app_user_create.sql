CREATE OR REPLACE FUNCTION app_user_create(_email TEXT)
    RETURNS app_user AS
$$
DECLARE
    _user app_user;
BEGIN
    INSERT INTO app_user (email)
    VALUES (_email)
    RETURNING * INTO _user;

    RETURN _user;
END;
$$ LANGUAGE plpgsql;
