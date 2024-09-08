package com.task06;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.RetentionSetting;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;


import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@LambdaHandler(
    lambdaName = "audit_producer",
	roleName = "audit_producer-role",
	isPublishVersion = false,
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@DependsOn(name = "Audit", resourceType = com.syndicate.deployment.model.ResourceType.DYNAMODB_TABLE)
@EnvironmentVariables(value = {
    @EnvironmentVariable(key = "target_table", value = "${target_table}")
})


public class AuditProducer implements RequestHandler<DynamodbEvent, Void> {

	private final DynamoDB dynamoDB;
    private final ObjectMapper objectMapper;

    public AuditProducer() {
        this.dynamoDB = new DynamoDB(AmazonDynamoDBClientBuilder.defaultClient());
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public Void handleRequest(DynamodbEvent event, Context context) {
        String tableName = System.getenv("target_table");

        for (DynamodbEvent.DynamodbStreamRecord record : event.getRecords()) {
            try {
                // Get the DynamoDB change (new or old)
                Map<String, AttributeValue> newImage = record.getDynamodb().getNewImage();
                Map<String, AttributeValue> oldImage = record.getDynamodb().getOldImage();

                // Check event type
                if ("INSERT".equals(record.getEventName())) {
                    // New item inserted in 'Configuration' table
                    saveNewAuditEntry(newImage, tableName);
                } else if ("MODIFY".equals(record.getEventName())) {
                    // Existing item modified in 'Configuration' table
                    saveUpdatedAuditEntry(oldImage, newImage, tableName);
                }
            } catch (Exception e) {
                context.getLogger().log("Error processing DynamoDB event: " + e.getMessage());
            }
        }

        return null;
    }

    private void saveNewAuditEntry(Map<String, AttributeValue> newImage, String tableName) {
        // Extract data from newImage
        String id = UUID.randomUUID().toString();
        String itemKey = newImage.get("key").getS();
        String modificationTime = java.time.Instant.now().toString();

        // Create a new item for the Audit table
        Table auditTable = dynamoDB.getTable(tableName);
        Item auditItem = new Item()
            .withPrimaryKey("id", id)
            .withString("itemKey", itemKey)
            .withString("modificationTime", modificationTime)
            .withMap("newValue", convertImageToMap(newImage));

        // Save the item to the Audit table
        auditTable.putItem(auditItem);
    }

    private void saveUpdatedAuditEntry(Map<String, AttributeValue> oldImage, Map<String, AttributeValue> newImage, String tableName) {
        // Extract relevant data
        String id = UUID.randomUUID().toString();
        String itemKey = newImage.get("key").getS();
        String modificationTime = java.time.Instant.now().toString();
        String updatedAttribute = "value";  // Assuming "value" attribute changed

        // Extract the old and new values
        int oldValue = oldImage != null ? Integer.parseInt(oldImage.get("value").getN()) : 0;
        int newValue = newImage != null ? Integer.parseInt(newImage.get("value").getN()) : 0;

        // Create an audit item for the update
        Table auditTable = dynamoDB.getTable(tableName);
        Item auditItem = new Item()
            .withPrimaryKey("id", id)
            .withString("itemKey", itemKey)
            .withString("modificationTime", modificationTime)
            .withString("updatedAttribute", updatedAttribute)
            .withNumber("oldValue", oldValue)
            .withNumber("newValue", newValue);

        // Save the item to the Audit table
        auditTable.putItem(auditItem);
    }

    private Map<String, Object> convertImageToMap(Map<String, AttributeValue> image) {
        // Convert AttributeValue map to a standard Map
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String, AttributeValue> entry : image.entrySet()) {
            String key = entry.getKey();
            AttributeValue value = entry.getValue();
            if (value.getS() != null) {
                result.put(key, value.getS());
            } else if (value.getN() != null) {
                result.put(key, Integer.parseInt(value.getN()));
            } else if (value.getM() != null) {
                result.put(key, convertImageToMap(value.getM()));
            }
            // Handle other AttributeValue types as necessary
        }
        return result;
    }
}
