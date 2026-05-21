import { useState } from "react";
import { useParams } from "react-router";

function BookFlightPage() {
    const { flightId } = useParams();

    const [passengerName, setPassengerName] = useState("");
    const [passengerEmail, setPassengerEmail] = useState("");
    const [message, setMessage] = useState("");

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
            `Are you sure you want to book this flight for ${passengerName}?`
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
                setMessage("Booking successful!");
                setPassengerName("");
                setPassengerEmail("");
            })
            .catch(() => {
                setMessage("Something went wrong. Booking was not completed.");
            });
    }

    return (
        <div>
            <h2>Book Flight</h2>

            <form onSubmit={handleSubmit}>
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