package config

import (
	"fmt"
	"os"
	"strings"
	"time"

	"github.com/go-viper/mapstructure/v2"
	"github.com/spf13/viper"
)

const defaultConfigPath = "./configs/config.yaml"

type Config struct {
	Server  ServerConfig  `mapstructure:"server"`
	Logger  LoggerConfig  `mapstructure:"logger"`
	Gateway GatewayConfig `mapstructure:"gateway"`
}

type ServerConfig struct {
	Host         string        `mapstructure:"host"`
	Port         int           `mapstructure:"port"`
	ReadTimeout  time.Duration `mapstructure:"read_timeout"`
	WriteTimeout time.Duration `mapstructure:"write_timeout"`
	IdleTimeout  time.Duration `mapstructure:"idle_timeout"`
}

type LoggerConfig struct {
	Mode  string `mapstructure:"mode"`
	Level string `mapstructure:"level"`
}

type GatewayConfig struct {
	Routes []RouteConfig `mapstructure:"routes"`
}

type RouteConfig struct {
	Name            string              `mapstructure:"name"`
	PathPrefix      string              `mapstructure:"path_prefix"`
	StripPrefix     string              `mapstructure:"strip_prefix"`
	Methods         []string            `mapstructure:"methods"`
	RequiredHeaders []string            `mapstructure:"required_headers"`
	PassHeaders     []string            `mapstructure:"pass_headers"`
	AddHeaders      map[string]string   `mapstructure:"add_headers"`
	LoadBalancing   LoadBalancingConfig `mapstructure:"load_balancing"`
	Upstream        []UpstreamConfig    `mapstructure:"upstream"`
}

type LoadBalancingConfig struct {
	Strategy string `mapstructure:"strategy"`
}

type UpstreamConfig struct {
	Url string `mapstructure:"url"`
}

func Load() (Config, error) {
	path := os.Getenv("CONFIG_PATH")
	if path == "" {
		path = defaultConfigPath
	}

	v := viper.New()
	v.SetConfigFile(path)
	v.SetConfigType("yaml")

	if err := v.ReadInConfig(); err != nil {
		return Config{}, err
	}

	var cfg Config
	if err := v.Unmarshal(
		&cfg,
		viper.DecodeHook(mapstructure.StringToTimeDurationHookFunc()),
	); err != nil {
		return Config{}, err
	}

	if err := validate(cfg); err != nil {
		return Config{}, err
	}

	return cfg, nil
}

func validate(cfg Config) error {
	if cfg.Server.Port <= 0 || cfg.Server.Port > 65535 {
		return fmt.Errorf("invalid server port: %d", cfg.Server.Port)
	}

	if len(cfg.Gateway.Routes) == 0 {
		return fmt.Errorf("gateway.routes must not be empty")
	}

	names := make(map[string]struct{}, len(cfg.Gateway.Routes))

	for _, route := range cfg.Gateway.Routes {
		if strings.TrimSpace(route.Name) == "" {
			return fmt.Errorf("route name must not be empty")
		}

		if _, exists := names[route.Name]; exists {
			return fmt.Errorf("duplicate route name: %s", route.Name)
		}
		names[route.Name] = struct{}{}

		if strings.TrimSpace(route.PathPrefix) == "" {
			return fmt.Errorf("route %s: path_prefix must not be empty", route.Name)
		}

		if len(route.Methods) == 0 {
			return fmt.Errorf("route %s: methods must not be empty", route.Name)
		}

		if len(route.Upstream) == 0 {
			return fmt.Errorf("route %s: upstream must not be empty", route.Name)
		}

		if route.LoadBalancing.Strategy != "round_robin" {
			return fmt.Errorf(
				"route %s: unsupported load balancing strategy %q",
				route.Name,
				route.LoadBalancing.Strategy,
			)
		}

		for _, upstream := range route.Upstream {
			if strings.TrimSpace(upstream.Url) == "" {
				return fmt.Errorf("route %s: upstream url must not be empty", route.Name)
			}
		}
	}

	return nil
}
