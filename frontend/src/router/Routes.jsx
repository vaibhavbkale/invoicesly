import { createBrowserRouter } from "react-router-dom";
import App from "../App.jsx";
import Dashboard from "../pages/Dashboard.jsx";
import Customers from "../pages/Customers.jsx";
import Invoices from "../pages/Invoices.jsx";

const router = createBrowserRouter([
  {
    path: "/",
    element: <App />,
    children: [
      { path: "", element: <Dashboard /> },
      { path: "customers", element: <Customers /> },
      { path: "invoices", element: <Invoices /> },
    ],
  },
]);

export default router;
