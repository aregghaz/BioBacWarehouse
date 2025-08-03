#!/bin/bash

# Test script for ingredient creation with inventory reduction functionality

echo "Testing ingredient creation with inventory reduction..."

# Create a warehouse
WAREHOUSE_RESPONSE=$(curl -s -X POST -H "Content-Type: application/json" -d '{
  "name": "Test Warehouse",
  "location": "Test Location"
}' http://localhost:8080/api/warehouses)

echo "Warehouse created:"
echo $WAREHOUSE_RESPONSE

# Extract warehouse ID
WAREHOUSE_ID=$(echo $WAREHOUSE_RESPONSE | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
echo "Warehouse ID: $WAREHOUSE_ID"

# Create base ingredients
INGREDIENT1_RESPONSE=$(curl -s -X POST -H "Content-Type: application/json" -d '{
  "name": "Base Ingredient 1",
  "description": "First base ingredient",
  "unit": "kg",
  "active": true
}' http://localhost:8080/api/ingredients)

echo "Base Ingredient 1 created:"
echo $INGREDIENT1_RESPONSE

# Extract ingredient1 ID
INGREDIENT1_ID=$(echo $INGREDIENT1_RESPONSE | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
echo "Base Ingredient 1 ID: $INGREDIENT1_ID"

INGREDIENT2_RESPONSE=$(curl -s -X POST -H "Content-Type: application/json" -d '{
  "name": "Base Ingredient 2",
  "description": "Second base ingredient",
  "unit": "g",
  "active": true
}' http://localhost:8080/api/ingredients)

echo "Base Ingredient 2 created:"
echo $INGREDIENT2_RESPONSE

# Extract ingredient2 ID
INGREDIENT2_ID=$(echo $INGREDIENT2_RESPONSE | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
echo "Base Ingredient 2 ID: $INGREDIENT2_ID"

# Create inventory items for base ingredients
INVENTORY1_RESPONSE=$(curl -s -X POST -H "Content-Type: application/json" -d "{
  \"ingredientId\": $INGREDIENT1_ID,
  \"warehouseId\": $WAREHOUSE_ID,
  \"quantity\": 100,
  \"lastUpdated\": \"2025-08-02\"
}" http://localhost:8080/api/inventory)

echo "Inventory for Base Ingredient 1 created:"
echo $INVENTORY1_RESPONSE

# Extract inventory1 ID
INVENTORY1_ID=$(echo $INVENTORY1_RESPONSE | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
echo "Inventory 1 ID: $INVENTORY1_ID"

INVENTORY2_RESPONSE=$(curl -s -X POST -H "Content-Type: application/json" -d "{
  \"ingredientId\": $INGREDIENT2_ID,
  \"warehouseId\": $WAREHOUSE_ID,
  \"quantity\": 500,
  \"lastUpdated\": \"2025-08-02\"
}" http://localhost:8080/api/inventory)

echo "Inventory for Base Ingredient 2 created:"
echo $INVENTORY2_RESPONSE

# Extract inventory2 ID
INVENTORY2_ID=$(echo $INVENTORY2_RESPONSE | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
echo "Inventory 2 ID: $INVENTORY2_ID"

# Check initial inventory levels
echo "Initial inventory levels:"
INVENTORY1_GET=$(curl -s -X GET http://localhost:8080/api/inventory/$INVENTORY1_ID)
echo "Base Ingredient 1 inventory: $INVENTORY1_GET"

INVENTORY2_GET=$(curl -s -X GET http://localhost:8080/api/inventory/$INVENTORY2_ID)
echo "Base Ingredient 2 inventory: $INVENTORY2_GET"

# Create a compound ingredient using the base ingredients
echo "Creating compound ingredient..."
COMPOUND_RESPONSE=$(curl -s -X POST -H "Content-Type: application/json" -d "{
  \"name\": \"Compound Ingredient\",
  \"description\": \"A compound ingredient made from base ingredients\",
  \"unit\": \"kg\",
  \"active\": true,
  \"initialQuantity\": 10,
  \"warehouseId\": $WAREHOUSE_ID,
  \"childIngredientComponents\": [
    {
      \"childIngredientId\": $INGREDIENT1_ID,
      \"quantity\": 2.0
    },
    {
      \"childIngredientId\": $INGREDIENT2_ID,
      \"quantity\": 5.0
    }
  ]
}" http://localhost:8080/api/ingredients)

echo "Compound Ingredient created:"
echo $COMPOUND_RESPONSE

# Extract compound ingredient ID
COMPOUND_ID=$(echo $COMPOUND_RESPONSE | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
echo "Compound Ingredient ID: $COMPOUND_ID"

# Check updated inventory levels (should be reduced)
echo "Updated inventory levels after compound ingredient creation:"
INVENTORY1_GET_AFTER=$(curl -s -X GET http://localhost:8080/api/inventory/$INVENTORY1_ID)
echo "Base Ingredient 1 inventory after: $INVENTORY1_GET_AFTER"

INVENTORY2_GET_AFTER=$(curl -s -X GET http://localhost:8080/api/inventory/$INVENTORY2_ID)
echo "Base Ingredient 2 inventory after: $INVENTORY2_GET_AFTER"

# Get the inventory for the compound ingredient
COMPOUND_INVENTORY=$(curl -s -X GET http://localhost:8080/api/inventory/ingredient/$COMPOUND_ID)
echo "Compound Ingredient inventory: $COMPOUND_INVENTORY"

# Check if the base ingredients' inventory was reduced
echo "Checking if base ingredients' inventory was reduced..."
if [[ $INVENTORY1_GET == *"\"quantity\":100"* && $INVENTORY1_GET_AFTER == *"\"quantity\":100"* ]]; then
  echo "ERROR: Base Ingredient 1 inventory was not reduced"
else
  echo "SUCCESS: Base Ingredient 1 inventory was reduced"
fi

if [[ $INVENTORY2_GET == *"\"quantity\":500"* && $INVENTORY2_GET_AFTER == *"\"quantity\":500"* ]]; then
  echo "ERROR: Base Ingredient 2 inventory was not reduced"
else
  echo "SUCCESS: Base Ingredient 2 inventory was reduced"
fi

echo "Test completed."