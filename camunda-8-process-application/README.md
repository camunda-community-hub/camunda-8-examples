# Camunda 8 Process Application
built with 8.0.0

## Topic
Creation of a process application by using the APIs offered: Tasklist, Zeebe, Operate.

## Development and building

### Backend
Spring-Boot Application that is built using `mvn clean package` or run with an IDE

To run the Application, please make sure you have a `application-dev.yaml` in your `src/main/resources`that contains 
the cluster credentials.

### Frontend
Vue3 Application that is built using `npm run build` or run in dev mode with `npm run dev`

## Packaging all toghether

By running `mvn clean package` on the parent, you will get a jar file inside the backend project that contains backend and frontend.
By providing credentials for the Zeebe Client while giving the API Client the required scopes (Tasklist, Operate, Zeebe), all clients will work with Cloud.

## Additional information

