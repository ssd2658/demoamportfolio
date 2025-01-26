# MyPortfolio Application

## Docker Deployment

### Prerequisites
- Docker
- Docker Compose

### Local Deployment Steps

1. Clone the repository
2. Navigate to the project directory
3. Build and run the application:

```bash
docker-compose up --build
```

The application will be accessible at `http://localhost:8080`

### Stopping the Application

```bash
docker-compose down
```

### Development Notes
- Uses Spring Boot 3.4.1
- Java 17
- Dockerized for easy local deployment
