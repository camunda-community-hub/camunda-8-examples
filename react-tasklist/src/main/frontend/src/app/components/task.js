'use client'

import dynamic from 'next/dynamic'
import {useEffect, useState} from "react";

const CamundaForm = dynamic(
    () => import('./camunda-form'),
    {ssr: false}
)
const ReactForm = dynamic(
    () => import('./react-form'),
    {ssr: false}
)

export default function Task({taskId}) {
  const [task, setTask] = useState();
  const completeTask = (data) => {
    console.log(data);
    fetch(`/api/tasks/${taskId}/complete`, {method: "PATCH", body: JSON.stringify(data), headers: {"Content-Type":"application/json"}}).then(r => r.status >=200 && r.status<400 ? alert("Task completed"):alert(`Error while completing the task: ${r.statusText}`))
  }
  useEffect(() => {
    fetch(`/api/tasks/${taskId}`).then(r => r.json()).then(json => {
      setTask(json);
    });
  }, []);


  return (
      <>
        {task ? (
            <>
              <h3>{task.name}</h3>{
              task.schema ? (
                  <CamundaForm schema={task.schema} data={task.data} onComplete={completeTask}></CamundaForm>) : (
                  <ReactForm formKey={task.formKey} data={task.data} onComplete={completeTask}></ReactForm>)}
            </>
        ) : (<p>Loading...</p>)}
      </>
  )
}