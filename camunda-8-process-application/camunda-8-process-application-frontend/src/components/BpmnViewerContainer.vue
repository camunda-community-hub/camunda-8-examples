<script setup>
import gql from "graphql-tag";
import { ref, reactive, computed, watchEffect, watch } from "vue";
import { useQuery, useResult } from "@vue/apollo-composable";
import BpmnViewer from "./viewer/BpmnViewer.vue";

const props = defineProps(["processDefinitionId", "taskDefinitionId"]);
const query = useQuery(
  gql`
    query($processDefinitionId: ID!) {
      bpmnXml(processDefinitionId: $processDefinitionId) {
        id
        data
      }
    }
  `,
  { processDefinitionId: props.processDefinitionId }
);
const bpmnXml = useResult(query.result);

</script>

<template>
  <BpmnViewer
    :bpmnXml="bpmnXml"
    :taskDefinitionId="taskDefinitionId"
    v-if="bpmnXml"
  ></BpmnViewer>
</template>
