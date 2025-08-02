#!/bin/bash

# Base URL for the API
BASE_URL="http://localhost:8080/api"

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo "Testing RecipeItem API endpoints..."

# 1. Get all recipe items
echo -e "\n${GREEN}Getting all recipe items:${NC}"
curl -s -X GET $BASE_URL/recipe-items | jq

# 2. Get recipe items by product ID (assuming product ID 1 exists)
echo -e "\n${GREEN}Getting recipe items by product ID 1:${NC}"
curl -s -X GET $BASE_URL/recipe-items/product/1 | jq

# 3. Get recipe items by ingredient ID (assuming ingredient ID 1 exists)
echo -e "\n${GREEN}Getting recipe items by ingredient ID 1:${NC}"
curl -s -X GET $BASE_URL/recipe-items/ingredient/1 | jq

# 4. Create a new recipe item for product ID 1
echo -e "\n${GREEN}Creating a new recipe item for product ID 1:${NC}"
curl -s -X POST $BASE_URL/recipe-items/product/1 \
  -H "Content-Type: application/json" \
  -d '{
    "ingredientId": 1,
    "quantity": 2.5,
    "notes": "Test recipe item"
  }' | jq

# 5. Get the newly created recipe item (assuming it has ID 1)
echo -e "\n${GREEN}Getting recipe item with ID 1:${NC}"
curl -s -X GET $BASE_URL/recipe-items/1 | jq

# 6. Update the recipe item
echo -e "\n${GREEN}Updating recipe item with ID 1:${NC}"
curl -s -X PUT $BASE_URL/recipe-items/1 \
  -H "Content-Type: application/json" \
  -d '{
    "ingredientId": 1,
    "quantity": 3.0,
    "notes": "Updated test recipe item"
  }' | jq

# 7. Delete the recipe item
echo -e "\n${GREEN}Deleting recipe item with ID 1:${NC}"
curl -s -X DELETE $BASE_URL/recipe-items/1

echo -e "\n${GREEN}Test completed!${NC}"