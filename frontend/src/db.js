const Database = require('better-sqlite3');
const path = require('path');
const db = new Database(path.join(__dirname, 'invoices.db'));

// Create table if not exists
db.prepare(`
  CREATE TABLE IF NOT EXISTS invoice (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    customerName TEXT,
    createdAt TEXT,
    amount REAL,
    lineItemsJson TEXT,
    synced INTEGER DEFAULT 0
  )
`).run();

module.exports = db;
