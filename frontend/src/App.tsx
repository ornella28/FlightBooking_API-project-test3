import { BrowserRouter, Routes, Route } from "react-router";
import Navbar from "./components/Navbar";
import FlightsPage from "./pages/FlightsPages.tsx";
import AvailableFlightsPage from "./pages/AvailableFlightsPage";

function App() {
  return (
      <BrowserRouter>
        <h1>Flight Reservation System</h1>

        <Navbar />

        <Routes>
          <Route path="/" element={<FlightsPage />} />
          <Route path="/available-flights" element={<AvailableFlightsPage />} />
        </Routes>
      </BrowserRouter>
  );
}

export default App;