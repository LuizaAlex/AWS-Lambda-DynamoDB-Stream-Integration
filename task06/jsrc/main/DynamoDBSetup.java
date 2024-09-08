import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.CreateTableResult;
import com.amazonaws.services.dynamodbv2.model.UpdateTableRequest;
import com.amazonaws.services.dynamodbv2.model.UpdateTableResult;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.StreamSpecification;

public class DynamoDBSetup {
    public static void main(String[] args) {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
        
        // Define table schema
        CreateTableRequest createTableRequest = new CreateTableRequest()
            .withTableName("Configuration")
            .withKeySchema(new KeySchemaElement("key", "HASH"))
            .withAttributeDefinitions(new AttributeDefinition("key", "S"))
            .withProvisionedThroughput(new ProvisionedThroughput(1L, 1L));
        
        CreateTableResult createTableResult = client.createTable(createTableRequest);
        
        // Enable DynamoDB Stream
        UpdateTableRequest updateTableRequest = new UpdateTableRequest()
            .withTableName("Configuration")
            .withStreamSpecification(new StreamSpecification()
                .withStreamEnabled(true)
                .withStreamViewType("NEW_AND_OLD_IMAGES"));
        
        UpdateTableResult updateTableResult = client.updateTable(updateTableRequest);
        
        System.out.println("Table created and stream enabled.");
    }
}
