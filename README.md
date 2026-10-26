## Development
We recommend using IntelliJ IDEA Ultimate Edition for development with project folder opened as project.

#### Prerequisites
- Java 25
- Node 24 + NPM
- Docker & Docker Compose Plugin

### Preparation
- Clone this repository.
- Run `npm install` in the `ui` folder.

### Launching individual components
- Development database may be launched by running `docker-compose up -d` in the `db/dev` directory.
- API may be launched from your development environment or via Gradle using `./gradlew bootRun` in `api` directory. The API is listening on port `8080`.
- UI may be launched by running `npx nx s` in the `ui` directory. The UI server is listening on port `4200` and is reverse proxying requests to the API.

> [!WARNING]
> If you are running deploying the app on Linux or WSL, then you'll need to run `db/fix-data-permissions.sh` script. This is due to used database image running with uid and gid `54321`, and expects it for its data directory.

> [!NOTE]
> You may need to adjust a path in `api/src/main/resources/application.properties` if your working directory is not the root of this repository when launching the API.

> [!WARNING]
> By default, when the application is started for first time, then admin user with username `admin` and password `adminpass` is created. You should change password of this user as soon as possible.
