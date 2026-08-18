package app

import (
	"context"
	"github.com/BaronPipistron/go-api-gateway/internal/config"
	"github.com/BaronPipistron/go-api-gateway/internal/gateway"
	"github.com/BaronPipistron/go-api-gateway/internal/proxy"
	"github.com/BaronPipistron/go-api-gateway/internal/telemetry/metrics"
	httptransport "github.com/BaronPipistron/go-api-gateway/internal/transport/http"
	"go.uber.org/zap"
)

type App struct {
	server *Server
}

func New(cfg config.Config, logger *zap.SugaredLogger) (*App, error) {
	reverseProxy := proxy.NewReverseProxy(logger)
	gatewayMetrics := metrics.New()
	handler, err := gateway.NewHandler(cfg.Gateway.Routes, reverseProxy, gatewayMetrics, logger)
	if err != nil {
		return nil, err
	}
	router := httptransport.NewRouter(logger, handler, gatewayMetrics)

	server := NewServer(cfg.Server, router, logger)

	return &App{server: server}, nil
}

func (a *App) Run(ctx context.Context) error {
	return a.server.Run(ctx)
}
