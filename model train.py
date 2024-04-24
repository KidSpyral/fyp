from unittest.util import _MAX_LENGTH
import numpy as np
import pandas as pd
from fast_ml.model_development import train_valid_test_split
from sklearn.model_selection import train_test_split
from transformers import DistilBertTokenizer, BertTokenizer, Trainer, TrainingArguments, AutoConfig, AutoTokenizer, AutoModelForSequenceClassification
import torch
from torch.nn.functional import softmax
from sklearn.metrics import classification_report
from sklearn.preprocessing import LabelEncoder
import datasets
import os



DEVICE = torch.device("cuda" if torch.cuda.is_available() else "cpu")
print (f'Device Availble: {DEVICE}')

df_train = pd.read_csv('C:/Users/kidsp/Downloads/fyp/train.txt', delimiter=';', header=None, names=['sentence','label','label_text'])
df_test = pd.read_csv('C:/Users/kidsp/Downloads/fyp/test.txt', delimiter=';', header=None, names=['sentence','label','label_text'])
df_val = pd.read_csv('C:/Users/kidsp/Downloads/fyp/val.txt', delimiter=';', header=None, names=['sentence','label','label_text'])


df_train['label'].unique()

le = LabelEncoder()
df_train['label'] = le.fit_transform(df_train['label'])
df_train['label_text'] = le.inverse_transform(df_train['label'])
df_train.head()

df_test['label'].unique()

le = LabelEncoder()
df_test['label'] = le.fit_transform(df_test['label'])
df_test['label_text'] = le.inverse_transform(df_test['label'])
df_test.head()

df_val['label'].unique()

le = LabelEncoder()
df_val['label'] = le.fit_transform(df_val['label'])
df_val['label_text'] = le.inverse_transform(df_val['label'])
df_val.head()

print(df_train)

print (le.classes_)

train_sentences = df_train.sentence.values
train_labels = df_train.label.values


val_sentences = df_val.sentence.values
val_labels = df_val.label.values


test_sentences = df_test.sentence.values
test_labels = df_test.label.values

MAX_LEN = 256

tokenizer = DistilBertTokenizer.from_pretrained('distilbert-base-uncased',do_lower_case=True)
input_ids = [tokenizer.encode(sent, add_special_tokens=True,max_length=MAX_LEN,pad_to_max_length=True,truncation=True) for sent in train_sentences]

attention_masks = []
## Create a mask of 1 for all input tokens and 0 for all padding tokens
attention_masks = [[float(i>0) for i in seq] for seq in input_ids]
print(attention_masks[2])



print("Distribution of data based on labels: ",df_train.label.value_counts())
print("Distribution of data based on labels: ",df_train.sentence.values)



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
        return self.tokenizer(idx, return_tensors = 'pt').to(DEVICE)

    
train_dataset = DataLoader(train_sentences, train_labels, tokenizer, MAX_LEN)
val_dataset = DataLoader(val_sentences, val_labels, tokenizer, MAX_LEN)
test_dataset = DataLoader(test_sentences, test_labels, tokenizer, MAX_LEN)

print (train_dataset.__getitem__(0))

f1 = datasets.load_metric('f1')
accuracy = datasets.load_metric('accuracy')
precision = datasets.load_metric('precision')
recall = datasets.load_metric('recall')
def compute_metrics(eval_pred):
    metrics_dict = {}
    predictions, labels = eval_pred
    predictions = np.argmax(predictions, axis=1)
    
    metrics_dict.update(f1.compute(predictions = predictions, references = labels, average = 'macro'))
    metrics_dict.update(accuracy.compute(predictions = predictions, references = labels))
    metrics_dict.update(precision.compute(predictions = predictions, references = labels, average = 'macro'))
    metrics_dict.update(recall.compute(predictions = predictions, references = labels, average = 'macro'))
    return metrics_dict

id2label = {idx:label for idx, label in enumerate(le.classes_)}
label2id = {label:idx for idx, label in enumerate(le.classes_)}
config = AutoConfig.from_pretrained('distilbert-base-uncased',
                                    num_labels = 51,
                                    id2label = id2label,
                                    label2id = label2id)
model = AutoModelForSequenceClassification.from_config(config)

model = model.to(DEVICE)

print (config)

training_args = TrainingArguments(
    output_dir='C:/Users/kidsp/Downloads/fyp/kaggle/working/results',
    num_train_epochs=10,
    per_device_train_batch_size=64,
    per_device_eval_batch_size=64,
    warmup_steps=500,
    weight_decay=0.05,
    report_to='none',
    evaluation_strategy='steps',
    logging_dir='C:/Users/kidsp/Downloads/fyp/kaggle/working/logs',
    logging_steps=50)

trainer = Trainer(
    model=model,
    args=training_args,
    train_dataset=train_dataset,
    eval_dataset=val_dataset,
    compute_metrics=compute_metrics)    

trainer.train()

trainer.save_model('C:/Users/kidsp/Downloads/fyp/kaggle/working/prayer_model')
