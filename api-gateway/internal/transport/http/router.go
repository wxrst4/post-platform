package httptransport

import (
	"github.com/BaronPipistron/go-api-gateway/internal/gateway"
	"github.com/BaronPipistron/go-api-gateway/internal/middleware"
	"github.com/BaronPipistron/go-api-gateway/internal/telemetry/metrics"
	"github.com/gin-gonic/gin"
	"go.uber.org/zap"
)

func NewRouter(
	logger *zap.SugaredLogger,
	gatewayHandler *gateway.Handler,
	gatewayMetrics *metrics.Metrics,
) *gin.Engine {
	router := gin.New()
	router.RedirectTrailingSlash = false

	router.Use(
		gin.Recovery(),
		middleware.RequestID(),
		middleware.Logging(logger),
		middleware.Metrics(gatewayMetrics),
	)

	router.GET("/health", Health)

	router.GET("/metrics", func(c *gin.Context) {
		gateway.SetRouteName(c, "metrics")
		gatewayMetrics.Handler().ServeHTTP(c.Writer, c.Request)
	})

	router.NoRoute(gatewayHandler.Handle)

	return router
}
