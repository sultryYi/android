package com.example.sliding;

import java.util.Objects;

public class Question implements Comparable<Question>{
    public int questionId;
    public String questionClassId;
    public String questionContent;
    public int agreeNum;
    public int disagreeNum;
    public String publishTime;

    public Question(int questionId, String questionClassId, String questionContent, int agreeNum, int disagreeNum, String publishTime) {
        this.questionId = questionId;
        this.questionClassId = questionClassId;
        this.questionContent = questionContent;
        this.agreeNum = agreeNum;
        this.disagreeNum = disagreeNum;
        this.publishTime = publishTime;
    }

    @Override
    public int compareTo(Question question) {
        if(this.questionId>=question.questionId){
            return 1;
        }else
            return -1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Question question = (Question) o;
        return questionId == question.questionId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(questionId);
    }
}
