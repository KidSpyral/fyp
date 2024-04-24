import pandas as pd
import numpy as np
from sklearn.preprocessing import LabelEncoder
from transformers import Trainer, DistilBertTokenizer, TrainingArguments, AutoModelForSequenceClassification, AutoTokenizer
import torch

DEVICE = torch.device("cuda" if torch.cuda.is_available() else "cpu")
print(f'Device Available: {DEVICE}')

class DataLoader(torch.utils.data.Dataset):
    def __init__(self, sentences, labels, tokenizer, max_len):
        self.sentences = sentences
        self.labels = labels
        self.tokenizer = tokenizer
        self.max_len = max_len

    def __getitem__(self, idx):
        encoding = self.tokenizer(
            self.sentences[idx],
            truncation=True,
            padding='max_length',
            max_length=self.max_len,
            return_tensors='pt'
        )

        encoding = {key: val for key, val in encoding.items() if key != 'token_type_ids'}

        item = {key: val[0] for key, val in encoding.items()}

        if self.labels is not None:
            item['labels'] = torch.tensor(self.labels[idx], dtype=torch.long)

        return item

    def __len__(self):
        return len(self.sentences)

    def encode(self, idx):
        return self.tokenizer(idx, return_tensors='pt').to(DEVICE)

class SentimentModel():
    def __init__(self, model_path):
        self.model = AutoModelForSequenceClassification.from_pretrained(model_path).to(DEVICE)
        self.tokenizer = AutoTokenizer.from_pretrained(model_path)
        args = TrainingArguments(output_dir='C:/Users/kidsp/Downloads/fyp/kaggle/working/results', per_device_eval_batch_size=64)
        self.batch_model = Trainer(model=self.model, args=args, train_dataset=None)  # You need to provide the train_dataset
        self.single_dataloader = DataLoader(sentences=[], labels=[], tokenizer=self.tokenizer, max_len=256)

    def batch_predict_proba(self, x):
        predictions = self.batch_model.predict(DataLoader(sentences=x, labels=None, tokenizer=self.tokenizer, max_len=256))
        logits = torch.from_numpy(predictions.predictions)

        if DEVICE == 'cpu':
            proba = torch.nn.functional.softmax(logits, dim=1).detach().numpy()
        else:
            proba = torch.nn.functional.softmax(logits, dim=1).to('cpu').detach().numpy()
        return proba

    def predict_proba(self, x):
        x = self.single_dataloader.encode(x).to(DEVICE)
        predictions = self.model(**x)
        logits = predictions.logits

        if DEVICE == 'cpu':
            proba = torch.nn.functional.softmax(logits, dim=1).detach().numpy()
        else:
            proba = torch.nn.functional.softmax(logits, dim=1).to('cpu').detach().numpy()
        return proba

df_test = pd.read_csv('C:/Users/kidsp/Downloads/fyp/test.csv', header=None, names=['sentence', 'label', 'label_text'])
df_test['label'].unique()

le = LabelEncoder()
df_test['label'] = le.fit_transform(df_test['label'])
df_test['label_text'] = le.inverse_transform(df_test['label'])
df_test.head()

batch_sentences = df_test['sentence'].sample(n=min(10000, len(df_test['sentence'].unique())), random_state=1).to_list()
single_sentence = df_test.sample(n=1, random_state=1)['sentence'].to_list()[0]

sentiment_model = SentimentModel('C:/Users/kidsp/Downloads/fyp/kaggle/working/prayer_model')

single_sentence_probas = sentiment_model.predict_proba(single_sentence)
label2id = sentiment_model.model.config.id2label
predicted_class_label = label2id[np.argmax(single_sentence_probas)]
print(predicted_class_label)


