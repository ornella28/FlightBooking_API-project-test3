import { BrowserRouter, Routes, Route } from "react-router";
import Navbar from "./components/Navbar";
import FlightsPage from "./pages/FlightsPages.tsx";
import AvailableFlightsPage from "./pages/AvailableFlightsPage.tsx";
import BookFlightPage from "./pages/BookFlightPage.tsx";
import BookingsPage from "./pages/BookingsPage.tsx";
import ChatbotPage from "./pages/ChatbotPage.tsx";



function App() {
  return (
      <BrowserRouter>
          <div className="app">
              <div className="header">
                  <h1>Flight Reservation System</h1>
                  <p>Search, book and manage flights easily</p>
              </div>

              <Navbar />

              <Routes>
                  <Route path="/" element={<FlightsPage />} />
                  <Route path="/available-flights" element={<AvailableFlightsPage />} />
                  <Route path="/book-flight/:flightId" element={<BookFlightPage />} />
                  <Route path="/bookings" element={<BookingsPage />} />
                  <Route path="/assistant" element={<ChatbotPage />} />

              </Routes>
          </div>
      </BrowserRouter>
  );
}

export default App;