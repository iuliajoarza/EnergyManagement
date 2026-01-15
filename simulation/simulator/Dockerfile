FROM python:3.12-slim
WORKDIR /app
COPY simulator.py .
RUN pip install pika
CMD ["python", "-u", "simulator.py"]
