import { FaCaretDown, FaCaretRight } from "react-icons/fa";
function TaskHeader(props) {

    let badgeColor = ""
    let status = ""

    switch (props.task.eventType) {
        case "CREATED":
            badgeColor = "orange-badge"
            status = "To Do"
            break;
        case "COMPLETED":
            badgeColor = "green-badge"
            status = "Done"
            break;
        case "ENDED":
            badgeColor = "red-badge"
            status = "Ended"
            break;
        default:
            break;


    }

    return (
        <div onClick={props.handleClick} className="task-header d-flex justify-content-between">
            <div>
                <h5 className=" card-title bold" data-bs-toggle="collapse" href={"#task-" + props.task.userTaskId}>
                    {props.taskOpen ?<FaCaretDown></FaCaretDown>:<FaCaretRight></FaCaretRight>}
                    <span className="task-name">{props.task.taskElementName} </span>
                    {props.taskOpen && <span className={"badge badge-pill " + badgeColor }>{status}</span>}
                </h5>
            </div>
            { !props.taskOpen && <div className="info-header">{props.task.assignee}</div>}
            { !props.taskOpen && <div className="info-header">{props.task.priority}</div>}
            { !props.taskOpen && <div className="info-header">{props.task.dueDate}</div>}
            { !props.taskOpen && <div className="info-header"><span className={"badge badge-pill " + badgeColor }>{status}</span></div>}
        </div>
    );
}

export default TaskHeader