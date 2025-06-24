import {FaRegCheckCircle} from "react-icons/fa";

function Header() {
    return (
        <div className="header fixed-top">
            <nav className="navbar shadow-sm p-3">
                <h3 className="task-list-name bold" href="">Custom Tasklist</h3>
                <div className="task-list-icon">
                    <FaRegCheckCircle size={"3em"}></FaRegCheckCircle>
                </div>
            </nav>
        </div>
    );
}

export default Header;
