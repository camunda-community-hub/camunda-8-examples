<script lang="ts" setup>
import { useQuery, useMutation, useResult } from "@vue/apollo-composable";
import gql from "graphql-tag";

defineProps(["tasklist", "selectedTask"]);
defineEmits(["selectedTask"]);
</script>

<template>
  <div v-for="task in tasklist" class="element">
    <button @click="$emit('selectedTask', task)" :class="{
      active: selectedTask === task,
    }">{{ task.name }}</button>
  </div>
</template>

<style scoped>
button {
  width: 100%;
  box-sizing: border-box;
}

.element {
  padding: 10px;
}
</style>
