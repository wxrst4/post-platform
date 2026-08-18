package balancer

import (
	"fmt"
	"net/url"
	"sync"
)

type RoundRobinBalancer struct {
	mu        sync.Mutex
	upstreams []*url.URL
	next      int
}

func NewRoundRobinBalancer(upstreams []*url.URL) (*RoundRobinBalancer, error) {
	if len(upstreams) == 0 {
		return nil, fmt.Errorf("round-robin balancer requires at least one upstream")
	}

	copied := make([]*url.URL, 0, len(upstreams))
	for _, u := range upstreams {
		if u == nil {
			return nil, fmt.Errorf("round-robin balancer requires non-nil upstreams")
		}
		value := *u
		copied = append(copied, &value)
	}

	return &RoundRobinBalancer{
		upstreams: copied,
	}, nil
}

func (b *RoundRobinBalancer) Next() (*url.URL, error) {
	b.mu.Lock()
	defer b.mu.Unlock()

	if len(b.upstreams) == 0 {
		return nil, fmt.Errorf("no upstreams configured")
	}

	upstream := b.upstreams[b.next]
	b.next = (b.next + 1) % len(b.upstreams)

	value := *upstream
	return &value, nil
}
