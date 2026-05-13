import { Link } from "react-router";

function Navbar() {
    return (
        <nav>
            <Link to="/">All Flights</Link> |{" "}
            <Link to="/available-flights">Available Flights</Link>
        </nav>
    );
}

export default Navbar;