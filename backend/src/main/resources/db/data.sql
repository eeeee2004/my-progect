INSERT IGNORE INTO sys_user (username, password, real_name, phone, role_type) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', '13800000000', 'ADMIN'),
('guest1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '张三', '13800000001', 'GUEST'),
('guest2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '李四', '13800000002', 'GUEST');

INSERT IGNORE INTO employee (username, password, real_name, phone, role_type) VALUES
('front1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '前台小王', '13900000001', 'FRONT_DESK'),
('front2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '前台小李', '13900000002', 'FRONT_DESK');

INSERT IGNORE INTO room_type (name, bed_type, area, base_price, capacity, check_in_time, check_out_time, breakfast, window_type, floor_info, cancel_policy, description, facilities, sort_order) VALUES
('标准间', '双床', 25.00, 288.00, 2, '14:00', '12:00', '不含早', '有窗', '1-3层', '入住前24小时可免费取消，超时扣除首晚房费', '经济实惠的标准客房，配备两张单人床，适合朋友出行', 'WiFi,空调,电视,独立卫浴', 1),
('大床房', '大床', 30.00, 388.00, 2, '14:00', '12:00', '含双早', '有窗', '2-3层', '入住前24小时可免费取消，超时扣除首晚房费', '舒适的大床房，配备1.8米大床和迷你吧，适合情侣或商务出行', 'WiFi,空调,电视,独立卫浴,迷你吧,保险箱', 2),
('豪华套房', '大床', 50.00, 688.00, 3, '14:00', '13:00', '含双早', '落地窗', '3层', '入住前48小时可免费取消，超时扣除50%房费', '豪华套房，独立客厅+卧室，配备浴缸和全景落地窗，尽享尊贵体验', 'WiFi,空调,电视,独立卫浴,迷你吧,浴缸,客厅,保险箱,智能马桶', 3);

INSERT IGNORE INTO room (room_number, floor, room_type_id, status) VALUES
('101', 1, 1, 'AVAILABLE'),
('102', 1, 1, 'AVAILABLE'),
('103', 1, 2, 'AVAILABLE'),
('201', 2, 2, 'AVAILABLE'),
('202', 2, 2, 'AVAILABLE'),
('203', 2, 3, 'AVAILABLE'),
('301', 3, 3, 'AVAILABLE'),
('302', 3, 3, 'AVAILABLE');

INSERT IGNORE INTO price_calendar (room_type_id, rule_type, start_date, end_date, price_factor, priority, status) VALUES
(1, 'WEEKEND', '2026-01-01', '2030-12-31', 1.20, 10, 1),
(2, 'WEEKEND', '2026-01-01', '2030-12-31', 1.15, 10, 1),
(3, 'WEEKEND', '2026-01-01', '2030-12-31', 1.25, 10, 1),
(1, 'PROMOTION', '2026-06-01', '2026-06-30', 0.85, 20, 1);
