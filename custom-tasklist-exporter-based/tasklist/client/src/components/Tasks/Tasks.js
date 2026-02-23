import Task from "./Task";
import { useQuery} from '@apollo/client';
import GetFilteredTasks from "../../integration/GetFilteredTasks";
import {useEffect} from "react";
function Tasks() {

    const { loading, error, data, startPolling } = useQuery(GetFilteredTasks,{
        variables: {eventType: "CREATED", page: 0, size: 10}
    });

    useEffect(() => {
        startPolling(500);
    }, [startPolling]);

    if (loading) return <p>Loading...</p>;
    if (error) return <p>Error : {error.message}</p>;


    return data.filteredUserTasks.map(task => (
        <Task
            key={task.userTaskId}
            task={task}
        ></Task>
    ));

}

export default Tasks;
