## Phaas

[![Build Status](https://travis-ci.org/TomiTakussaari/phaas.svg?branch=master)](https://travis-ci.org/TomiTakussaari/phaas)
[![Coverage Status](https://coveralls.io/repos/github/TomiTakussaari/phaas/badge.svg?branch=master)](https://coveralls.io/github/TomiTakussaari/phaas?branch=master)
[![Coverity Scan Build Status](https://scan.coverity.com/projects/11237/badge.svg)](https://scan.coverity.com/projects/tomitakussaari-phaas)
[SonarQube](https://sonarqube.com/dashboard?id=com.github.tomitakussaari%3Aphaas)

Small(ish) http api for protecting passwords and other data.
Still under construction!

#### Why
I've often encountered situations where application needs to protect data, such as passwords or stateless authentication tokens.

Usually encryption key has been stored somewhere very near the application itself, meaning that if application is ever compromised, 
chances are that encryption key is compromised too. It also means that when developers change, we should change all our encryption keys too.

Other problem is that password hashing & verification are quite resource intensive, often eating up all CPU resources for hundreds of milliseconds, preventing application from doing anything else.

Phaas is study/attempt to help with those issues by creating small http api that helps to protect such data. 

##### What

- Protects data by encrypting and signing, it with secret key, and turning it to JWT-compatible token
- Protects passwords by first calculating hash from it, then encrypting the hash with secret key. Given hash can then be verified against plain text password 
- Secret key is stored in encrypted format in phaas user database
- Client password (sent by via HTTP/BasicAuth) are used to decrypt secret key per request
- Attacker needs to compromise both Phaas user database and client app database, to compromise passwords or tokens, adding another layer of security.
- User database can be initialized by environment variable, making it immutable, or by connecting to any database that supports JDBC

```properties

# example of environment configuration that will initialize phaas user database with user "testing-user", and makes user database immutable

db.users.content = [{"userDTO":{"id":null,"userName":"testing-user","passwordHash":"$2a$10$8jH2j2uTf5AEXanJhiFvu.6sS.IDUB25AOqGIeYBwrOlFqe8XAHJm","roles":"ROLE_USER","sharedSecretForSigningCommunication":"secret"},"userConfigurationDTOs":[{"id":null,"user":"testing-user","dataProtectionKey":"da5385256044cfa6.b6c628d3bc92b404c4f7735e52cf80dbd6ad88fbece0a42f564e13d75607d922fd2231d9f309428430dffb1fdb00cf52cd5b080811d866757676509dba50ca77","active":true,"algorithm":"SHA256_BCRYPT"}]}]
immutable.users.db = true

```
 
#### Demo & Swagger documentation
https://mysterious-island-45294.herokuapp.com

Basic Auth: testing-user / my-password



