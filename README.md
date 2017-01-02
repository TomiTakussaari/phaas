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

db.users.content = [{"userDTO":{"id":null,"userName":"testing-user","passwordHash":"$argon2i$v=19$m=65536,t=2,p=2$hVLRWCeJ1VpjJCRTJL0fkQ$nwMi69L05pyVBcDGsflJ+Y4Ett9Z3bQVxUP/YodPLBo","roles":"ROLE_USER","sharedSecretForSigningCommunication":"secret"},"userConfigurationDTOs":[{"id":null,"user":"testing-user","dataProtectionKey":"$2.0f021e62f1f18e08.66e971f100652ac17b560750f527af7057cbacff5449100dcd50627af03b6de092c6978c3d68d2b155cafd5e883b98d622b532a86344b1cc290f781e37a80074","active":true,"algorithm":"ARGON2"}]}]
immutable.users.db = true
phaas.pepper.source = string://secret-pepper

```
 
#### Demo & Swagger documentation
https://mysterious-island-45294.herokuapp.com

Basic Auth: testing-user / my-password



