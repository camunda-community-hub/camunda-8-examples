import  {Form} from '@bpmn-io/form-js-viewer';
import React, {useEffect, useRef} from 'react'
import CompleteUserTaskMutation from "../../integration/CompleteUserTaskMutation";
import {useMutation} from "@apollo/client";
function FormViewer(props) {

    const formElement = useRef(null)
    const form = new Form()
    const [completeTask, {error }] = useMutation(CompleteUserTaskMutation);

    useEffect(() => {
        const current = formElement.current
        if (current.children.length === 0) {
            form.attachTo(current)
            form.importSchema(props.schema, JSON.parse(props.data))
        }
        form.on('submit', (event) => {
            event.preventDefault();
            completeTask({
                variables: {
                    userTaskId: props.userTaskId,
                    variables: JSON.stringify(event.data)}
            });
         });
    },)

    if (error) return `Submission error! ${error.message}`;


    return (
            <div ref={formElement}></div>
    );
}

export default FormViewer;
