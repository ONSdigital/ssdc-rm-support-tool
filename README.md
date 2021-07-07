# ssdc-rm-support-tool

## Security
Not seeing the features you want or need? Try adding yourself as a super user, like this:

```sql
insert into casev3.users
values ('8d44dca6-359e-4e84-b55d-2d1903f9e561', -- user ID
        'fred.bloggs@ons.gov.uk');              -- user email

insert into casev3.user_group
values ('8269d75c-bfa1-4930-aca2-10dd9c6a2b42'); -- group ID

insert into casev3.user_group_member
values ('fb5d6366-e009-46f2-b23e-7b6ee6a27f97',  -- member ID
        '8269d75c-bfa1-4930-aca2-10dd9c6a2b42',  -- group ID
        '8d44dca6-359e-4e84-b55d-2d1903f9e561'); -- user ID

insert into casev3.user_group_permission
values ('c469377e-680e-4cb1-92a0-5217be2b3a52', -- permission ID
        'SUPER_USER',                           -- activity
        '8269d75c-bfa1-4930-aca2-10dd9c6a2b42', -- group ID
        null);
```