import {gql} from "@apollo/client";

function diagramById(id) {
    return gql`
        query{diagramById(diagramId: \"${id}\") {
            diagramId
            processDefinition
        }}
    `;
}

export default diagramById;