import {gql} from "@apollo/client";

const CompleteUserTaskMutation =
    gql`
        mutation completeUserTask($userTaskId: ID! $variables: String) {
            completeUserTask(userTaskId: $userTaskId variables: $variables) {
                userTaskId
                processInstanceId
                eventType
                source
            }
        }
    `;

export default CompleteUserTaskMutation;