FROM python:3.9-slim
WORKDIR /app
COPY . /app

RUN pip install --no-cache-dir -r requirements.txt

EXPOSE 5000

# to allow prints for debugging
ENV PYTHONUNBUFFERED=1

CMD ["python", "load_generator_service.py"]
