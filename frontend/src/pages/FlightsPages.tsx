import { useEffect, useState } from "react";
import type { Flight } from "../types";

function formatDate(dateString: string) {
    return new Date(dateString).toLocaleString("en-GB", {
        day: "numeric",
        month: "short",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit",
    });
}

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
            <h2 className="page-title">All Flights</h2>

            <div className="card-grid">
                {flights.map((flight) => (
                    <div className="card" key={flight.id}>
                        <h3>{flight.destination}</h3>

                        <p>Flight number: {flight.flightNumber}</p>
                        <p>Departure: {formatDate(flight.departureTime)}</p>
                        <p>Arrival: {formatDate(flight.arrivalTime)}</p>
                        <p>Price: {flight.price} kr</p>

                        <p>
                            Status: <span className="status">{flight.status}</span>
                        </p>
                    </div>
                ))}
            </div>
        </div>
    );
}

export default FlightsPage;