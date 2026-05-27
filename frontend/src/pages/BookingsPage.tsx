import { useState } from "react";

function formatDate(dateString: string) {
    return new Date(dateString).toLocaleString("en-GB", {
        day: "numeric",
        month: "short",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit",
    });
}

type Booking = {
    id: number;
    passengerName: string;
    passengerEmail: string;
    flightNumber: string;
    departureTime: string;
    arrivalTime: string;
    destination: string;
    price: number;
    status: string;
};

function BookingsPage() {
    const [email, setEmail] = useState("");
    const [bookings, setBookings] = useState<Booking[]>([]);
    const [message, setMessage] = useState("");

    function searchBookings(event: React.FormEvent) {
        event.preventDefault();

        fetch(`http://localhost:8080/api/flights/bookings?email=${email}`)
            .then((response) => {
                if (!response.ok) {
                    throw new Error("Could not fetch bookings");
                }
                return response.json();
            })
            .then((data) => {
                console.log("BOOKINGS DATA:", data);
                setBookings(data);
                setMessage(data.length === 0 ? "No bookings found." : "");
            })
    }

    function resetSearch() {
        setEmail("");
        setBookings([]);
        setMessage("");
    }

    function cancelBooking(flightId: number, passengerEmail: string) {
        const confirmCancel = window.confirm(
            `Are you sure you want to cancel flight ${flightId} for ${passengerEmail}?`
        );

        if (!confirmCancel) {
            return;
        }

        fetch(
            `http://localhost:8080/api/flights/${flightId}/cancel?email=${passengerEmail}`,
            {
                method: "DELETE",
            }
        )
            .then((response) => {
                if (!response.ok) {
                    throw new Error("Cancel failed");
                }

                setMessage("Booking cancelled successfully!");

                setBookings((previousBookings) =>
                    previousBookings.filter((booking) => booking.id !== flightId)
                );
            })
            .catch(() => {
                setMessage("Something went wrong. Booking was not cancelled.");
            });
    }

    return (
        <div>
            <h2 className="page-title">My Bookings</h2>

            <form onSubmit={searchBookings}>
                <label>Passenger email</label>
                <br />
                <input
                    type="email"
                    value={email}
                    onChange={(event) => setEmail(event.target.value)}
                    required
                />

                <button type="submit">My bookings</button>
            </form>

            {message && <p className="message">{message}</p>}

            <div className="card-grid">
                {bookings.map((booking) => (
                    <div className="card" key={booking.id}>
                        <h3>{booking.destination}</h3>

                        <p><strong>Flight ID:</strong> {booking.id}</p>
                        <p><strong>Passenger:</strong> {booking.passengerName}</p>
                        <p><strong>Email:</strong> {booking.passengerEmail}</p>
                        <p><strong>Flight number:</strong> {booking.flightNumber}</p>
                        <p><strong>Departure:</strong> {formatDate(booking.departureTime)}</p>
                        <p><strong>Arrival:</strong> {formatDate(booking.arrivalTime)}</p>
                        <p><strong>Price:</strong> {booking.price} kr</p>
                        <p>
                            <strong>Status:</strong>{" "}
                            <span className="status">{booking.status}</span>
                        </p>
                        <button
                            className="cancel-button"
                            onClick={() => cancelBooking(booking.id, booking.passengerEmail)}
                        >
                            Cancel this booking
                        </button>
                    </div>
                ))}
            </div>
            {bookings.length > 0 && (
                <button onClick={resetSearch}>Search another email</button>
            )}
        </div>
    );
}

export default BookingsPage;