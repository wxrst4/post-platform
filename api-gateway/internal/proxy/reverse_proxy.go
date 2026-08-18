package proxy

import (
	"context"
	"net/http"
	"net/http/httputil"
	"net/url"
	"strings"

	"go.uber.org/zap"
)

type RequestConfig struct {
	RouteName   string
	Upstream    *url.URL
	StripPrefix string
	PassHeaders []string
	AddHeaders  map[string]string
}

type ReverseProxy struct {
	logger *zap.SugaredLogger
}

type requestInfo struct {
	routeName string
	upstream  string
}

func NewReverseProxy(logger *zap.SugaredLogger) *ReverseProxy {
	return &ReverseProxy{logger: logger}
}

func (p *ReverseProxy) ServeHTTP(
	w http.ResponseWriter,
	r *http.Request,
	cfg RequestConfig,
) {
	targetUpstream := *cfg.Upstream

	info := requestInfo{
		routeName: cfg.RouteName,
		upstream:  targetUpstream.String(),
	}

	proxyRequest := r.WithContext(
		context.WithValue(
			r.Context(),
			requestInfoKey{},
			info,
		),
	)

	reverseProxy := &httputil.ReverseProxy{
		Director: func(out *http.Request) {
			out.URL.Scheme = targetUpstream.Scheme
			out.URL.Host = targetUpstream.Host
			out.URL.Path = singleJoiningSlash(
				targetUpstream.Path,
				stripPathPrefix(r.URL.Path, cfg.StripPrefix),
			)
			out.URL.RawPath = ""
			out.URL.RawQuery = r.URL.RawQuery
			out.Host = targetUpstream.Host

			prepareHeaders(r, out, cfg)
		},
		ErrorHandler: p.handleError,
	}

	reverseProxy.ServeHTTP(w, proxyRequest)
}

func stripPathPrefix(path, prefix string) string {
	if prefix == "" || prefix == "/" {
		return path
	}

	normalized := strings.TrimRight(prefix, "/")

	if path == normalized {
		return "/"
	}

	if strings.HasPrefix(path, normalized+"/") {
		stripped := strings.TrimPrefix(path, normalized)
		if stripped == "" {
			return "/"
		}
		return stripped
	}

	return path
}

func singleJoiningSlash(a, b string) string {
	aHasSlash := strings.HasSuffix(a, "/")
	bHasSlash := strings.HasPrefix(b, "/")

	switch {
	case aHasSlash && bHasSlash:
		return a + b[1:]
	case !aHasSlash && !bHasSlash:
		return a + "/" + b
	default:
		return a + b
	}
}

func prepareHeaders(
	in *http.Request,
	out *http.Request,
	cfg RequestConfig,
) {
	out.Header = make(http.Header)

	if len(cfg.PassHeaders) == 0 {
		for name, values := range in.Header {
			for _, value := range values {
				out.Header.Add(name, value)
			}
		}
	} else {
		for _, name := range cfg.PassHeaders {
			for _, value := range in.Header.Values(name) {
				out.Header.Add(name, value)
			}
		}
	}

	for name, value := range cfg.AddHeaders {
		out.Header.Set(name, value)
	}
}

type requestInfoKey struct{}
