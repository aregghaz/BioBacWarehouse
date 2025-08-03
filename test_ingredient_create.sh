#!/bin/bash

# Test script for ingredient creation functionality with many-to-many relationships

echo "Testing ingredient creation with many-to-many relationships..."

# Create ingredient 1
INGREDIENT1_RESPONSE=$(curl -s -X POST -H "Content-Type: application/json" -d '{
  "name": "Ingredient 1",
  "description": "First ingredient for testing",
  "unit": "kg",
  "active": true
}' http://localhost:8080/api/ingredients)

echo "Ingredient 1 created:"
echo $INGREDIENT1_RESPONSE

# Extract ingredient1 ID
INGREDIENT1_ID=$(echo $INGREDIENT1_RESPONSE | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
echo "Ingredient 1 ID: $INGREDIENT1_ID"

# Create ingredient 2
INGREDIENT2_RESPONSE=$(curl -s -X POST -H "Content-Type: application/json" -d '{
  "name": "Ingredient 2",
  "description": "Second ingredient for testing",
  "unit": "g",
  "active": true
}' http://localhost:8080/api/ingredients)

echo "Ingredient 2 created:"
echo $INGREDIENT2_RESPONSE

# Extract ingredient2 ID
INGREDIENT2_ID=$(echo $INGREDIENT2_RESPONSE | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
echo "Ingredient 2 ID: $INGREDIENT2_ID"

# Create ingredient 3
INGREDIENT3_RESPONSE=$(curl -s -X POST -H "Content-Type: application/json" -d '{
  "name": "Ingredient 3",
  "description": "Third ingredient for testing",
  "unit": "g",
  "active": true
}' http://localhost:8080/api/ingredients)

echo "Ingredient 3 created:"
echo $INGREDIENT3_RESPONSE

# Extract ingredient3 ID
INGREDIENT3_ID=$(echo $INGREDIENT3_RESPONSE | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
echo "Ingredient 3 ID: $INGREDIENT3_ID"

# Update ingredient 1 to include ingredients 2 and 3 as children with quantities
UPDATE1_RESPONSE=$(curl -s -X PUT -H "Content-Type: application/json" -d "{
  \"name\": \"Ingredient 1 Updated\",
  \"description\": \"First ingredient for testing\",
  \"unit\": \"kg\",
  \"active\": true,
  \"childIngredientComponents\": [
    {
      \"childIngredientId\": $INGREDIENT2_ID,
      \"quantity\": 2.5
    },
    {
      \"childIngredientId\": $INGREDIENT3_ID,
      \"quantity\": 1.75
    }
  ]
}" http://localhost:8080/api/ingredients/$INGREDIENT1_ID)

echo "Ingredient 1 updated with children 2 and 3:"
echo $UPDATE1_RESPONSE

# Get ingredient 1 to verify children are included
GET1_RESPONSE=$(curl -s -X GET http://localhost:8080/api/ingredients/$INGREDIENT1_ID)
echo "Get ingredient 1 with children:"
echo $GET1_RESPONSE

# Update ingredient 2 to include ingredient 3 as a child with quantity
UPDATE2_RESPONSE=$(curl -s -X PUT -H "Content-Type: application/json" -d "{
  \"name\": \"Ingredient 2 Updated\",
  \"description\": \"Second ingredient for testing\",
  \"unit\": \"g\",
  \"active\": true,
  \"childIngredientComponents\": [
    {
      \"childIngredientId\": $INGREDIENT3_ID,
      \"quantity\": 3.0
    }
  ]
}" http://localhost:8080/api/ingredients/$INGREDIENT2_ID)

echo "Ingredient 2 updated with child 3:"
echo $UPDATE2_RESPONSE

# Get ingredient 2 to verify children are included
GET2_RESPONSE=$(curl -s -X GET http://localhost:8080/api/ingredients/$INGREDIENT2_ID)
echo "Get ingredient 2 with children:"
echo $GET2_RESPONSE

# Get ingredient 3 to verify it has multiple parents
GET3_RESPONSE=$(curl -s -X GET http://localhost:8080/api/ingredients/$INGREDIENT3_ID)
echo "Get ingredient 3 with multiple parents:"
echo $GET3_RESPONSE

# Create a new ingredient with children and quantities directly
NEW_INGREDIENT_RESPONSE=$(curl -s -X POST -H "Content-Type: application/json" -d "{
  \"name\": \"New Ingredient With Children\",
  \"description\": \"A new ingredient with children\",
  \"unit\": \"kg\",
  \"active\": true,
  \"childIngredientComponents\": [
    {
      \"childIngredientId\": $INGREDIENT2_ID,
      \"quantity\": 5.0
    },
    {
      \"childIngredientId\": $INGREDIENT3_ID,
      \"quantity\": 2.5
    }
  ]
}" http://localhost:8080/api/ingredients)

echo "New ingredient created with children:"
echo $NEW_INGREDIENT_RESPONSE

# Extract new ingredient ID
NEW_INGREDIENT_ID=$(echo $NEW_INGREDIENT_RESPONSE | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
echo "New Ingredient ID: $NEW_INGREDIENT_ID"

# Get the new ingredient to verify children are included
NEW_GET_RESPONSE=$(curl -s -X GET http://localhost:8080/api/ingredients/$NEW_INGREDIENT_ID)
echo "Get new ingredient with children:"
echo $NEW_GET_RESPONSE

# Get ingredient 2 again to verify it now has multiple parents
GET2_AGAIN_RESPONSE=$(curl -s -X GET http://localhost:8080/api/ingredients/$INGREDIENT2_ID)
echo "Get ingredient 2 with multiple parents:"
echo $GET2_AGAIN_RESPONSE

echo "Test completed."