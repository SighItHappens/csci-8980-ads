CACHE_SIZE_LIMIT = 100

class Config(object):
    CACHE_SIZE = CACHE_SIZE_LIMIT

class ProductionConfig(Config):
    pass

class DevelopmentConfig(Config):
    CACHE_SIZE = CACHE_SIZE_LIMIT
    MODEL_NAME = 'MLP'

class TestingConfig(Config):
    CACHE_SIZE = 100