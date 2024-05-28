
DELETE FROM CUSTOMER_ADDRESS;
DELETE FROM ADDRESS;
DELETE FROM CUSTOMER;
DELETE FROM DEVICE;

INSERT INTO CUSTOMER
    (ID, LA_NAME)
VALUES
    (1, 'alpha'),
    (2, 'beta'),
    (5, 'gama'),
    (6, 'x man'),
    (9, 'teta');

INSERT INTO ADDRESS
    (ID, NU_NUMBER, LA_STREET, LA_POSTAL_CODE, LA_CITY, LA_COUNTRY)
VALUES
    (1, 42, 'Rte de Luxembourg', 4590, 'Bascharage', 'Luxembourg'),
    (2, 39, 'Bd des Quatre Journées', 1210, 'Saint-Josse-ten-Noode', 'Belgium'),
    (5, 3499, 'Rte de Saint-Martial', 82000, 'Montauban', 'France'),
    (9, 1, 'Bleekweg', '5611 EZ', 'Eindhoven', 'Netherlands'),
    (0, 0, 'xxx', 'xxx', 'xxx', 'xxx');

INSERT INTO CUSTOMER_ADDRESS
    (ID_CUSTOMER, ID_ADDRESS)
VALUES
    (1, 9),
    (2, 2),
    (5, 1),
    (6, 1),
    (9, 5);

INSERT INTO DEVICE
    (ID, LA_NAME, LA_DESCRIPTION)
VALUES
    (1, 'mobile phone', 'A handy'),
    (2, 'TV', 'Television'),
    (3, 'Bicycle', 'Like a motor ... but a light'),
    (4, 'Table', 'just a simple table.');
