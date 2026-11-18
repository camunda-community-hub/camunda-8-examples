import React from "react";
import Tab from "./Tab";
import FormViewer from "../../Bpmn/FormViewer";
import DiagramViewer from "../../Bpmn/DiagramViewer";
import {useQuery} from "@apollo/client";
import DiagramQuery from "../../../integration/DiagramQuery";
import defaultForm from "../../../integration/DefaultForm";

function Tabs(props) {

    const processDefinitionId = props.task.processDefinitionId;
    let formKey;
    if(props.task.formKey === null){
        formKey = defaultForm;
        }
    else {
        try {
            formKey = JSON.parse(props.task.formKey);
        } catch (e) {
            formKey = defaultForm;
        }
        }

    const { loading, error, data } = useQuery(DiagramQuery(processDefinitionId));

    if (loading) return <p>Loading...</p>;
    if (error) return <p>Error : {error.message}</p>;

    return (
        <nav>
            <div className="nav nav-tabs" id="nav-tab" role="tablist">
                <button className="nav-link active" id={"nav-form-tab-" + props.task.userTaskId}
                        data-bs-toggle="tab"
                        data-bs-target={"#nav-form-" + props.task.userTaskId}
                        type="button"
                        role="tab"
                        aria-controls="nav-form"
                        aria-selected="true">Form
                </button>
                <button className="nav-link" id={"nav-diagram-tab-" + props.task.userTaskId}
                        data-bs-toggle="tab"
                        data-bs-target={"#nav-diagram-" + props.task.userTaskId}
                        type="button"
                        role="tab"
                        aria-controls="nav-diagram"
                        aria-selected="false">Diagram
                </button>
            </div>
            <div className="tab-content" id="nav-tabContent">
                <Tab
                    active={true}
                    label="form"
                    content={<FormViewer userTaskId= {props.task.userTaskId} schema={formKey} data={props.task.variables}></FormViewer>}
                    task={props.task}>
                </Tab>
                <Tab
                    label="diagram"
                    content={<DiagramViewer xml={data.diagramById.processDefinition} ></DiagramViewer>}
                    task={props.task}>
                </Tab>
            </div>
        </nav>
    );
}

export default Tabs;
