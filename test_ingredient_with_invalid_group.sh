#!/bin/bash

# Test script for ingredient creation with an invalid group ID
# This tests the fix for the NoSuchElementException issue

echo "Testing ingredient creation with an invalid group ID..."

# Try to create an ingredient with a non-existent group ID (using 999999)
RESPONSE=$(curl -s -X POST -H "Content-Type: application/json" -d '{
  "name": "Test Ingredient",
  "description": "Ingredient with invalid group ID",
  "unit": "kg",
  "active": true,
  "groupId": 999999
}' http://localhost:8080/api/ingredients)

echo "Response from server:"
echo $RESPONSE

# Print the full response for debugging
echo "Full response:"
echo "$RESPONSE" | jq '.' 2>/dev/null || echo "$RESPONSE"

# Check if the response contains our custom error message
if echo $RESPONSE | grep -q "Ingredient group not found with ID: 999999"; then
  echo "TEST PASSED: Server correctly returned a descriptive error message"
else
  echo "TEST FAILED: Server did not return the expected error message"
fi

echo "Test completed."