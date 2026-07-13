<script lang="ts" setup>
import { computed, ref, reactive } from "vue";

const props = defineProps(["task"]);
const emits = defineEmits(["errorMessage"]);

const variables = computed(() => props.task.variables);

const findVariable = (name: string): any => {
  console.log("Finding", name);
  return JSON.parse(
    variables.value.find((element: any) => element.name === name)!.value
  );
};
console.log("Start finding variables");
const applicationId = findVariable("applicationId");
const applicantName = findVariable("applicantName");
const age = findVariable("age");
const rating = findVariable("rating");
const vehicleManufacturer = findVariable("vehicleManufacturer");
const vehicleModel = findVariable("vehicleModel");
let accepted = ref(false);

defineExpose(reactive({ variables: [{ name: "accepted", value: accepted }] }));
</script>

<template>
  <table>
    <tr>
      <td>Application ID</td>
      <td>{{ applicationId }}</td>
    </tr>
    <tr>
      <td>Applicant Name</td>
      <td>{{ applicantName }}</td>
    </tr>
    <tr>
      <td>Age</td>
      <td>{{ age }}</td>
    </tr>
    <tr>
      <td>Risk rating</td>
      <td>
        <div class="color-block" :style="{ color: rating.group }">&#9632;</div>
        {{ rating.comment }}
      </td>
    </tr>
    <tr>
      <td>Vehicle</td>
      <td>{{ vehicleManufacturer }} {{ vehicleModel }}</td>
    </tr>
    <tr>
      <td>Accepted</td>
      <td><input type="checkbox" v-model="accepted" /></td>
    </tr>
  </table>
</template>

<style scoped>
p {
  padding: 20px;
}

td {
  height: auto;
  border-color: var(--primary);
  border-bottom-style: dotted;
  border-width: 3px;
}

tr {
  height: 50px;
  
}
</style>
