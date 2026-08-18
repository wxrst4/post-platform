package metrics

import (
	"github.com/prometheus/client_golang/prometheus"
	"github.com/prometheus/client_golang/prometheus/promhttp"
	"net/http"
	"runtime"
	"time"
)

var (
	MemoryAlloc = prometheus.NewGauge(
		prometheus.GaugeOpts{
			Name: "memory_alloc",
			Help: "Current memory allocation",
		})

	NumGoroutine = prometheus.NewGauge(
		prometheus.GaugeOpts{
			Name: "num_goroutine",
			Help: "Number of goroutines",
		})
)

type Metrics struct {
	registry *prometheus.Registry

	requestsTotal    *prometheus.CounterVec
	requestDuration  *prometheus.HistogramVec
	upstreamRequests *prometheus.CounterVec
	routeNotFound    prometheus.Counter
	methodNotAllowed *prometheus.CounterVec
	badRequest       *prometheus.CounterVec
}

func New() *Metrics {
	m := &Metrics{
		registry: prometheus.NewRegistry(),
		requestsTotal: prometheus.NewCounterVec(
			prometheus.CounterOpts{
				Name: "gateway_requests_total",
				Help: "Total number of HTTP requests",
			},
			[]string{"method", "path", "status"},
		),
		requestDuration: prometheus.NewHistogramVec(
			prometheus.HistogramOpts{
				Name: "gateway_request_duration_seconds",
				Help: "Duration of HTTP requests",
			},
			[]string{"method", "path", "status"},
		),
		upstreamRequests: prometheus.NewCounterVec(
			prometheus.CounterOpts{
				Name: "gateway_upstream_requests_total",
				Help: "Total number of requests sent to upstream services",
			},
			[]string{"method", "path", "status"},
		),
		routeNotFound: prometheus.NewCounter(
			prometheus.CounterOpts{
				Name: "gateway_route_not_found_total",
				Help: "Total number of requests for non-existent routes",
			},
		),
		methodNotAllowed: prometheus.NewCounterVec(
			prometheus.CounterOpts{
				Name: "gateway_method_not_allowed_total",
				Help: "Total number of requests with unsupported HTTP methods",
			},
			[]string{"route", "method"},
		),
		badRequest: prometheus.NewCounterVec(
			prometheus.CounterOpts{
				Name: "gateway_bad_request_total",
				Help: "Total number of requests with invalid or malformed data",
			},
			[]string{"route", "reason"},
		),
	}

	m.registry.MustRegister(
		m.requestsTotal,
		m.requestDuration,
		m.upstreamRequests,
		m.routeNotFound,
		m.methodNotAllowed,
		m.badRequest,
	)

	return m
}

func (m *Metrics) Handler() http.Handler {
	return promhttp.HandlerFor(m.registry, promhttp.HandlerOpts{})
}

func StartRuntimeMetricsCollector() {
	go func() {
		var memStats runtime.MemStats
		for {
			runtime.ReadMemStats(&memStats)
			MemoryAlloc.Set(float64(memStats.Alloc))
			NumGoroutine.Set(float64(runtime.NumGoroutine()))

			time.Sleep(1 * time.Second)
		}
	}()
}

func (m *Metrics) IncBadRequest(route string, reason string) {
	m.badRequest.WithLabelValues(route, reason).Inc()
}

func (m *Metrics) IncMethodNotAllowed(route string, method string) {
	m.methodNotAllowed.WithLabelValues(route, method).Inc()
}

func (m *Metrics) IncRouteNotFound() {
	m.routeNotFound.Inc()
}

func (m *Metrics) IncUpstreamRequest(method string, path string, status string) {
	m.upstreamRequests.WithLabelValues(method, path, status).Inc()
}

func (m *Metrics) RecordRequest(method string, path string, status string, duration time.Duration) {
	m.requestsTotal.WithLabelValues(method, path, status).Inc()
	m.requestDuration.WithLabelValues(method, path, status).Observe(duration.Seconds())
}
