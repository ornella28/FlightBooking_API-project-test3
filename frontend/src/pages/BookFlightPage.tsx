import { useEffect, useState } from "react";
import { useParams } from "react-router";
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

function BookFlightPage() {
    const { flightId } = useParams();

    const [flight, setFlight] = useState<Flight | null>(null);
    const [passengerName, setPassengerName] = useState("");
    const [passengerEmail, setPassengerEmail] = useState("");
    const [message, setMessage] = useState("");
    const [loading, setLoading] = useState(true);
    const [bookingCompleted, setBookingCompleted] = useState(false);

    useEffect(() => {
        fetch("http://localhost:8080/api/flights")
            .then((response) => response.json())
            .then((data) => {
                const selectedFlight = data.find(
                    (flight: Flight) => flight.id === Number(flightId)
                );

                setFlight(selectedFlight);
            })
            .catch(() => {
                setMessage("Could not load flight information.");
            })
            .finally(() => {
                setLoading(false);
            });
    }, [flightId]);

    function handleSubmit(event: React.FormEvent) {
        event.preventDefault();

        if (passengerName.trim().length < 2) {
            setMessage("Passenger name must be at least 2 characters.");
            return;
        }

        if (!passengerEmail.includes("@")) {
            setMessage("Please enter a valid email address.");
            return;
        }

        const confirmBooking = window.confirm(
            `Are you sure you want to book flight ${flight?.flightNumber} to ${flight?.destination}?`
        );

        if (!confirmBooking) {
            setMessage("Booking cancelled.");
            return;
        }

        const bookingData = {
            passengerName,
            passengerEmail,
        };

        fetch(`http://localhost:8080/api/flights/${flightId}/book`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify(bookingData),
        })
            .then((response) => {
                if (!response.ok) {
                    throw new Error("Booking failed");
                }
                return response.json();
            })
            .then(() => {
                setBookingCompleted(true);
                setMessage("Flight booked successfully!");
            })
            .catch(() => {
                setMessage("Something went wrong. Booking was not completed.");
            });
    }

    if (loading) return <p>Loading flight information...</p>;

    if (bookingCompleted) {
        return (
            <div className="card">
                <h2>Booking Confirmed</h2>

                <p className="message">
                    Flight booked successfully!
                </p>
            </div>
        );
    }

    return (
        <div className="booking-page">
            <h2 className="page-title">Book Flight</h2>

            {flight && (
                <div className="card booking-card">
                    <h3>{flight.destination}</h3>
                    <p>Flight number: {flight.flightNumber}</p>
                    <p>Departure: {formatDate(flight.departureTime)}</p>
                    <p>Arrival: {formatDate(flight.arrivalTime)}</p>
                    <p>Price: {flight.price} kr</p>
                    <p>
                        Status: <span className="status">{flight.status || "Available"}</span>
                    </p>
                </div>
            )}

            <form className="booking-form" onSubmit={handleSubmit}>
                <div>
                    <label>Passenger name</label>
                    <br />
                    <input
                        type="text"
                        value={passengerName}
                        onChange={(event) => setPassengerName(event.target.value)}
                        required
                    />
                </div>

                <div>
                    <label>Passenger email</label>
                    <br />
                    <input
                        type="email"
                        value={passengerEmail}
                        onChange={(event) => setPassengerEmail(event.target.value)}
                        required
                    />
                </div>

                <button type="submit">Confirm booking</button>
            </form>

            {message && <p className="message">{message}</p>}
        </div>
    );
}

export default BookFlightPage;