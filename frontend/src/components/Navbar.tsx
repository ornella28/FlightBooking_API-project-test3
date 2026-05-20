import { Link } from "react-router";

function Navbar() {
    return (
        <nav>
            <Link to="/">All Flights</Link> |{" "}
            <Link to="/available-flights">Available Flights</Link> |{" "}
            <Link to="/bookings">Search Bookings</Link> |{" "}
            <Link to="/cancel-booking">Cancel Booking</Link>
        </nav>
    );
}

export default Navbar;