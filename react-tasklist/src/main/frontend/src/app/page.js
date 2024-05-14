'use client'

import dynamic from 'next/dynamic'
import {useEffect, useState} from "react";

const Task = dynamic(
    () => import('./components/task'),
    {ssr: false}
)

export default function Home() {
  const [tasks, setTasks] = useState();
  const startProcess = () => fetch("/api/start-process", {
    method: "POST",
    body: JSON.stringify({foo: "bar"}),
    headers: {"Content-Type": "application/json"}
  }).then(r => r.status >= 200 && r.status < 400 ? alert("Process started") : alert(`Error while starting process: ${r.statusText}`))

  useEffect(() => {
    fetch(`/api/tasks`).then(r => r.json()).then(json => {
      setTasks(json);
    });
  }, []);
  const taskList = (tasks || []).map(task => <Task key={task.id} taskId={task.id}></Task>)
  return (
      <main className="flex min-h-screen flex-col items-center p-24">
        <button onClick={startProcess}>Start Process</button>
        <h1>Tasklist</h1>
        {taskList}
      </main>
  )
}
