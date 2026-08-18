package proxy

import "net/http"

func (p *ReverseProxy) handleError(
	w http.ResponseWriter,
	r *http.Request,
	err error,
) {
	info, _ := r.Context().Value(requestInfoKey{}).(requestInfo)

	p.logger.Errorw(
		"error while proxying request",
		"route", info.routeName,
		"upstream", info.upstream,
		"method", r.Method,
		"path", r.URL.Path,
		"error", err,
	)

	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusBadGateway)
	_, _ = w.Write([]byte(`{"error":"bad gateway"}`))
}
