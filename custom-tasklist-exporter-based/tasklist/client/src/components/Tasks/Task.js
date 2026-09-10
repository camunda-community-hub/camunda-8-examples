import TaskInfo from "./TaskInfo";
import TaskHeader from "./TaskHeader";
import React, {useState} from "react";
import Tabs from "./Tabs/Tabs";

function Task(props) {

    const [taskOpen, setTaskOpen] = useState(false)

    function showDetails() {
        setTaskOpen(taskOpen => !taskOpen)
    }

    return (
        <div className="task card shadow-sm bg-white rounded">
            <TaskHeader
                handleClick={showDetails}
                task={props.task}
                taskOpen={taskOpen}>
            </TaskHeader>
            {taskOpen && <TaskInfo task={props.task}></TaskInfo>}
            {taskOpen && <Tabs task={props.task}></Tabs>}
        </div>
    );
}

export default Task