package logging

import (
	"github.com/BaronPipistron/go-api-gateway/internal/config"
	"go.uber.org/zap"
	"go.uber.org/zap/zapcore"
	"strings"
)

func New(cfg config.LoggerConfig) (*zap.SugaredLogger, error) {
	level := zapcore.InfoLevel
	if cfg.Level != "" {
		if err := level.UnmarshalText([]byte(strings.ToLower(cfg.Level))); err != nil {
			return nil, err
		}
	}

	var zapCfg zap.Config
	switch strings.ToLower(cfg.Mode) {
	case "dev":
		zapCfg = zap.NewDevelopmentConfig()
	case "prod":
		zapCfg = zap.NewProductionConfig()
	default:
		zapCfg = zap.NewDevelopmentConfig()
	}

	zapCfg.Level = zap.NewAtomicLevelAt(level)

	logger, err := zapCfg.Build()
	if err != nil {
		return nil, err
	}

	return logger.Sugar(), nil
}
