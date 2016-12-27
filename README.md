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

- Protects data by encrypting, or signing, it with secret key
- Secret key is stored in encrypted format in phaas user database
- Client password (sent by via HTTP/BasicAuth) are used to decrypt secret key per request
- Attacker needs to compromise both Phaas user database and client app that is using Phaas to decrypt data, adding another layer of security.
- Supports:
    - Password hashing & verification
    - JSON Web Token create & verification
    - Data HMAC create & verification
    - Data encrypts & decrypt
- User database can be initialized by environment variable, making it immutable, or by connecting to any database that supports JDBC

```properties

# example of environment configuration that will initialize phaas user database with user "testing-user", and makes user database immutable

db.users.content = [{"userDTO":{"id":null,"userName":"testing-user","passwordHash":"$2a$10$BRHdJNvWFTXEZUqmgOrrWOgjAQLfEmojjeVvzsf.PrNEWCPs.4CQq","roles":"ROLE_USER","sharedSecretForSigningCommunication":"secret"},"userConfigurationDTOs":[{"id":null,"user":"testing-user","dataProtectionKey":"2a0be8ce62026ccc.3b650d5231ec2ad6ffa02b00b64c24a51fe59dbc463613dcc8805e78a1ec0b34855600e8f91f301a2259277d0d12091ccd0400027eff25e33887f4e547e5c1e4051540cb39019db8571ba55ef6f0d32bc357731b41e3e70a2c6f699e9473803dc7d465205447fac9563c3165aaf85f47","active":true,"algorithm":"DEFAULT_SHA256ANDBCRYPT"}]}]
immutable.users.db = true

```
 
#### Demo & Swagger documentation
https://mysterious-island-45294.herokuapp.com

Basic Auth: testing-user / my-password



