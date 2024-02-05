<script lang="ts" setup>
import Tasklist from "./components/Tasklist.vue";
import Task from "./components/Task.vue";
import { onBeforeUpdate, ref } from "vue";
import { useQuery, useResult, useMutation } from "@vue/apollo-composable";
import gql from "graphql-tag";

const currentUserQuery = useQuery(gql`
  query currentUser {
    currentUser {
      userId
      displayName
    }
  }
`);

const currentUser = useResult(currentUserQuery.result);

const selectedTask = ref();

const tasksQuery = useQuery(
  gql`
    query($query: TaskQuery!) {
      tasks(query: $query) {
        id
        name
      }
    }
  `,
  {
    query: {
      state: "CREATED",
    },
  },
  {
    pollInterval: 60000,
  }
);
const tasklist = useResult(tasksQuery.result);

const completeTaskMutation = useMutation(gql`
  mutation($taskId: String!, $variables: [VariableInput!]!) {
    completeTask(taskId: $taskId, variables: $variables) {
      id
    }
  }
`);

const completeTask = completeTaskMutation.mutate;

onBeforeUpdate(async () => {
  if (tasklist.value && selectedTask.value) {
    tasksQuery.refetch()?.then(() => {
      if (
        tasklist.value.findIndex(
          (task: any) => task && selectedTask.value && task.id === selectedTask.value.id
        ) === -1
      ) {
        selectedTask.value = undefined;
      }
    });
  }
});
</script>

<template>
  <div class="header">
    <h1>Insurance Application</h1>
    <p v-if="currentUser">Welcome {{ currentUser.displayName }}</p>
    <p v-else></p>
  </div>
  <div class="tasklist">
    <div class="list">
      <Tasklist
        @selectedTask="(task) => (selectedTask = task)"
        :tasklist="tasklist"
        :selectedTask="selectedTask"
      >
      </Tasklist>
    </div>
    <Task
      v-if="selectedTask && !completeTaskMutation.loading.value"
      :taskId="selectedTask!.id"
      @completeTask="completeTask"
      class="content"
      :key="selectedTask.id"
    ></Task>
  </div>
</template>

<style>
* {
  font-family: var(--e-global-typography-primary-font-family);
}

html,
body {
  height: 100%;
  margin: 0px;
  border: 0px;
  padding: 0px;
  color: black;
  background-color: white;
  border-color: var(--e-global-color-af24b6d);
  border-radius: 30px;
}

h1,
h2,
h3,
h4,
h5,
h6 {
  margin: 0;
  padding: 20px;
  font-weight: var(--e-global-typography-primary-font-weight);
}

#app {
  height: 100%;
}

.header {
  text-align: center;
  height: 10%;
}

.header h1 {
  height: 70%;
  padding: 0;
}

.header p {
  height: 30%;
  margin: 0;
}

.tasklist {
  display: grid;
  grid-template-columns: 1fr 3fr;
  height: 90%;
}

.list {
  min-width: 300px;
  overflow-y: auto;
}

.content {
  width: auto;
  padding: 10px;
  margin-left: 10px;
  overflow-y: auto;
}

/* width */
::-webkit-scrollbar {
  width: 10px;
}

/* Track */
::-webkit-scrollbar-track {
  background: transparent;
  height: 80%;
}

/* Handle */
::-webkit-scrollbar-thumb {
  background: var(--e-global-color-af24b6d);
  border-radius: 5px;
}

/* Handle on hover */
::-webkit-scrollbar-thumb:hover {
  background: var(--primary);
}

::-webkit-scrollbar-track-piece:end {
  background: transparent;
  margin-bottom: 50px;
}

::-webkit-scrollbar-track-piece:start {
  background: transparent;
  margin-top: 50px;
}

.list,
.content,
details {
  border-radius: 30px;
  border-color: var(--primary);
  border-style: dotted;
  border-width: 3px;
}

details {
  padding: 10px;
}

summary {
  height: 30px;
}

button {
  cursor: pointer;
  padding: 10px;
  border: 3px solid var(--e-global-color-af24b6d);
  background-color: white;
  color: black;
  border-radius: 30px;
  font-weight: var(--e-global-typography-primary-font-weight);
}

button:hover:enabled {
  background-color: var(--e-global-color-af24b6d);
}

button.active {
  background-color: var(--e-global-color-af24b6d);
}

pre {
  font-family: var(--font-family-monospace);
  background-color: var(--gray);
  border-radius: 10px;
  padding: 5px;
  font-size: small;
  white-space: break-spaces;
}

.color-block {
  font-size: large;
  display: inline;
}
</style>
