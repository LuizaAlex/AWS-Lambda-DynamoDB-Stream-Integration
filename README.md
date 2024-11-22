# AWS-Lambda-DynamoDB-Stream-Integration

# DynamoDB Stream to Lambda Audit Function Application  

The DynamoDB Stream to Lambda Audit Function Application is designed to track changes in a `Configuration` table and log them in an `Audit` table. It leverages AWS Lambda and DynamoDB Streams to ensure seamless integration and efficient auditing of configuration changes.  

---

## Features  

- **Trigger-Based Auditing**: Automatically logs every change in the `Configuration` table using a DynamoDB Stream-triggered Lambda function.  
- **Real-Time Logging**: Tracks `CREATE`, `UPDATE`, and `DELETE` operations in the `Configuration` table and generates corresponding entries in the `Audit` table.  
- **Structured Audit Entries**: Captures detailed information, including old and new values, timestamps, and updated attributes, to maintain a comprehensive log.  
- **Serverless Architecture**: Fully serverless solution built using AWS Lambda and DynamoDB Streams.  

---



## App Flow  

### 1. DynamoDB Stream Setup  
- Enable a **DynamoDB Stream** on the `Configuration` table to capture all item-level changes.  
- Configure the stream to trigger the `audit_producer` Lambda function on any change.  

### 2. Lambda Function Logic  
- **Event Handling**: Processes events from the DynamoDB Stream.  
- **Audit Creation**: Parses the event, determines the change type (`CREATE`, `UPDATE`, `DELETE`), and creates a corresponding audit entry in the `Audit` table.  
- **Error Handling**: Includes retries and error logging to handle failures during event processing.  

---

## Resources Used  

### AWS Lambda  
- **Function**: `audit_producer`  
- **Runtime**: Java 11  
- **Trigger**: DynamoDB Stream  

### DynamoDB  
- **Table 1**: `Configuration`  
- **Table 2**: `Audit`  

---

## Project Setup  

### Prerequisites  
1. **AWS CLI**: Ensure the AWS CLI is installed and configured with appropriate credentials.  
2. **AWS Syndicate**: Install AWS Syndicate for project generation and deployment.  
3. **Java Environment**: Set up a Java 11 environment for Lambda development.  
