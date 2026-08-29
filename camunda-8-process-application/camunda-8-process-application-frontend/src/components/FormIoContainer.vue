<script lang="ts" setup>
import { computed, ref, type Ref } from "vue";
import FormIoViewer from "./viewer/FormIoViewer.vue";

type Variable = {
  name: string;
  value: string;
};

const props = defineProps(["task"]);
const emits = defineEmits(["errorMessage"]);

const viewer = ref(null as any);

const formKey = ref(props.task.formKey) as Ref<string>;

const clearedFormKey = ref(formKey.value.split(":")[2]);

const variables = computed(() => {
  const raw = props.task.variables as Array<Variable>;
  return raw.reduce(
    (map, obj) => ((map[obj.name] = JSON.parse(obj.value)), map),
    {} as any
  );
});

const formUrl = "https://lusgblinjtyawce.form.io/" + clearedFormKey.value;

defineExpose({
  variables: computed(() => {
    const raw = viewer.value.variables as any;
    console.log("Raw data", raw);

    const v = [];

    for (const key in raw) {
      const value = raw[key];
      v.push({ name: key, value: JSON.stringify(value) });
    }
    console.log("Variables to submit", v);
    return v;
  }),
});

const emitErrorMessage = (msg: string) => {
  console.log("Error Message: ", msg);
  emits("errorMessage", msg);
};
</script>

<template>
  <FormIoViewer
    :variables="variables"
    :formUrl="formUrl"
    ref="viewer"
    @errorMessage="emitErrorMessage"
  ></FormIoViewer>
</template>
