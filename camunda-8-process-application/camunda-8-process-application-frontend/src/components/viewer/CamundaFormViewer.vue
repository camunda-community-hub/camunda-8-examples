<script lang="ts" setup>
import { Form } from "@bpmn-io/form-js-viewer";
import { ref, computed, onMounted } from "vue";

type FormEvent = {
  data: any;
  errors: any;
};

const props = defineProps(["variables", "schema"]);
const emits = defineEmits(["errorMessage"]);

const container = ref(null);

const form = ref(null as any);

onMounted(async () => {
  const schema = props.schema;
  console.log("Schema", schema);
  const data = props.variables;
  console.log("Data", data);
  form.value = new Form({
    container: container.value,
  });
  form.value.importSchema(JSON.parse(schema), data);
  form.value.on("changed", ({ data, errors }: FormEvent) => {
    console.log("Data", data);
    console.log("Errors", errors);
    if (Object.keys(errors).length) {
      emits("errorMessage", "Please check the errors in the form");
    } else {
      emits("errorMessage");
    }
  });
});

defineExpose({
  variables: computed(() => {
    const { data } = form.value.submit();
    console.log("data", data);
    return data;
  }),
});
</script>

<template>
  <div ref="container" id="camunda-form"></div>
</template>

<style src="@bpmn-io/form-js-viewer/dist/assets/form-js.css">
.fjs-container {
  --background-color: black;
}
</style>
