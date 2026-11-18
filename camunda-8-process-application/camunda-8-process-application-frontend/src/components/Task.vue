<script lang="ts" setup>
import GenericForm from "./GenericForm.vue";
import { useQuery, useResult } from "@vue/apollo-composable";
import gql from "graphql-tag";
import { ref, reactive, computed, type Ref } from "vue";
import BpmnViewer from "./BpmnViewerContainer.vue";
import CamundaForm from "./CamundaFormContainer.vue";
import CustomFormContainer from "./CustomFormContainer.vue";
import FormIoContainer from "./FormIoContainer.vue";

const props = defineProps(["taskId"]);
defineEmits(["completeTask"]);

const taskQuery = useQuery(
  gql`
    query ($id: String!) {
      task(id: $id) {
        id
        name
        taskDefinitionId
        processName
        creationTime
        completionTime
        assignee
        variables {
          id
          name
          value
          previewValue
          isValueTruncated
        }
        taskState
        sortValues
        isFirst
        formKey
        processDefinitionId
        candidateGroups
      }
    }
  `,
  reactive({ id: computed(() => props.taskId) }),
  { fetchPolicy: "no-cache" }
);

const form = ref(null as any);

const task = ref(useResult(taskQuery.result));

const faulty = ref(false);
const message = ref("");

const setError = (msg: unknown) => {
  if (msg) {
    faulty.value = true;
    message.value = msg as string;
  } else {
    faulty.value = false;
    message.value = "";
  }
};

const taskMenu = ["Form", "Diagram", "Raw data"];
const activeTask = ref("Form");

const isCamundaForm = (formKey: string): boolean => {
  return formKey != null && formKey.startsWith("camunda-forms:bpmn:");
}
const isFormIoForm = (formKey: string): boolean => {
  return formKey != null && formKey.startsWith("form-io:bpmn:");
}
const isCustomForm = (formKey: string): boolean => {
  return formKey != null && formKey != "";
}
</script>

<template>
  <div v-if="task">
    <h3 class="task-header">{{ task.name }}</h3>
    <div class="task-menu">
      <button
        v-for="taskMenuPoint in taskMenu"
        v-bind:key="taskMenuPoint"
        @click="activeTask = taskMenuPoint"
        :class="{
          active: activeTask === taskMenuPoint,
        }"
      >
        {{ taskMenuPoint }}
      </button>
    </div>
    <div class="task-content">
      <div class="form-container" v-if="activeTask === 'Form'">
        <CamundaForm
          v-if="isCamundaForm(task.formKey)"
          :task="task"
          :key="'' + task.id + task.formKey + '-camundaForm'"
          @errorMessage="setError"
          ref="form"
        ></CamundaForm>
        <FormIoContainer
          v-else-if="isFormIoForm(task.formKey)"
          :task="task"
          :key="'' + task.id + task.formKey + '-formIoForm'"
          @errorMessage="setError"
          ref="form"
        ></FormIoContainer>
        <CustomFormContainer
          v-else-if="isCustomForm(task.formKey)"
          :task="task"
          :key="'' + task.id + task.formKey + '-customForm'"
          @errorMessage="setError"
          ref="form"
        ></CustomFormContainer>
        <GenericForm
          v-else
          :task="task"
          :key="'' + task.id + task.formKey + '-genericForm'"
          @errorMessage="setError"
          ref="form"
        ></GenericForm>
        <div class="button-wrapper">
          <button
            class="complete"
            :disabled="faulty"
            @click="
              $emit('completeTask', {
                taskId: task.id,
                variables: form.variables,
              })
            "
          >
            Complete
          </button>
        </div>

        <div class="error-message" v-if="faulty">
          {{ message }}
        </div>
      </div>
      <div class="diagram-container" v-if="activeTask === 'Diagram'">
        <BpmnViewer
          :processDefinitionId="task.processDefinitionId"
          :taskDefinitionId="task.taskDefinitionId"
          :key="task.id"
        ></BpmnViewer>
      </div>
      <div class="rawdata-container" v-if="activeTask === 'Raw data'">
        <pre>{{ JSON.stringify(task, undefined, 2) }}</pre>
      </div>
    </div>
  </div>
</template>

<style scoped>
h3 {
  padding: 10px 0px;
  width: auto;
}

.task-header {
  height: 3%;
}

.task-menu {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
}

.task-menu * {
  margin: 5px;
}

.task-content {
  height: 80%;
}

table {
  width: 100%;
}

tr,
td {
  height: 50px;
  padding: 10px;
}

.error-message {
  color: red;
}

.complete {
  float: right;
  width: auto;
}

.complete:disabled {
  background-color: var(--e-global-color-9154836);
  border-color: var(--e-global-color-9154836);
  cursor: default;
}

textarea {
  background-color: gray;
  color: white;
}

.variable-column {
  width: 15%;
}

.value-column {
  width: 85%;
}

.button-wrapper {
  height: 50px;
}

.diagram-container {
  height: 100%;
}
</style>
