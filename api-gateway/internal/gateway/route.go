package gateway

import (
	"github.com/BaronPipistron/go-api-gateway/internal/balancer"
	"github.com/BaronPipistron/go-api-gateway/internal/config"
	"net/http"
	"net/url"
	"strings"
)

type Route struct {
	Name            string
	PathPrefix      string
	StripPrefix     string
	RequiredHeaders []string
	PassHeaders     []string
	AddHeaders      map[string]string
	Balancer        balancer.Balancer
	Upstreams       []*url.URL

	methods []string
}

func newRoute(cfg config.RouteConfig) (*Route, error) {
	upstreams := make([]*url.URL, 0, len(cfg.Upstream))
	for _, u := range cfg.Upstream {
		parsed, err := url.Parse(u.Url)
		if err != nil {
			return nil, err
		}
		upstreams = append(upstreams, parsed)
	}

	loadBalancer, err := balancer.NewRoundRobinBalancer(upstreams)
	if err != nil {
		return nil, err
	}

	return &Route{
		Name:            cfg.Name,
		PathPrefix:      strings.TrimRight(cfg.PathPrefix, "/"),
		StripPrefix:     strings.TrimRight(cfg.StripPrefix, "/"),
		RequiredHeaders: cfg.RequiredHeaders,
		PassHeaders:     cfg.PassHeaders,
		AddHeaders:      cfg.AddHeaders,
		Balancer:        loadBalancer,
		Upstreams:       upstreams,
		methods:         cfg.Methods,
	}, nil
}

func (r *Route) IsMethodAllowed(method string) bool {
	for _, m := range r.methods {
		if m == method {
			return true
		}
	}

	return false
}

func (r *Route) AllowedMethodsHeader() string {
	return strings.Join(r.methods, ", ")
}

func (r *Route) MissingRequiredHeader(headers http.Header) string {
	for _, headerName := range r.RequiredHeaders {
		if strings.TrimSpace(headers.Get(headerName)) == "" {
			return headerName
		}
	}

	return ""
}
