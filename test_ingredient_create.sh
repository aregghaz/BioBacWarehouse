#!/bin/bash

# Test script for ingredient creation functionality

echo "Testing ingredient creation with child ingredients..."

# Create a parent ingredient
PARENT_RESPONSE=$(curl -s -X POST -H "Content-Type: application/json" -d '{
  "name": "Parent Ingredient",
  "description": "A parent ingredient for testing",
  "unit": "kg",
  "active": true
}' http://localhost:8080/api/ingredients)

echo "Parent ingredient created:"
echo $PARENT_RESPONSE

# Extract parent ID
PARENT_ID=$(echo $PARENT_RESPONSE | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
echo "Parent ID: $PARENT_ID"

# Create child ingredients
CHILD1_RESPONSE=$(curl -s -X POST -H "Content-Type: application/json" -d '{
  "name": "Child Ingredient 1",
  "description": "A child ingredient for testing",
  "unit": "g",
  "active": true
}' http://localhost:8080/api/ingredients)

echo "Child ingredient 1 created:"
echo $CHILD1_RESPONSE

# Extract child1 ID
CHILD1_ID=$(echo $CHILD1_RESPONSE | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
echo "Child 1 ID: $CHILD1_ID"

CHILD2_RESPONSE=$(curl -s -X POST -H "Content-Type: application/json" -d '{
  "name": "Child Ingredient 2",
  "description": "Another child ingredient for testing",
  "unit": "g",
  "active": true
}' http://localhost:8080/api/ingredients)

echo "Child ingredient 2 created:"
echo $CHILD2_RESPONSE

# Extract child2 ID
CHILD2_ID=$(echo $CHILD2_RESPONSE | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
echo "Child 2 ID: $CHILD2_ID"

# Update parent to include children
UPDATE_RESPONSE=$(curl -s -X PUT -H "Content-Type: application/json" -d "{
  \"name\": \"Parent Ingredient Updated\",
  \"description\": \"A parent ingredient for testing\",
  \"unit\": \"kg\",
  \"active\": true,
  \"childIngredientIds\": [$CHILD1_ID, $CHILD2_ID]
}" http://localhost:8080/api/ingredients/$PARENT_ID)

echo "Parent updated with children:"
echo $UPDATE_RESPONSE

# Get the parent to verify children are included
GET_RESPONSE=$(curl -s -X GET http://localhost:8080/api/ingredients/$PARENT_ID)
echo "Get parent with children:"
echo $GET_RESPONSE

# Create a new ingredient with children directly
NEW_PARENT_RESPONSE=$(curl -s -X POST -H "Content-Type: application/json" -d "{
  \"name\": \"New Parent With Children\",
  \"description\": \"A new parent ingredient with children\",
  \"unit\": \"kg\",
  \"active\": true,
  \"childIngredientIds\": [$CHILD1_ID, $CHILD2_ID]
}" http://localhost:8080/api/ingredients)

echo "New parent created with children:"
echo $NEW_PARENT_RESPONSE

# Extract new parent ID
NEW_PARENT_ID=$(echo $NEW_PARENT_RESPONSE | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
echo "New Parent ID: $NEW_PARENT_ID"

# Get the new parent to verify children are included
NEW_GET_RESPONSE=$(curl -s -X GET http://localhost:8080/api/ingredients/$NEW_PARENT_ID)
echo "Get new parent with children:"
echo $NEW_GET_RESPONSE

echo "Test completed."