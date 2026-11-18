<script lang="ts" setup>
import { ref, reactive, computed, type ComputedRef } from "vue";

type VariableDto = {
    id: string;
    name: string;
    value: string;
    originalValue: string;
    state: Array<"Faulty" | "Changed" | "New">;
};

const props = defineProps(["task"]);
const emits = defineEmits(["errorMessage"]);


const variables = ref([] as Array<VariableDto>);

props.task.variables.forEach((e: any) => {
    variables.value.push({
        id: e.id,
        name: e.name,
        value: e.value,
        state: [],
        originalValue: e.value,
    });
});

const variablesToSubmit = ref([] as Array<{ name: string, value: string }>);

const faultyVariables = () => variables.value.filter((e) => e.state.includes("Faulty"));
const changedVariables = () => variables.value.filter((e) => e.state.includes("Changed")).map((e) => { return { name: e.name, value: e.value } });
const errorMessage = () =>
    faultyVariables().length == 0
        ? undefined
        : `Please check and correct these variables: ${faultyVariables()
            .map((e) => e.name)
            .join(", ")}`;

const onVariableUpdate = (variable: VariableDto) => {
    // FAULTY
    if (variable.state.includes("New") && variable.value === "") {

    } else {
        try {
            JSON.parse(variable.value);
            if (variable.state.includes("Faulty")) {
                variable.state.splice(variable.state.indexOf("Faulty"), 1);
            }
        } catch (error) {
            if (!variable.state.includes("Faulty")) {
                variable.state.push("Faulty");
            }
        }
    }

    // CHANGED
    if (variable.originalValue === variable.value) {
        if (variable.state.includes("Changed")) {
            variable.state.splice(variable.state.indexOf("Changed"), 1);
        }
    } else {
        if (!variable.state.includes("Changed")) {
            variable.state.push("Changed");
        }
    }
    emits("errorMessage", errorMessage());
    variablesToSubmit.value = changedVariables();
};

const addVariable = () => {
    console.log("Inserting new variable");
    const variable = {
        id: `new-${Math.round(Math.random() * 1000)}`,
        name: "",
        value: "",
        state: ["New"],
        originalValue: "",
    } as VariableDto;
    variables.value.push(variable);
    onVariableUpdate(variable);
};

const deleteVariable = (variable: VariableDto) => {
    variables.value.splice(
        variables.value.findIndex((e) => e.id === variable.id),
        1
    );
};

defineExpose({ variables: variablesToSubmit });
</script>

<template>
    <h3>Form Key: {{ task.formKey }}</h3>
    <h3>Assignee: {{ task.assignee }}</h3>
    <h3>Candidate Groups: {{ task.candidateGroups }}</h3>
    <table>
        <colgroup>
            <col class="variable-column" />
            <col class="value-column" />
        </colgroup>
        <tr v-for="variable in variables">
            <td class="variable-name">
                <h4 v-if="!variable.state.includes('New')">{{ variable.name }}</h4>
                <h4 v-else>
                    <input type="text" v-model="variable.name" @input="onVariableUpdate(variable)" /><button
                        @click="deleteVariable(variable)">x</button>
                </h4>
            </td>
            <td class="variable-value">
                <textarea type="text" :id="variable.id" v-model="variable.value" @input="onVariableUpdate(variable)"
                    :class="{
                        faulty: variable.state.includes('Faulty'),
                        changed: variable.state.includes('Changed'),
                    }" />
            </td>
        </tr>
    </table>
    <button @click="addVariable">+</button>
</template>

<style scoped>
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

.variable-name {
    max-width: 10%;
    text-align: right;
}

textarea {
    width: 90%;
    height: 90%;
    font-family: var(--font-family-monospace);
    padding: 10px;
    border-radius: 30px;
}

.faulty {
    color: var(--danger);
}

.changed {
    background-color: var(--yellow);
}
</style>
