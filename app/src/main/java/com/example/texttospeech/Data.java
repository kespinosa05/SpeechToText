package com.example.texttospeech;

import org.apache.commons.text.similarity.LevenshteinResults;

public class Data {

    private String text;
    private String recognizedText;
    private Float confidence;
    private LevenshteinResults result;

    public Data(String text) {
        this.text = text;
    }

    public String getRecognizedText() {
        return recognizedText;
    }

    public void setRecognizedText(String recognizedText) {
        this.recognizedText = recognizedText;
    }

    public Float getConfidence() {
        return confidence;
    }

    public void setConfidence(Float confidence) {
        this.confidence = confidence;
    }

    public Data() {
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public LevenshteinResults getResult() {
        return result;
    }

    public void setResult(LevenshteinResults result) {
        this.result = result;
    }
}
