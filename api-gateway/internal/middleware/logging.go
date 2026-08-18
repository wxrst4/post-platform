package middleware

import (
	"time"

	"github.com/gin-gonic/gin"
	"go.uber.org/zap"
)

func Logging(logger *zap.SugaredLogger) gin.HandlerFunc {
	return func(c *gin.Context) {
		start := time.Now()

		c.Next()

		logger.Infow("HTTP request",
			"request_id", c.GetString("X-Request-ID"),
			"route", c.GetString("route_name"),
			"upstream", c.GetString("upstream"),
			"method", c.Request.Method,
			"path", c.Request.URL.Path,
			"status", c.Writer.Status(),
			"client_ip", c.ClientIP(),
			"user_agent", c.Request.UserAgent(),
			"latency", time.Since(start).String(),
		)
	}
}
