// src/layout/Navbar.jsx
import React from 'react'
import { useNavigate } from 'react-router-dom'

const Navbar = ({ isAuthenticated, onLogout }) => {
  const navigate = useNavigate()

  const handleLoginLogout = () => {
    if (isAuthenticated) {
      onLogout()
    } else {
      navigate('/login')
    }
  }

  const handleAddInvoice = () => {
    navigate('/invoices/new')
  }

  return (
    <nav style={styles.navbar}>
      <div style={styles.leftSection}>
        <span style={styles.logo}>🧾 Invoicesly</span>
      </div>

      <div style={styles.rightSection}>
        {isAuthenticated && (
          <button style={styles.button} onClick={handleAddInvoice}>
            ➕ Add Invoice
          </button>
        )}
        <button style={styles.button} onClick={handleLoginLogout}>
          {isAuthenticated ? 'Logout' : 'Login'}
        </button>
      </div>
    </nav>
  )
}

const styles = {
  navbar: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    padding: '0.75rem 1.25rem',
    backgroundColor: '#20232a',
    color: 'white',
    borderBottom: '1px solid #333',
  },
  logo: {
    fontWeight: 'bold',
    fontSize: '1.25rem',
  },
  rightSection: {
    display: 'flex',
    gap: '0.75rem',
  },
  leftSection: {
    display: 'flex',
    alignItems: 'center',
  },
  button: {
    padding: '0.4rem 0.9rem',
    backgroundColor: '#61dafb',
    border: 'none',
    borderRadius: '5px',
    cursor: 'pointer',
    color: '#000',
    fontWeight: '600',
  },
}

export default Navbar
