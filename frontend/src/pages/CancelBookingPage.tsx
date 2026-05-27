import { useState } from "react";

function CancelBookingPage() {
    const [flightId, setFlightId] = useState("");
    const [passengerEmail, setPassengerEmail] = useState("");
    const [message, setMessage] = useState("");

    function handleCancel(event: React.FormEvent) {
        event.preventDefault();

        if (!flightId.trim()) {
            setMessage("Please enter a flight ID.");
            return;
        }

        if (Number(flightId) <= 0) {
            setMessage("Flight ID must be a positive number.");
            return;
        }

        if (!passengerEmail.trim()) {
            setMessage("Please enter the passenger email.");
            return;
        }

        if (!passengerEmail.includes("@")) {
            setMessage("Please enter a valid email address.");
            return;
        }

        fetch(
            `http://localhost:8080/api/flights/bookings?email=${passengerEmail}`
        )
            .then((response) => {
                if (!response.ok) {
                    throw new Error("Could not verify booking.");
                }

                return response.json();
            })
            .then((bookings) => {
                const bookingExists = bookings.some(
                    (booking: any) => booking.id === Number(flightId)
                );

                if (!bookingExists) {
                    setMessage(
                        "No booking was found for this flight ID and email address."
                    );
                    return;
                }

                const confirmCancel = window.confirm(
                    `Are you sure you want to cancel flight ${flightId}?`
                );

                if (!confirmCancel) {
                    setMessage("Cancellation cancelled.");
                    return;
                }

                return fetch(
                    `http://localhost:8080/api/flights/${flightId}/cancel?email=${passengerEmail}`,
                    {
                        method: "DELETE",
                    }
                );
            })
            .then((response) => {
                if (!response) return;

                if (!response.ok) {
                    throw new Error("Cancel failed");
                }

                setMessage("Booking cancelled successfully!");
                setFlightId("");
                setPassengerEmail("");
            })
            .catch(() => {
                setMessage("Something went wrong. Booking was not cancelled.");
            });
    }

    return (
        <div>
            <h2>Cancel Booking</h2>

            <form onSubmit={handleCancel}>
                <div>
                    <label>Flight ID</label>
                    <br />
                    <input
                        type="number"
                        value={flightId}
                        onChange={(event) => setFlightId(event.target.value)}
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

                <button type="submit">Cancel booking</button>
            </form>

            {message && <p className="message">{message}</p>}
        </div>
    );
}

export default CancelBookingPage;