// frontend/electron/main.js
const { app, BrowserWindow } = require('electron');
const path = require('path');

function createWindow() {
  const win = new BrowserWindow({
    width: 1000,
    height: 700,
    webPreferences: {
      nodeIntegration: true, // allow Node in renderer
      contextIsolation: false, // for simplicity now
    },
  });

  // Use Vite dev server during dev
  win.loadURL('http://localhost:5173');
}

app.whenReady().then(createWindow);
