function TaskInfo(props) {

    return (
            <div className="task-info d-flex flex-row align-self-end">
                <p>Tags:
                    <span className="badge badge-pill green-badge">{props.task.source}</span>
                </p>
                {props.task.priority != null && <p><span>Priority: </span>{props.task.priority}</p>}
                {props.task.dueDate != null  && <p><span>Due Date: </span>{props.task.dueDate}</p>}
                {props.task.assignee != null && <p><span>Assignee: </span>{props.task.assignee}</p>}
            </div>
    );
}

export default TaskInfo