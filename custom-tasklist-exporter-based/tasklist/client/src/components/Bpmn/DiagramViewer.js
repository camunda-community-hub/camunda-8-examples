import BpmnJS from "bpmn-js/lib/NavigatedViewer";
import {useEffect, useRef} from "react";

function DiagramViewer(props) {

    const diagramElement = useRef(null)

    const viewer = new BpmnJS();

    useEffect( () => {
        if (diagramElement.current.children.length === 0) {
            viewer.attachTo(diagramElement.current)
            viewer.importXML(props.xml);
        }
    })

    return (
            <div ref={diagramElement}></div>
    );
}

export default DiagramViewer