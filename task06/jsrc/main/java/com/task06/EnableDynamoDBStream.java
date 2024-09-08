package com.task06;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.StreamSpecification;
import com.amazonaws.services.dynamodbv2.model.UpdateTableRequest;
import com.amazonaws.services.dynamodbv2.model.UpdateTableResult;

public class EnableDynamoDBStream {

    public static void main(String[] args) {
        // Creează clientul DynamoDB
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();

        // Creează specificațiile pentru stream
        StreamSpecification streamSpec = new StreamSpecification()
            .withStreamEnabled(true)
            .withStreamViewType("NEW_AND_OLD_IMAGES");

        // Creează cererea pentru actualizarea tabelului
        UpdateTableRequest request = new UpdateTableRequest()
            .withTableName("Configuration")
            .withStreamSpecification(streamSpec);

        // Actualizează tabela
        UpdateTableResult result = client.updateTable(request);

        System.out.println("Stream enabled: " + result);
    }
}
