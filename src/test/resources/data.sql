INSERT INTO courses (id, title, description, created_at, updated_at)
VALUES
    ('11111111-1111-1111-1111-111111111111', 'Minecraft Coding', 'Weekly coding class for beginners.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('22222222-2222-2222-2222-222222222222', 'Roblox Game Design', 'Game design camp with project-based sessions.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('33333333-3333-3333-3333-333333333333', 'Public Speaking', 'Confidence building live class.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO teachers (id, name, email, time_zone, created_at, updated_at)
VALUES
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Asha Mehta', 'asha@example.com', 'Asia/Kolkata', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'John Carter', 'john@example.com', 'America/New_York', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO parents (id, name, email, time_zone, created_at, updated_at)
VALUES
    ('cccccccc-cccc-cccc-cccc-cccccccccccc', 'Priya Sharma', 'priya@example.com', 'Asia/Kolkata', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('dddddddd-dddd-dddd-dddd-dddddddddddd', 'Emma Wilson', 'emma@example.com', 'Europe/London', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO offerings (id, course_id, teacher_id, title, description, teacher_time_zone, status, created_at, updated_at)
VALUES
    ('44444444-4444-4444-4444-444444444444', '11111111-1111-1111-1111-111111111111', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Saturday Batch', 'Eight week weekend batch.', 'Asia/Kolkata', 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('55555555-5555-5555-5555-555555555555', '22222222-2222-2222-2222-222222222222', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Overlapping Game Design Batch', 'Used to verify overlap rejection.', 'Asia/Kolkata', 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('66666666-6666-6666-6666-666666666666', '33333333-3333-3333-3333-333333333333', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Non Overlapping Speaking Batch', 'Used to verify successful second booking.', 'America/New_York', 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO sessions (id, offering_id, teacher_id, start_at_utc, end_at_utc, source_time_zone, created_at)
VALUES
    ('77777777-7777-7777-7777-777777777771', '44444444-4444-4444-4444-444444444444', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', TIMESTAMP WITH TIME ZONE '2026-06-06 12:30:00+00', TIMESTAMP WITH TIME ZONE '2026-06-06 13:30:00+00', 'Asia/Kolkata', CURRENT_TIMESTAMP),
    ('77777777-7777-7777-7777-777777777772', '44444444-4444-4444-4444-444444444444', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', TIMESTAMP WITH TIME ZONE '2026-06-13 12:30:00+00', TIMESTAMP WITH TIME ZONE '2026-06-13 13:30:00+00', 'Asia/Kolkata', CURRENT_TIMESTAMP),
    ('88888888-8888-8888-8888-888888888881', '55555555-5555-5555-5555-555555555555', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', TIMESTAMP WITH TIME ZONE '2026-06-13 13:00:00+00', TIMESTAMP WITH TIME ZONE '2026-06-13 14:00:00+00', 'Asia/Kolkata', CURRENT_TIMESTAMP),
    ('99999999-9999-9999-9999-999999999991', '66666666-6666-6666-6666-666666666666', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', TIMESTAMP WITH TIME ZONE '2026-06-09 21:00:00+00', TIMESTAMP WITH TIME ZONE '2026-06-09 22:00:00+00', 'America/New_York', CURRENT_TIMESTAMP);

INSERT INTO bookings (id, parent_id, offering_id, status, booked_at)
VALUES
    ('abababab-abab-abab-abab-abababababab', 'cccccccc-cccc-cccc-cccc-cccccccccccc', '44444444-4444-4444-4444-444444444444', 'CONFIRMED', CURRENT_TIMESTAMP);
