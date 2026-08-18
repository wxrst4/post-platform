package middleware

import (
	"strconv"
	"time"

	"github.com/BaronPipistron/go-api-gateway/internal/telemetry/metrics"
	"github.com/gin-gonic/gin"
)

func Metrics(gatewayMetrics *metrics.Metrics) gin.HandlerFunc {
	return func(c *gin.Context) {
		start := time.Now()

		c.Next()

		route := c.GetString("route_name")
		if route == "" {
			route = "unmatched"
		}

		gatewayMetrics.RecordRequest(
			c.Request.Method,
			route,
			strconv.Itoa(c.Writer.Status()),
			time.Since(start),
		)
	}
}
