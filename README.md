## Phaas

[![Build Status](https://travis-ci.org/TomiTakussaari/phaas.svg?branch=master)](https://travis-ci.org/TomiTakussaari/phaas)
[![Coverage Status](https://coveralls.io/repos/github/TomiTakussaari/phaas/badge.svg?branch=master)](https://coveralls.io/github/TomiTakussaari/phaas?branch=master)
[![Coverity Scan Build Status](https://scan.coverity.com/projects/11237/badge.svg)](https://scan.coverity.com/projects/tomitakussaari-phaas)
[SonarQube](https://sonarqube.com/dashboard?id=com.github.tomitakussaari%3Aphaas)

Small(ish) http api for securely protecting passwords and generating tokens.

##### What and how

### Interactive demo & Swagger documentation
- https://mysterious-island-45294.herokuapp.com/swagger-ui.html 
- Authentication: (Basic) testing-user / my-password


Password hash request:

```    
         
      $ curl 'http://localhost:8080/passwords/hash' -X PUT --data-binary $'{"rawPassword": "password"}' \ 
      -H 'Authorization: Basic YWRtaW46cFY7RXZTO21vUGE2Pmo1XEVnSF4wJSs4UDMnL0ZW' \  
      -H 'Content-Type: application/json' -H 'Accept: application/json'
      
      # returns encrypted hash of password 
      {
        "hash" : "1.9febef75606f6c94.6ac2253774863bfdebd3d71eac2651724997865557962cd57659301932bf500d86a74a677955beec6304ffb9a60aad356c876d59b2d4d074c3df59fd89e8803f7572d68dbe38b24acf1dfd4b801e4caa"
      }
      
```   

Password verification request:      

```
      
      curl -X PUT --header 'Content-Type: application/json' --header 'Accept: application/json' \
        -H 'Authorization: Basic YWRtaW46cFY7RXZTO21vUGE2Pmo1XEVnSF4wJSs4UDMnL0ZW' \
        -d '{ \ 
         "passwordCandidate": "password", \ 
         "hash": "1.a7a8bb21c63765c4.e47a382752d1bbd2336196059438ec6bddea3c15753d8d1b3cf585e326af907cafd269cbb309fd9521cc48f82a0a8a0f98448bc9fab5859af20ed96163759deb61e133181689045cfac7f91390f05a91" \ 
       }' 'http://localhost:8080/passwords/verify'
       
       # returns http 200 ok if password matches, upgradedHash is returned if there is new (better?) way to protect that password
       {
         "upgradedHash": null,
         "valid": true
       }

      
  ``` 
 
Create encrypted and signed token. 
- can be used, for example, to implement sessions without having any server side state.
- claims can contain pretty much anything you want, that can be serialized as JSON.

```

curl -X POST --header 'Content-Type: application/json' -H 'Authorization: Basic YWRtaW46cFY7RXZTO21vUGE2Pmo1XEVnSF4wJSs4UDMnL0ZW' \ -H 'Accept: text/plain' -d '{ \ 
   "claims": {"claim1": "value1"},  \ 
 }' 'http://localhost:8080/tokens'
 
 # returns json 
 {
   "id": "763c374d-8666-4da4-aaa9-6a491045a19e",
   "token": "eyJ6aXAiOiJERUYiLCJraWQiOiIxIiwiY3R5IjoiVG9rZW5zIiwiZW5jIjoiQTI1NkdDTSIsImFsZyI6ImRpciJ9..PgTKh_GbC3r_8FaQ.Rcn73C5xouJeOdSp17WSVpPqFtATtMDwNC91pvSw0cOy4X5WfQp_1AoFuuWF1giuUGNRYnUSpSlr0GKAWl2oqbLX1ocYb_npwR8mRTQJufSWuEr6o8ecMfb_jmw3uhJi9ABHF_0n10FJDdqyXnh26V7aZAauoMmL2gE-apNC-i-UHJQLP9nIQecDd5FpyHRbSLb6uvPYLj5wup2Ff8MgtgncIYlKcWDeOg.mQhLkKzrg8slutfRNySz6w"
 }

```

Parse and verify token.

```

curl -X PUT --header 'Content-Type: application/json' -H 'Authorization: Basic YWRtaW46cFY7RXZTO21vUGE2Pmo1XEVnSF4wJSs4UDMnL0ZW' -H 'Accept: application/json' -d '{ \ 
   "token": "eyJ6aXAiOiJERUYiLCJraWQiOiIxIiwiY3R5IjoiVG9rZW5zIiwiZW5jIjoiQTI1NkdDTSIsImFsZyI6ImRpciJ9..PgTKh_GbC3r_8FaQ.Rcn73C5xouJeOdSp17WSVpPqFtATtMDwNC91pvSw0cOy4X5WfQp_1AoFuuWF1giuUGNRYnUSpSlr0GKAWl2oqbLX1ocYb_npwR8mRTQJufSWuEr6o8ecMfb_jmw3uhJi9ABHF_0n10FJDdqyXnh26V7aZAauoMmL2gE-apNC-i-UHJQLP9nIQecDd5FpyHRbSLb6uvPYLj5wup2Ff8MgtgncIYlKcWDeOg.mQhLkKzrg8slutfRNySz6w" \ 
 }' 'http://localhost:8080/tokens'
 
 # returns json response 
 {
   "claims": {
     "claim1": "value1"
   },
   "issuedAt": "2017-01-04T19:09:51+02:00",
   "id": "763c374d-8666-4da4-aaa9-6a491045a19e"
 }
 
```

Returned tokens and password hashes are encrypted, with encryption key that is stored (encrypted) in Phaas user database.
Encryption key itself is encrypted with user's password, and pepper. Pepper is global, and can be given to Phaas as a String, or Phaas can read it from file or via http(s).


### Why
- This way, password hashes and tokens themselves are completely useless even if attacker 
    - Attacker would need to also dump data from phaas user db, find out pepper that phaas is using, and figure out password that application was using to communicate with phaas.
    - Possible ? Certainly, but much more difficult
- En/decrypting data, and verifying passwords, can be cpu consuming job, so it might be good idea to offload it to servers that are not busy with your business logic
- Application only needs to store credentials it uses to talk with phaas, no more "secretKeys" stored inside your codebase


### Configuration

User database can be initialized statically using environment variables. 
See [UsersFromEnvironmentStringCreator](src/test/java/com/github/tomitakussaari/phaas/user/UsersFromEnvironmentStringCreator.java) for examples.

This way you can run multiple instances of Phaas with same set of users and configuration.
Phaas can also use external database for it's user, but there is no documentation about it yet.

```properties

# example of environment variables configuration that will initialize phaas user database with user "testing-user", make user database immutable and use pepper "secret-pepper" when protecting data

db.users.content = [{"userDTO":{"id":null,"userName":"testing-user","roles":"ROLE_USER","sharedSecretForSigningCommunication":"secret"},"userConfigurationDTOs":[{"id":null,"user":"testing-user","dataProtectionKey":"$2.0f021e62f1f18e08.66e971f100652ac17b560750f527af7057cbacff5449100dcd50627af03b6de092c6978c3d68d2b155cafd5e883b98d622b532a86344b1cc290f781e37a80074","active":true,"algorithm":"ARGON2"}]}]
immutable.users.db = true
phaas.pepper.source = string://secret-pepper # could also be file://secret.txt or https://my-http-server/secret|my-custom-header=header-value&my-other-custom-header=value2

```
 



