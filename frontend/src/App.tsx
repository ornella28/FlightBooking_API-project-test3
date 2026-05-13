import { BrowserRouter, Routes, Route } from "react-router";
import Navbar from "./components/Navbar";
import FlightsPage from "./pages/FlightsPages.tsx";
import AvailableFlightsPage from "./pages/AvailableFlightsPage";
import BookFlightPage from "./pages/BookFlightPage.tsx";

function App() {
  return (
      <BrowserRouter>
        <h1>Flight Reservation System</h1>

        <Navbar />

        <Routes>
          <Route path="/" element={<FlightsPage />} />
          <Route path="/available-flights" element={<AvailableFlightsPage />} />
            <Route path="/book-flight/:flightId" element={<BookFlightPage />} />
        </Routes>
      </BrowserRouter>
  );
}

export default App;