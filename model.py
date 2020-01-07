import torch
import torch.nn as nn
import torchvision.models as models


class EncoderCNN(nn.Module):
    def __init__(self, embed_size):
        super(EncoderCNN, self).__init__()
        resnet = models.resnet50(pretrained=True)
        for param in resnet.parameters():
            param.requires_grad_(False)
        
        modules = list(resnet.children())[:-1]
        self.resnet = nn.Sequential(*modules)
        self.embed = nn.Linear(resnet.fc.in_features, embed_size)

    def forward(self, images):
        features = self.resnet(images)
        features = features.view(features.size(0), -1)
        features = self.embed(features)
        return features
    

class DecoderRNN(nn.Module):
    def __init__(self, embed_size, hidden_size, vocab_size, num_layers=1):
        super().__init__()
        self.embed_size, self.hidden_size, self.vocab_size, self.num_layers = embed_size, hidden_size, vocab_size, num_layers
        self.embed = nn.Embedding(self.vocab_size, self.embed_size)
        self.lstm = nn.LSTM(self.embed_size, self.hidden_size)
        self.fc = nn.Linear(self.hidden_size, self.vocab_size)
        self.sample_outputs = []
    
    def forward(self, features, captions):
        caption_length = captions.size()[1]
        batch_size = len(captions)
        
        if len(features.size()) == 2:
            features = features.unsqueeze(1)
        assert len(features.size()) == 3, "Image input features must be a rank 3 tensor." 
         
        # Drop the last word from captions 
        # it does not contribute towards prediction
        captions = captions[:,:-1]
        
        # Embed the captions
        embeddings = self.embed(captions)
        
        # Add the image features to captions as a new column at the beginning of the sequence to kick of the joint training
        joint_embeddings = torch.cat((features, embeddings), dim=1)
        assert joint_embeddings.size()[1] == caption_length, "Incorrect joint input dimension size"
        
        lstm_out, _ = self.lstm(joint_embeddings)
        outputs = self.fc(lstm_out.view(-1, self.hidden_size))
        
        # reshape the output to (batch_size, caption_length, vocab_size)
        return outputs.view(batch_size, -1, self.vocab_size)
        
    def sample(self, inputs, states=None, max_len=20):
        " accepts pre-processed image tensor (inputs) and returns predicted sentence (list of tensor ids of length max_len) "
        sample_outputs = []
        
#         if len(inputs.size()) == 2:
#             inputs = inputs.unsqueeze(1)
#         assert len(inputs.size()) == 3, "Image input features must be a rank 3 tensor." 
        
#         # Pass the image through our trained network for get the first word
#         lstm_out, hidden = self.lstm(inputs)
        
#         # Get the scores for our vocabulary
#         fc_out = self.fc(lstm_out.squeeze(1))
        
#         # Get the max of our scores which is our predicted next word
#         output = fc_out.max(dim=1)[1]
#         sample_outputs.append(output.item())
        
#         # Iterate using the about start word upto max_len or stop word to get our predicted caption
#         inputs = output.unsqueeze(1)
        hidden = states
        for idx in range(max_len):
#             embed_out = self.embed(inputs)
#             print(inputs.shape)
            lstm_out, hidden = self.lstm(inputs, hidden)
            fc_out = self.fc(lstm_out.squeeze(1))
            output = fc_out.max(dim=1)[1]
            sample_outputs.append(output.item())
            
            if output.item() == 1:
                return sample_outputs
            
            # Make current output our next input
            inputs = self.embed(output.unsqueeze(1))
        return sample_outputs