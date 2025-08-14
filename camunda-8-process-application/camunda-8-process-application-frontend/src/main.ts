import { createApp, provide, h } from "vue";
import { ApolloClient, createHttpLink, InMemoryCache } from '@apollo/client/core'
import { DefaultApolloClient, provideApolloClient } from '@vue/apollo-composable'

// HTTP connection to the API
const httpLink = createHttpLink({
    // You should use an absolute URL here
    uri: '/graphql',
})

// Cache implementation
const cache = new InMemoryCache({
    typePolicies: {
        Query: {
            fields: {
                tasks: {
                    merge: false
                }
            }

        }
    }
})

// Create the apollo client
export const apolloClient = new ApolloClient({
    link: httpLink,
    cache,
})


import App from "./App.vue";

const app = createApp({
    setup() {
        provideApolloClient(apolloClient)
    },

    render: () => h(App),
})
app.mount("#app");
