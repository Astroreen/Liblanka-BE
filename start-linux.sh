export $(cat .env | xargs)
./gradlew bootRun --args='--spring.profiles.active=local'