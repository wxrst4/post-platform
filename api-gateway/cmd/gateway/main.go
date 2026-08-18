package main

import (
	"context"
	"errors"
	"github.com/BaronPipistron/go-api-gateway/internal/app"
	"github.com/BaronPipistron/go-api-gateway/internal/config"
	"github.com/BaronPipistron/go-api-gateway/internal/telemetry/logging"
	"log"
	"net/http"
	"os"
	"os/signal"
	"syscall"
)

func main() {
	cfg, err := config.Load()
	if err != nil {
		log.Fatalf("config error: %v", err)
	}

	logger, err := logging.New(cfg.Logger)
	if err != nil {
		log.Fatalf("logger error: %v", err)
	}
	defer func() {
		_ = logger.Sync()
	}()

	application, err := app.New(cfg, logger)
	if err != nil {
		logger.Fatalf("app error: %v", err)
	}

	ctx, stop := signal.NotifyContext(context.Background(), os.Interrupt, syscall.SIGTERM)
	defer stop()

	if err := application.Run(ctx); err != nil && !errors.Is(err, http.ErrServerClosed) {
		logger.Fatalf("app error: %v", err)
	}
}
