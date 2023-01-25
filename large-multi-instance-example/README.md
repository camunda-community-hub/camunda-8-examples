# Multi-instance Processing of a (Very) Large Sequence

Example to show concepts for processing a very large sequence of entities with a multi-instance subprocess.

## Process modeling for large sequences

Right now (Jan 2023) the Zeebe engine can not distribute the load of a multi-instance sequence over all nodes in the cluster.

All subprocesses or tasks are handled on the same node.

If you have a large sequence of items to process, you have to split the sequence into smaller chucks to be handled on a single node and 
use messaging to distribute the load over all cluster nodes.

<img src="documentation/modeling-concept.png" alt="concept for large multi-instance processing" width="100%" />

For this example I choose a campaign that should generate 10000 letters to be send by mail. 
The Campaign Process will divide the elements in a number of buckets. Each bucket contains up to 2000 elements. 
The Letter Process will handle only a single letter.

The example doesn't collect the elements from the multi-instance processing. The results are saved as ids in the lowest level letter process, that creates just a single letter.

A variable called `businessKey` helps to identify all processes that belong to a single campaign.

<img src="documentation/operate-processes-with-business-key.png" alt="operate with businessKey=3" width="100%" />

## Implementation

To minimize the data load per element, some additional modeling hints will help.

In the super process, only you will get the begin and end indexes for each bucket. 

In the multi-instance scope, you can overwrite the list with a dummy value via an input mapping. 
Otherwise, the complete list will be passed to the subprocess.
This is a huge overload, as from now on only the element of the multi-instance is required.

![multi-instance-configuration](documentation/multi-instance-configuration.png)

In the first subprocess, create the list of items, that should be handled in the multi-instance call activity.

In the inner multi-instance call activity, you should again overwrite the list variable with a dummy value.

## Return results from the multi-instance call activity

In case you want to collect the results from the multi-instance call activity, 
currently (Jan 2023) only the last element is in the list, all other elements are null.

There is an issue filed for this: https://github.com/camunda/zeebe/issues/11476

The [multi-instance-simple-example process model](src/test/resources/multi-instance-simple-example.bpmn) contains a failing diagram and a workaround: 
Wrap the call activity into a multi-instance subprocess. 

The subprocess will collect all output elements into the list.

There is also a test to proof if future Zeebe versions have resolved this issue: 
[SimpleMultiInstanceTest.java](src/test/java/com/camunda/consulting/SimpleMultiInstanceTest.java)

The failing test is disabled to let the build pass through.