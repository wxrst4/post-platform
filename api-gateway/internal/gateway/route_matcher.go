package gateway

import "strings"

type RouteMatcher struct {
	routes []*Route
}

func NewRouteMatcher(routes []*Route) *RouteMatcher {
	return &RouteMatcher{
		routes: routes,
	}
}

// /api/users
// /api/users/v1 - это лучше
func (m *RouteMatcher) Match(path string) (*Route, bool) {
	var best *Route

	for _, route := range m.routes {
		if !hasPathPrefix(path, route.PathPrefix) {
			continue
		}
		if best == nil || len(route.PathPrefix) > len(best.PathPrefix) {
			best = route
		}
	}

	return best, best != nil
}

// /api/users2 -> /api/users
func hasPathPrefix(path, prefix string) bool {
	if prefix == "" || prefix == "/" {
		return true
	}

	normalized := strings.TrimRight(prefix, "/")
	return path == normalized || strings.HasPrefix(path, normalized+"/")
}
