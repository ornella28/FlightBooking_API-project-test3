import FlightsPage from "./pages/FlightsPages.tsx";
import AvailableFlightsPage from "./pages/AvailableFlightsPage";

function App() {
  return (
      <div>
        <h1>Flight Reservation System</h1>

        <FlightsPage />

        <hr />

        <AvailableFlightsPage />
      </div>
  );
}

export default App;