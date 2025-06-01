curl.exe -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password123"}'

  curl.exe -X POST http://localhost:8080/api/mieszkania \
    -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTc0ODkwNjMxMSwiZXhwIjoxNzQ4OTQyMzExfQ.TJP1R3LLnznAIo9w8_c9UvDnUqetyPJFuNLeH1FyCEM" \
    -H "Content-Type: application/json" \
    -d '{"developer":"Test","investment":"Test","number":"T1","area":50,"price":500000,"rooms":2,"lat":54.0,"lng":18.0}'
