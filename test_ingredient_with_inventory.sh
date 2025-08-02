#!/bin/bash

# Test script for ingredient creation with inventory

echo "Testing ingredient creation with inventory..."

# First, get a warehouse ID to use
WAREHOUSE_RESPONSE=$(curl -s -X GET http://localhost:8080/api/warehouses)
echo "Available warehouses:"
echo $WAREHOUSE_RESPONSE

# Extract the first warehouse ID
WAREHOUSE_ID=$(echo $WAREHOUSE_RESPONSE | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
echo "Using Warehouse ID: $WAREHOUSE_ID"

# Create an ingredient with initial quantity and warehouse ID
INGREDIENT_RESPONSE=$(curl -s -X POST -H "Content-Type: application/json" -d "{
  \"name\": \"Test Ingredient With Inventory\",
  \"description\": \"An ingredient with initial inventory\",
  \"unit\": \"kg\",
  \"active\": true,
  \"initialQuantity\": 100,
  \"warehouseId\": $WAREHOUSE_ID
}" http://localhost:8080/api/ingredients)

echo "Ingredient created with inventory:"
echo $INGREDIENT_RESPONSE

# Extract ingredient ID
INGREDIENT_ID=$(echo $INGREDIENT_RESPONSE | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
echo "Ingredient ID: $INGREDIENT_ID"

# Get the inventory items for this ingredient to verify
INVENTORY_RESPONSE=$(curl -s -X GET http://localhost:8080/api/inventory/ingredient/$INGREDIENT_ID)
echo "Inventory for ingredient:"
echo $INVENTORY_RESPONSE

echo "Test completed."