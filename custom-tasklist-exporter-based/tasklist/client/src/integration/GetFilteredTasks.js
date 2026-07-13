import {gql} from "@apollo/client";

const GET_FILTERED_TASKS = gql`
    query FilteredTasks($source: String, $eventType: EventType,  $page: Int, $size: Int) {
        filteredUserTasks(source: $source , eventType: $eventType, page: $page, size: $size) {
            userTaskId
            taskElementName
            source
            assignee
            priority
            eventType
            dueDate
            candidateUsers
            candidateGroups
            processInstanceId
            processDefinitionId
            formKey
            variables
        }
    }
`;
export default GET_FILTERED_TASKS