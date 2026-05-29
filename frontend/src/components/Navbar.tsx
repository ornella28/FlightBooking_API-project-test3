import { Link } from "react-router";

function Navbar() {
    return (
        <nav className="navbar">
            <Link to="/">All Flights</Link>
            <Link to="/available-flights">Available Flights</Link>
            <Link to="/bookings">Search Bookings</Link>
            <Link to="/assistant">Assistant</Link>

        </nav>
    );
}

export default Navbar;