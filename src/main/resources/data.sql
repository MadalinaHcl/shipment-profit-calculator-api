-- ==========================================================================
-- Shipment Profit Calculator - Seed data (3-4 records per entity)
-- ==========================================================================

INSERT INTO shipment (shipment_code) VALUES ('0001');
INSERT INTO shipment (shipment_code) VALUES ('0002');
INSERT INTO shipment (shipment_code) VALUES ('0003');
INSERT INTO shipment (shipment_code) VALUES ('0004');

INSERT INTO income (shipment_id, amount, description) VALUES (1, 1000.00, 'Customer payment - shipment 0001');
INSERT INTO income (shipment_id, amount, description) VALUES (2, 500.00,  'Customer payment - shipment 0002');
INSERT INTO income (shipment_id, amount, description) VALUES (3, 230.50, 'Customer payment - shipment 0003');
INSERT INTO income (shipment_id, amount, description) VALUES (4, 750.00, 'Customer payment - shipment 0004');

INSERT INTO cost (shipment_id, cost_type, amount, description) VALUES (1, 'BASE',       200.00, 'Main carriage cost');
INSERT INTO cost (shipment_id, cost_type, amount, description) VALUES (2, 'BASE',       900.00, 'Main carriage cost');
INSERT INTO cost (shipment_id, cost_type, amount, description) VALUES (3, 'BASE',       200.00, 'Main carriage cost');
INSERT INTO cost (shipment_id, cost_type, amount, description) VALUES (3, 'ADDITIONAL', 0.00,   'No additional cost');
INSERT INTO cost (shipment_id, cost_type, amount, description) VALUES (4, 'BASE',       300.00, 'Main carriage cost');
INSERT INTO cost (shipment_id, cost_type, amount, description) VALUES (4, 'ADDITIONAL', 50.00,  'Customs handling fee');

INSERT INTO profit_loss (shipment_id, total_income, total_costs, profit_or_loss) VALUES (1, 1000.00, 200.00, 800.00);
INSERT INTO profit_loss (shipment_id, total_income, total_costs, profit_or_loss) VALUES (2, 500.00,  900.00, -400.00);
INSERT INTO profit_loss (shipment_id, total_income, total_costs, profit_or_loss) VALUES (3, 230.50,  200.00, 30.50);
INSERT INTO profit_loss (shipment_id, total_income, total_costs, profit_or_loss) VALUES (4, 750.00,  350.00, 400.00);
