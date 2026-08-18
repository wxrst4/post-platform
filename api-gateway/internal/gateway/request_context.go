package gateway

import "github.com/gin-gonic/gin"

const (
	RequestIDKey = "X-Request-ID"
	RouteNameKey = "route_name"
	UpstreamKey  = "upstream"
)

func SetRequestID(c *gin.Context, id string) {
	c.Set(RequestIDKey, id)
}

func SetRouteName(c *gin.Context, name string) {
	c.Set(RouteNameKey, name)
}

func SetUpstream(c *gin.Context, upstream string) {
	c.Set(UpstreamKey, upstream)
}
