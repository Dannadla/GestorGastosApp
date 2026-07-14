const express = require('express');
const db = require('../db');
const authMiddleware = require('../middleware/authMiddleware');

const router = express.Router();

// Todas las rutas de gastos requieren un JWT válido
router.use(authMiddleware);

// GET /api/expenses -> lista los gastos/ingresos del usuario autenticado
router.get('/', (req, res) => {
  const rows = db
    .prepare('SELECT * FROM expenses WHERE user_id = ? ORDER BY date DESC')
    .all(req.userId);
  res.json(rows);
});

// GET /api/expenses/summary -> totales por categoría (para el dashboard)
router.get('/summary', (req, res) => {
  const byCategory = db
    .prepare(
      `SELECT category, SUM(amount) as total
       FROM expenses WHERE user_id = ? AND type = 'gasto'
       GROUP BY category`
    )
    .all(req.userId);

  const totals = db
    .prepare(
      `SELECT type, SUM(amount) as total
       FROM expenses WHERE user_id = ?
       GROUP BY type`
    )
    .all(req.userId);

  res.json({ byCategory, totals });
});

// POST /api/expenses -> crea un nuevo gasto/ingreso
router.post('/', (req, res) => {
  const { category, amount, description, type, date } = req.body;

  if (!category || amount === undefined || !date) {
    return res.status(400).json({ message: 'category, amount y date son requeridos' });
  }

  const info = db
    .prepare(
      `INSERT INTO expenses (user_id, category, amount, description, type, date)
       VALUES (?, ?, ?, ?, ?, ?)`
    )
    .run(req.userId, category, amount, description || '', type || 'gasto', date);

  const created = db.prepare('SELECT * FROM expenses WHERE id = ?').get(info.lastInsertRowid);
  res.status(201).json(created);
});

// PUT /api/expenses/:id -> reemplaza el registro completo
router.put('/:id', (req, res) => {
  const { category, amount, description, type, date } = req.body;
  const expense = db
    .prepare('SELECT * FROM expenses WHERE id = ? AND user_id = ?')
    .get(req.params.id, req.userId);

  if (!expense) {
    return res.status(404).json({ message: 'Registro no encontrado' });
  }

  db.prepare(
    `UPDATE expenses SET category = ?, amount = ?, description = ?, type = ?, date = ?
     WHERE id = ? AND user_id = ?`
  ).run(category, amount, description || '', type || 'gasto', date, req.params.id, req.userId);

  const updated = db.prepare('SELECT * FROM expenses WHERE id = ?').get(req.params.id);
  res.json(updated);
});

// PATCH /api/expenses/:id -> actualiza parcialmente el registro
router.patch('/:id', (req, res) => {
  const expense = db
    .prepare('SELECT * FROM expenses WHERE id = ? AND user_id = ?')
    .get(req.params.id, req.userId);

  if (!expense) {
    return res.status(404).json({ message: 'Registro no encontrado' });
  }

  const updated = { ...expense, ...req.body };

  db.prepare(
    `UPDATE expenses SET category = ?, amount = ?, description = ?, type = ?, date = ?
     WHERE id = ? AND user_id = ?`
  ).run(
    updated.category,
    updated.amount,
    updated.description,
    updated.type,
    updated.date,
    req.params.id,
    req.userId
  );

  const result = db.prepare('SELECT * FROM expenses WHERE id = ?').get(req.params.id);
  res.json(result);
});

// DELETE /api/expenses/:id -> elimina un registro
router.delete('/:id', (req, res) => {
  const expense = db
    .prepare('SELECT * FROM expenses WHERE id = ? AND user_id = ?')
    .get(req.params.id, req.userId);

  if (!expense) {
    return res.status(404).json({ message: 'Registro no encontrado' });
  }

  db.prepare('DELETE FROM expenses WHERE id = ? AND user_id = ?').run(req.params.id, req.userId);
  res.status(204).send();
});

module.exports = router;
