import os

class Config:
    SECRET_KEY = os.urandom(24)
    UPLOAD_FOLDER = os.path.join(os.getcwd(), 'data')
    STATIC_FOLDER = os.path.join(os.getcwd(), 'static/images')
    ALLOWED_EXTENSIONS = {'csv'}
    MAX_CONTENT_LENGTH = 16 * 1024 * 1024  # 16MB