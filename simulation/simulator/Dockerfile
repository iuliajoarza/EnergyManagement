FROM python:3.12-slim
WORKDIR /app
COPY simulator.py .
RUN pip install pika psycopg2-binary
CMD ["python", "-u", "simulator.py"]
