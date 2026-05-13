import { useEffect, useState } from "react";
import type { Flight } from "../types";

function FlightsPage() {
    const [flights, setFlights] = useState<Flight[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        fetch("http://localhost:8080/api/flights")
            .then((response) => {
                if (!response.ok) {
                    throw new Error("Could not fetch flights");
                }
                return response.json();
            })
            .then((data) => {
                setFlights(data);
            })
            .catch(() => {
                setError("Something went wrong while loading flights.");
            })
            .finally(() => {
                setLoading(false);
            });
    }, []);

    if (loading) {
        return <p>Loading flights...</p>;
    }

    if (error) {
        return <p>{error}</p>;
    }

    return (
        <div>
            <h2>All Flights</h2>

            {flights.map((flight) => (
                <div key={flight.id}>
                    <h3>
                        {flight.origin} → {flight.destination}
                    </h3>
                    <p>Flight number: {flight.flightNumber}</p>
                    <p>Departure: {flight.departureTime}</p>
                    <p>Arrival: {flight.arrivalTime}</p>
                    <p>Available seats: {flight.availableSeats}</p>
                </div>
            ))}
        </div>
    );
}

export default FlightsPage;