-- Хэш-индекс для clinic
CREATE INDEX idx_dental_work_clinic_hash
ON dental_lab.dental_work USING HASH (clinic);

-- Хэш-индекс для patient
CREATE INDEX idx_dental_work_patient_hash
ON dental_lab.dental_work USING HASH (patient);

-- B-tree индекс для complete_at
CREATE INDEX idx_dental_work_complete_at
ON dental_lab.dental_work (complete_at);

-- B-tree индекс для связи фото и работы
CREATE INDEX idx_photo_filename_dental_work_id
ON dental_lab.photo_filename (dental_work_id);
