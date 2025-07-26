import { NavLink } from "react-router-dom";

export default function Sidebar() {
  return (
    <nav style={{
      width: 200,
      padding: "1rem",
      background: "#f5f5f5",
      boxShadow: "2px 0 5px rgba(0,0,0,0.1)"
    }}>
      <h3>Invoicesly</h3>
      <ul style={{ listStyle: "none", padding: 0 }}>
        <li>
          <NavLink
            to="/"
            end
            style={({ isActive }) => ({
              fontWeight: isActive ? "bold" : "normal"
            })}
          >
            Dashboard
          </NavLink>
        </li>
        <li>
          <NavLink
            to="/customers"
            style={({ isActive }) => ({
              fontWeight: isActive ? "bold" : "normal"
            })}
          >
            Customers
          </NavLink>
        </li>
        <li>
          <NavLink
            to="/invoices"
            style={({ isActive }) => ({
              fontWeight: isActive ? "bold" : "normal"
            })}
          >
            Invoices
          </NavLink>
        </li>
      </ul>
    </nav>
  );
}
