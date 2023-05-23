# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),

## [Unreleased]

### Fixed
- returns 409 instead of 400 if a client was already registered with given certificate
- update OpenAPI by adding return codes


## [2.0.7] - 2023-05-12

### Fixed
- fix #47 - potential dataloss on concurrent database access @dvasunin (#50)
- fix #48 - possible multi-registration of clients @dvasunin (#52)
- fix #49 - user input not validated @dvasunin (#51)
- fix #33 - SKI and AKI validated as user input @dvasunin (#53)


## [2.0.6] - 2023-05-08

### Changed
- Specified USER in Dockerfile using IDs instead of name
- Specify runAsUser for deployment

### Removed
- Remove ingress controller sepecific annnotations from default config

## [2.0.5] - 2023-05-02

### Changed
- Updated librabries and upgrade spring boot


## [2.0.4] - 2023-04-24

### Fixed
- Fixed spring security web veracode security by upgrading its version to 6.0.3


## [2.0.3] - 2023-04-20

### Added
- Added Spring expression 6.0.8

### Changed
- Updated ARC42 document


## [2.0.2] - 2023-04-11

### Changed
- Upgrade Spring Boot to 3.0.5


## [2.0.1] - 2023-03-20

### Added
- Create request returns registration specific information, including clientID
- Added new env token in deployment

### Changed
- Changed secrets
- Changed the base image
- Modified values in values.yaml


## [2.0.0] - 2023-03-16

### Changed
- Upgrade Spring Boot to 3.0.3
- Upgrade Snakeyaml to 2.0 as 1.33 has security issue
- New application.properties changes
- Spring boot upgrade, keycloak upgrade
- Changed base image


## [1.0.6] - 2023-02-22

### Added
 - Added AUTHORS.md, INSTALL.md file
 - Added service port to values.yaml
 - Added comments in values.yaml
 - Created README.md inside charts/dapsreg-svc/

### Changed
 - Upgrade the springboot Library
 - Modified .helmignore file
 - Referring the tag from values.yaml to deployment.yaml
 - Make referringConnector parameter (which contains BPN number in suffix) be mandatory


## [1.0.5] - 2023-02-05

### Added
- Created new helm charts for DAPS registration service
- The latest version for daps registration service is 1.0.4

## [1.0.4] - 2022-02-13

### Added
- Registration in DAPS
- Adding BPN number in DAT token
- Provide CRUD operations to EDC connnector registration to the DAPS
- Keycloak protection is added

### Changed
- Generate the controller from the openAPI description
- Update all the used libraries to the latest version
- All smells from SonarQube were fixed
- Moved helm charts from `deployment/helm` to `charts`

### Removed
- Controller has been removed
