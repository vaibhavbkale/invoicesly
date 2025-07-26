import Sidebar from "./Sidebar.jsx";
import Navbar from './Navbar'
import { Outlet } from "react-router-dom";

const Layout = ({ isAuthenticated, onLogout }) => {
    return (
      <div style={{ display: 'flex', height: '100vh' }}>
        <Sidebar />
        <div style={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
          <Navbar isAuthenticated={isAuthenticated} onLogout={onLogout} />
          <div style={{ padding: '1rem', flexGrow: 1 }}>
            <Outlet />
          </div>
        </div>
      </div>
    )
  }
  
  export default Layout
