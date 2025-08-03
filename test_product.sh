#!/bin/bash

# Test script for product functionality

echo "Testing product creation and retrieval..."

# Create a product
PRODUCT_RESPONSE=$(curl -s -X POST -H "Content-Type: application/json" -d '{
  "name": "Test Product",
  "description": "A test product",
  "sku": "TEST-001"
}' http://localhost:8080/api/products)

echo "Product created:"
echo $PRODUCT_RESPONSE

# Extract product ID
PRODUCT_ID=$(echo $PRODUCT_RESPONSE | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
echo "Product ID: $PRODUCT_ID"

# Get the product to verify it was created correctly
GET_RESPONSE=$(curl -s -X GET http://localhost:8080/api/products/$PRODUCT_ID)
echo "Get product:"
echo $GET_RESPONSE

# Update the product
UPDATE_RESPONSE=$(curl -s -X PUT -H "Content-Type: application/json" -d "{
  \"name\": \"Updated Test Product\",
  \"description\": \"An updated test product\",
  \"sku\": \"TEST-001-UPDATED\"
}" http://localhost:8080/api/products/$PRODUCT_ID)

echo "Product updated:"
echo $UPDATE_RESPONSE

# Get the updated product
GET_UPDATED_RESPONSE=$(curl -s -X GET http://localhost:8080/api/products/$PRODUCT_ID)
echo "Get updated product:"
echo $GET_UPDATED_RESPONSE

echo "Test completed."