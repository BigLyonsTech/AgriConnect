-- PostgreSQL does not index foreign keys or filter columns on its own.
-- Every tenant-scoped query filters on tenant_id, and the farm/crop/org
-- lookups below back the repository finder methods.

CREATE INDEX idx_farms_tenant          ON farms (tenant_id);
CREATE INDEX idx_crops_tenant_farm     ON crops (tenant_id, farm_id);
CREATE INDEX idx_livestock_tenant_farm ON livestock (tenant_id, farm_id);
CREATE INDEX idx_expenses_tenant_farm  ON expenses (tenant_id, farm_id);
CREATE INDEX idx_revenues_tenant_farm  ON revenues (tenant_id, farm_id);
CREATE INDEX idx_inventory_tenant_farm ON inventory_items (tenant_id, farm_id);
CREATE INDEX idx_yield_tenant_crop     ON yield_records (tenant_id, crop_id);

CREATE INDEX idx_users_organization    ON users (organization_id);
CREATE INDEX idx_listings_status       ON listings (status);
CREATE INDEX idx_listings_organization ON listings (organization_id);
CREATE INDEX idx_orders_buyer          ON orders (buyer_organization_id);
CREATE INDEX idx_orders_seller         ON orders (seller_organization_id);
CREATE INDEX idx_escrow_order          ON escrow_transactions (order_id);
CREATE INDEX idx_escrow_buyer          ON escrow_transactions (buyer_organization_id);
