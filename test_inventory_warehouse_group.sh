#!/bin/bash

# Test script to verify warehouseId and groupId in inventory items

echo "Testing inventory with warehouseId and groupId..."

# Create a new inventory item with warehouseId and groupId
echo "Creating inventory item with warehouseId and groupId..."
RESPONSE=$(curl -s -X POST -H "Content-Type: application/json" -d '{
  "warehouseId": 1,
  "ingredientGroupId": 1,
  "quantity": 100,
  "lastUpdated": "2025-08-03"
}' http://localhost:8080/api/inventory)

echo "Response: $RESPONSE"

# Extract the ID from the response
ID=$(echo $RESPONSE | grep -o '"id":[0-9]*' | cut -d':' -f2)
echo "Created inventory item with ID: $ID"

# Get the inventory item by ID
echo "Getting inventory item by ID..."
curl -s -X GET http://localhost:8080/api/inventory/$ID

# Get inventory items by warehouseId
echo -e "\n\nGetting inventory items by warehouseId..."
curl -s -X GET http://localhost:8080/api/inventory/warehouse/1

# Get inventory items by groupId
echo -e "\n\nGetting inventory items by groupId..."
curl -s -X GET http://localhost:8080/api/inventory/group/1

# Update the inventory item
echo -e "\n\nUpdating inventory item..."
curl -s -X PUT -H "Content-Type: application/json" -d '{
  "warehouseId": 2,
  "ingredientGroupId": 2,
  "quantity": 200,
  "lastUpdated": "2025-08-04"
}' http://localhost:8080/api/inventory/$ID

# Get the updated inventory item
echo -e "\n\nGetting updated inventory item..."
curl -s -X GET http://localhost:8080/api/inventory/$ID

# Clean up - delete the inventory item
echo -e "\n\nDeleting inventory item..."
curl -s -X DELETE http://localhost:8080/api/inventory/$ID

echo -e "\n\nTest completed."