import { useState } from "react";

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

    return (
        <div>
            <h2>Search Bookings</h2>

            <form onSubmit={searchBookings}>
                <label>Passenger email</label>
                <br />
                <input
                    type="email"
                    value={email}
                    onChange={(event) => setEmail(event.target.value)}
                    required
                />

                <button type="submit">Search</button>
            </form>

            {message && <p className="message">{message}</p>}

            {bookings.map((booking) => (
                <div key={booking.id}>
                    <h3>{booking.passengerName}</h3>
                    <p>Email: {booking.passengerEmail}</p>
                    <p>Flight ID: {booking.id}</p>
                    <p>Flight number: {booking.flightNumber}</p>
                    <p>Destination: {booking.destination}</p>
                    <p>Departure: {booking.departureTime}</p>
                    <p>Arrival: {booking.arrivalTime}</p>
                    <p>Price: {booking.price}</p>
                    <p>Status: {booking.status}</p>
                </div>
            ))}
        </div>
    );
}

export default BookingsPage;