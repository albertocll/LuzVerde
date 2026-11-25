-- El historial de evaluaciones y de CV se lista siempre por usuario.
CREATE INDEX IF NOT EXISTS idx_evaluaciones_usuario ON evaluaciones (usuario_id, creado_en);
CREATE INDEX IF NOT EXISTS idx_cvs_usuario ON cvs (usuario_id, creado_en);
