-- Drop the existing ingredient_components table if it exists
DROP TABLE IF EXISTS ingredient_components;

-- Create the ingredient_components table with the correct schema
CREATE TABLE ingredient_components (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_ingredient_id BIGINT NOT NULL,
    child_ingredient_id BIGINT NOT NULL,
    quantity DOUBLE,
    CONSTRAINT fk_parent_ingredient FOREIGN KEY (parent_ingredient_id) REFERENCES ingredient(id),
    CONSTRAINT fk_child_ingredient FOREIGN KEY (child_ingredient_id) REFERENCES ingredient(id)
);