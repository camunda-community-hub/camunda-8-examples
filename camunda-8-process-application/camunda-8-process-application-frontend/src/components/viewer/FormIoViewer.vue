<script lang="ts" setup>
import { Formio } from "formiojs";
import { computed, onMounted, ref } from "vue";

const props = defineProps(["variables", "formUrl"]);

const container = ref(null);

const form = ref(null as any);

const variablesToSubmit = ref({} as any);

onMounted(async () => {
  form.value = await Formio.createForm(
    document.getElementById("formio-form"),
    props.formUrl as string,
    {}
  );
  form.value.submission = {
    data: props.variables,
  };
  form.value.on("change", onVariableUpdate);
});

defineExpose({
  variables: computed(() => {
    return variablesToSubmit.value;
  }),
});

const onVariableUpdate = (changed: any) => {
  variablesToSubmit.value = {};
  Object.keys(changed.data).forEach((key) => {
    console.log(`Checking ${key}`);
    const variable = changed.data[key];
    console.log("Value is ", variable);
    // check if variable exists
    const existingVariable = props.variables[key];
    // if not, add to submit
    if (!existingVariable) {
      console.log("=> new");
      variablesToSubmit.value[key] = variable;
      // if yes, check if changed
    } else if (
      existingVariable !== variable &&
      JSON.stringify(existingVariable) !== JSON.stringify(variable)
    ) {
      console.log("=> changed");
      // if yes, add to submit
      variablesToSubmit.value[key] = variable;
    } else {
      console.log("=> unchanged");
    }
  });
  console.log(variablesToSubmit.value);
};
</script>

<template>
  <div ref="container" id="formio-form"></div>
</template>

<style src="formiojs/dist/formio.full.min.css"></style>
