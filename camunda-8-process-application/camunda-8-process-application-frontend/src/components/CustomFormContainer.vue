<script lang="ts" setup>
import type { Ref } from "preact";
import { ref, computed } from "vue";
import CheckApplication from "./forms/CheckApplication.vue";

const props = defineProps(["task"]);
const emits = defineEmits(["errorMessage"]);

const form = ref(null as any);

const formKeyMapping: { [index: string]: any } = {
  checkApplication: CheckApplication,
};

const emitErrorMessage = (msg: string) => {
  console.log("Error Message: ", msg);
  emits("errorMessage", msg);
};

defineExpose({
  variables: computed(() => form.value.variables),
});
</script>

<template>
  <component
    ref="form"
    :is="formKeyMapping[task.formKey]"
    :task="task"
    :key="'' + task.id + task.formKey + '-customForm'"
    @errorMessage="emitErrorMessage"
  ></component>
</template>
