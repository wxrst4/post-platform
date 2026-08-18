package gateway

import (
	"net/http"

	"github.com/BaronPipistron/go-api-gateway/internal/config"
	"github.com/BaronPipistron/go-api-gateway/internal/proxy"
	"github.com/BaronPipistron/go-api-gateway/internal/telemetry/metrics"
	"github.com/gin-gonic/gin"
	"go.uber.org/zap"
)

type Handler struct {
	matcher *RouteMatcher
	proxy   *proxy.ReverseProxy
	metrics *metrics.Metrics
	logger  *zap.SugaredLogger
}

func NewHandler(
	routeConfigs []config.RouteConfig,
	reverseProxy *proxy.ReverseProxy,
	gatewayMetrics *metrics.Metrics,
	logger *zap.SugaredLogger,
) (*Handler, error) {
	routes := make([]*Route, 0, len(routeConfigs))

	for _, routeConfig := range routeConfigs {
		route, err := newRoute(routeConfig)
		if err != nil {
			return nil, err
		}
		routes = append(routes, route)
	}

	return &Handler{
		matcher: NewRouteMatcher(routes),
		proxy:   reverseProxy,
		metrics: gatewayMetrics,
		logger:  logger,
	}, nil
}

func (h *Handler) Handle(c *gin.Context) {
	route, ok := h.matcher.Match(c.Request.URL.Path)
	if !ok {
		h.metrics.IncRouteNotFound()
		c.JSON(http.StatusNotFound, gin.H{
			"error": "route not found",
		})
		return
	}

	c.Set("route_name", route.Name)

	if !route.IsMethodAllowed(c.Request.Method) {
		h.metrics.IncMethodNotAllowed(route.Name, c.Request.Method)
		c.Header("Allow", route.AllowedMethodsHeader())
		c.JSON(http.StatusMethodNotAllowed, gin.H{
			"error": "method not allowed",
		})
		return
	}

	if missingHeader := route.MissingRequiredHeader(c.Request.Header); missingHeader != "" {
		h.metrics.IncBadRequest(route.Name, "missing_required_header")

		status := http.StatusBadRequest
		if missingHeader == "Authorization" {
			status = http.StatusUnauthorized
		}

		c.JSON(status, gin.H{
			"error":  "missing required header",
			"header": missingHeader,
		})
		return
	}

	upstream, err := route.Balancer.Next()
	if err != nil {
		h.logger.Errorw("failed to get upstream",
			"route", route.Name,
			"error", err,
		)

		c.JSON(http.StatusBadGateway, gin.H{
			"error": "failed to get upstream",
		})
		return
	}

	c.Set("upstream", upstream.String())

	h.proxy.ServeHTTP(c.Writer, c.Request, proxy.RequestConfig{
		RouteName:   route.Name,
		Upstream:    upstream,
		StripPrefix: route.StripPrefix,
		AddHeaders:  route.AddHeaders,
		PassHeaders: route.PassHeaders,
	})
}
