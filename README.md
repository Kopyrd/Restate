# REstate - System Zarządzania Nieruchomościami

REstate to aplikacja do zarządzania nieruchomościami 
- zbudowana w technologii Spring Boot. 
- Umożliwia użytkownikom przeglądanie ofert nieruchomości, a administratorom zarządzanie mieszkaniami i użytkownikami..

## Spis treści


## Funkcje

- Rejestracja, logowanie i zarządzanie profilem użytkownika
- Zarządzanie mieszkaniami (dodawanie, edycja, usuwanie, wyszukiwanie)
- Kontrola dostępu oparta na rolach (USER, ADMIN)
- Zaawansowane wyszukiwanie mieszkań wg wielu kryteriów
- RESTful API z dokumentacją Swagger
- Obsługa konteneryzacji (Docker)

## Technologia

- **Backend**: Java 21, Spring Boot 3.5
- **Baza danych**: PostgreSQL 16
- **Bezpieczeństwo**: Spring Security, JWT (JSON Web Token)
- **Migracje bazy danych**: Flyway
- **Dokumentacja API**: Swagger/OpenAPI
- **Budowanie projektu**: Maven
- **Konteneryzacja**: Docker, Docker Compose

## Pierwsze kroki

### Wymagania wstępne

- Java 21 lub wyższa
- Maven 3.6 lub wyższy
- PostgreSQL 16 (lub Docker)

### Instalacja

1. Sklonuj repozytorium:

    ```bash
    git clone https://github.com/Kopyrd/Restate.git
    cd restate
    ```

2. Zbuduj projekt:

    ```bash
    mvn clean install
    ```

3. Uruchom aplikację:

    ```bash
    mvn spring-boot:run
    ```

5. Aplikacja będzie dostępna pod adresem http://localhost:8080

### Uruchamianie z Dockerem

1. Upewnij się, że Docker i Docker Compose są zainstalowane.

2. Uruchom kontenery:

    ```bash
    docker-compose up --build -d  //pierwsze uruchomienie
    docker-compose up -d          //kolejne uruchomienie obrazu
    ```

3. Aplikacja będzie dostępna pod adresem http://localhost:8080

## Dokumentacja API

Interfejs Swagger UI dostępny jest pod adresem:  
http://localhost:8080/swagger-ui.html

## Uwierzytelnianie

Aplikacja wykorzystuje JWT (JSON Web Token):

1. Użytkownik loguje się przy użyciu loginu i hasła.
2. Serwer zwraca token JWT po weryfikacji danych


   Baza początkowo posiada dwóch użytkowników:
   - **admin**: password123
   - **user1**: password123
   - aby się zalogować nalezy skorzystać z endpointu '/api/auth/login` - wpisac dane jednego z ww. użytkownikow,
     a następnie skopiować wygenerowany token który jest w przykładowej postaci:
- eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTc0OTMzODUzMSwiZXhwIjoxNzQ5Mzc0NTMxfQ.nyOFWVNVy8pXVe6UZQK_HyzXRVVnHdgk4EB7jUeTCSg
  wygenerowany token nalezy wkleic w polu "Authorize" i aktywować.
- 
3. Token jest dołączany do każdego żądania w nagłówku `Authorization`
4. Serwer weryfikuje token dla chronionych endpointów

**Konfiguracja tokenu:**
- Czas ważności: 24h
- Klucz sekretu: w `application.properties`

### Główne endpointy

#### Authentication
- `POST /api/auth/login` – logowanie użytkownika
- `POST /api/auth/register` – rejestracja użytkownika

#### Users
- `GET /api/users` – pobierz listę użytkowników (admin)
- `GET /api/users/{id}` – pobierz użytkownika po ID (admin)
- `PUT /api/users/{id}` – aktualizuj użytkownika (admin)
- `DELETE /api/users/{id}` – usuń użytkownika (admin)
- `POST /api/users/admin` – dodaj nowego administratora (admin)
- `GET /api/users/profile` – profil zalogowanego użytkownika (user / admin)
- `PUT /api/users/me` – aktualizacja profilu użytkownika (user /admin)

#### Properties (Mieszkania)
- `GET /api/mieszkania` – pobierz wszystkie mieszkania
- `GET /api/mieszkania/{id}` – szczegóły mieszkania (jedyny dostepny dla niezalogowanego uzytkownika)
- `POST /api/mieszkania` – dodaj mieszkanie (admin)
- `PUT /api/mieszkania/{id}` – edytuj mieszkanie (admin)
- `DELETE /api/mieszkania/{id}` – usuń mieszkanie (admin)
- `GET /api/mieszkania/investment/{investment}` – mieszkania wg inwestycji
- `GET /api/mieszkania/price-range` – mieszkania wg ceny
- `POST /api/mieszkania/search` – zaawansowane wyszukiwanie mieszkań
- `PATCH /api/mieszkania/{id}/status` – zmiana statusu mieszkania (admin)

Endpointy, które nie modyfikują informacji lecz je pobierają - dostępne dla każdego autoryzowanego użytkownika.
Pozostałe endpointy dostępne tylko dla administratora.

## Schemat bazy danych
![ERD Diagram](docs/images/ERD.png)

### User

- `id`
- `username`
- `password`
- `email`
- `first_name`
- `last_name`
- `role` (USER/ADMIN)
- `is_enabled`
- `created_at`

### Mieszkanie (Apartment)

- `id`
- `developer`
- `investment`
- `number`
- `area`
- `price`
- `voivodeship`
- `city`
- `district`
- `floor`
- `status` (AVAILABLE, RESERVED, SOLD)
- `description`
- `created_by`
- `created_at`
- `updated_at`

## Testy jednostkowe i integracyjne

**Wyniki pokrycia - testy jednostkowe**
![Unit Tests](docs/images/UTest.png)

**Wyniki pokrycia - testy integracyjne**
![Integration Tests](docs/images/ITEST.png)

