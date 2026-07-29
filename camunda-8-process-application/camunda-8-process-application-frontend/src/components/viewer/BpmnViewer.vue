<script setup>
import gql from "graphql-tag";
import { ref, reactive, computed, watchEffect, watch, onMounted } from "vue";
import { useQuery, useResult } from "@vue/apollo-composable";
import BpmnViewer from "bpmn-js";

const props = defineProps(["bpmnXml", "taskDefinitionId"]);

const bpmnXml = ref(props.bpmnXml);

const canvas = ref(null);

onMounted(async () => {
  const viewer = new BpmnViewer({ container: canvas.value });
  await viewer.importXML(bpmnXml.value.data);
  viewer.get("canvas").zoom("fit-viewport");
  const bpmnTask = viewer.get("elementRegistry").get(props.taskDefinitionId);
  const overlay = document.createElement("div");
  overlay.className = "active-bpmn-task";
  overlay.setAttribute(
    "style",
    `width: ${bpmnTask.width}px; height: ${bpmnTask.height}px`
  );
  viewer.get("overlays").add(props.taskDefinitionId, {
    position: {
      top: -4,
      left: -4,
    },
    html: overlay,
  });
  window.addEventListener("resize", () => viewer.get("canvas").zoom("fit-viewport"));
});
</script>

<template>
  <div ref="canvas" class="diagram-viewer" id="diagram-viewer"></div>
</template>

<style>
.diagram-viewer {
  border-radius: 30px;
  width: 100%;
  height: 100%;
  background-color: white;
}

.active-bpmn-task {
  background-color: var(--primary);
  border-radius: 15px;
  border: 4px solid var(--e-global-color-af24b6d);
  opacity: 0.6;
  pointer-events: none;
  padding: 0;
}
</style>
