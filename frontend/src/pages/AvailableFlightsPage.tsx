import { Link } from "react-router";
import { useEffect, useState } from "react";
import type { Flight } from "../types";

function AvailableFlightsPage() {
    const [flights, setFlights] = useState<Flight[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        fetch("http://localhost:8080/api/flights/available")
            .then((response) => {
                if (!response.ok) {
                    throw new Error("Could not fetch available flights");
                }
                return response.json();
            })
            .then((data) => {
                setFlights(data);
            })
            .catch(() => {
                setError("Something went wrong while loading available flights.");
            })
            .finally(() => {
                setLoading(false);
            });
    }, []);

    if (loading) return <p>Loading available flights...</p>;

    if (error) return <p>{error}</p>;

    return (
        <div>
            <h2>Available Flights</h2>

            {flights.length === 0 && <p>No available flights found.</p>}

            {flights.map((flight) => (
                <div key={flight.id}>
                    <h3>
                        {flight.origin} → {flight.destination}
                    </h3>
                    <p>Flight number: {flight.flightNumber}</p>
                    <p>Departure: {flight.departureTime}</p>
                    <p>Arrival: {flight.arrivalTime}</p>
                    <p>Available seats: {flight.availableSeats}</p>
                    <Link to={`/book-flight/${flight.id}`}>
                        <button>Book this flight</button>
                    </Link>
                </div>
            ))}
        </div>
    );
}

export default AvailableFlightsPage;