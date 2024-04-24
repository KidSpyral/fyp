from fastapi import FastAPI
from pydantic import BaseModel
import numpy as np
from transformers import AutoModelForSequenceClassification, AutoTokenizer
import torch

app = FastAPI()

class SentimentRequest(BaseModel):
    sentence: str

class SentimentResponse(BaseModel):
    sentiment: str
    confidence: float

model1_path = 'C:/Users/kidsp/Downloads/fyp/kaggle/working/sentiment_model'
model2_path = 'C:/Users/kidsp/Downloads/fyp/kaggle/working/prayer_model'

# Load the model and tokenizer
model1 = AutoModelForSequenceClassification.from_pretrained(model1_path)
tokenizer1 = AutoTokenizer.from_pretrained(model1_path)
id2label1 = model1.config.id2label

# Load the model and tokenizer
model2 = AutoModelForSequenceClassification.from_pretrained(model2_path)
tokenizer2 = AutoTokenizer.from_pretrained(model2_path)
id2label2 = model2.config.id2label

@app.get("/", response_model=SentimentResponse)
def home(request: SentimentRequest):
    return "API for Islamic Modelling"

@app.post("/predict_moodsentiment", response_model=SentimentResponse)
def predict_moodsentiment(request: SentimentRequest):
    sentence = request.sentence
    inputs = tokenizer1(sentence, return_tensors="pt")
    outputs = model1(**inputs)
    logits = outputs.logits
    probabilities = torch.nn.functional.softmax(logits, dim=1).detach().numpy()[0]

    
    predicted_class = np.argmax(probabilities)
    confidence = probabilities[predicted_class]

    predicted_class_name = id2label1[predicted_class]

    return {"sentiment": (predicted_class_name), "confidence": float(confidence)}

@app.post("/predict_prayersentiment", response_model=SentimentResponse)
def predict_prayersentiment(request: SentimentRequest):
    sentence = request.sentence
    inputs = tokenizer2(sentence, return_tensors="pt")
    outputs = model2(**inputs)
    logits = outputs.logits
    probabilities = torch.nn.functional.softmax(logits, dim=1).detach().numpy()[0]

    
    predicted_class = np.argmax(probabilities)
    confidence = probabilities[predicted_class]

    predicted_class_name = id2label2[predicted_class]

    return {"sentiment": (predicted_class_name), "confidence": float(confidence)}




if __name__ == "__main__":
    import uvicorn

    uvicorn.run(app, host="127.0.0.1", port=8000)