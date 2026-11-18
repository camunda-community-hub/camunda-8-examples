function Tab(props) {

    return (
        <div className={props.active ? "tab-pane show active": "tab-pane"} id={`nav-${props.label}-${props.task.userTaskId}`} role="tabpanel"
             aria-labelledby={`nav-${props.label}-tab-${props.task.userTaskId}`}>
            {props.content}
        </div>
    );
}

export default Tab