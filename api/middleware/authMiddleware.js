const jwt = require('jsonwebtoken');

// Verifica el JWT enviado en el header Authorization: Bearer <token>
// Si el token expiró o es inválido, responde 401 para que la app
// redirija al usuario al Login.
function authMiddleware(req, res, next) {
  const authHeader = req.headers['authorization'];
  const token = authHeader && authHeader.split(' ')[1];

  if (!token) {
    return res.status(401).json({ message: 'Token no proporcionado' });
  }

  jwt.verify(token, process.env.JWT_SECRET, (err, decoded) => {
    if (err) {
      if (err.name === 'TokenExpiredError') {
        return res.status(401).json({ message: 'Sesión expirada, inicia sesión nuevamente' });
      }
      return res.status(403).json({ message: 'Token inválido' });
    }
    req.userId = decoded.userId;
    next();
  });
}

module.exports = authMiddleware;
