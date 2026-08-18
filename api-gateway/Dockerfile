FROM golang:1.25-alpine AS build

WORKDIR /app

COPY go.mod go.sum ./

RUN go mod download

COPY . .

RUN go build -o gateway ./cmd/gateway


FROM alpine:3.22

WORKDIR /app

COPY --from=build /app/gateway /app/gateway
COPY configs ./configs

EXPOSE 8080

ENTRYPOINT ["/app/gateway"]