const bpmn = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
    "<bpmn:definitions xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" xmlns:bpmndi=\"http://www.omg.org/spec/BPMN/20100524/DI\" xmlns:dc=\"http://www.omg.org/spec/DD/20100524/DC\" xmlns:di=\"http://www.omg.org/spec/DD/20100524/DI\" xmlns:zeebe=\"http://camunda.org/schema/zeebe/1.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:modeler=\"http://camunda.org/schema/modeler/1.0\" id=\"Definitions_07eylac\" targetNamespace=\"http://bpmn.io/schema/bpmn\" exporter=\"Camunda Modeler\" exporterVersion=\"4.11.1\" modeler:executionPlatform=\"Camunda Cloud\" modeler:executionPlatformVersion=\"1.1.0\">\n" +
    "  <bpmn:process id=\"ApplicationProcess\" name=\"Application Process\" isExecutable=\"true\">\n" +
    "    <bpmn:extensionElements>\n" +
    "      <zeebe:userTaskForm id=\"userTaskForm_2lcds09\">{   \"schemaVersion\": 2,   \"components\": [     {       \"text\": \"# Review Application\",       \"type\": \"text\",       \"id\": \"Field_0j0h09s\"     },     {       \"label\": \"Application Id\",       \"type\": \"textfield\",       \"id\": \"Field_0mb144r\",       \"key\": \"applicationId\"     },     {       \"text\": \"## Applicant Information\",       \"type\": \"text\",       \"id\": \"Field_04uqzuj\"     },     {       \"label\": \"Applicant's Name\",       \"type\": \"textfield\",       \"id\": \"Field_1o52u87\",       \"key\": \"applicantName\"     },     {       \"label\": \"Applicant's Email\",       \"type\": \"textfield\",       \"id\": \"Field_1epkqk9\",       \"key\": \"applicantEmail\"     },     {       \"label\": \"Applicant Age\",       \"type\": \"textfield\",       \"id\": \"Field_13ctb55\",       \"key\": \"applicantAge\"     },     {       \"label\": \"Approve Application\",       \"type\": \"checkbox\",       \"id\": \"Field_13o5vxo\",       \"key\": \"approved\"     }   ],   \"type\": \"default\",   \"id\": \"ApplicationForm\",   \"executionPlatform\": \"Camunda Cloud\",   \"executionPlatformVersion\": \"1.2\" }</zeebe:userTaskForm>\n" +
    "    </bpmn:extensionElements>\n" +
    "    <bpmn:startEvent id=\"ProcessStartedStartEvent\" name=\"Application received\">\n" +
    "      <bpmn:outgoing>Flow_15tj0hw</bpmn:outgoing>\n" +
    "    </bpmn:startEvent>\n" +
    "    <bpmn:sequenceFlow id=\"Flow_15tj0hw\" sourceRef=\"ProcessStartedStartEvent\" targetRef=\"Activity_0jpftwp\" />\n" +
    "    <bpmn:sequenceFlow id=\"Flow_0n4rtd7\" sourceRef=\"Activity_00anggv\" targetRef=\"Gateway_0o2m74l\" />\n" +
    "    <bpmn:userTask id=\"Activity_00anggv\" name=\"Review Application\">\n" +
    "      <bpmn:extensionElements>\n" +
    "        <zeebe:formDefinition formKey=\"camunda-forms:bpmn:userTaskForm_2lcds09\" />\n" +
    "      </bpmn:extensionElements>\n" +
    "      <bpmn:incoming>Flow_1h7lrn0</bpmn:incoming>\n" +
    "      <bpmn:outgoing>Flow_0n4rtd7</bpmn:outgoing>\n" +
    "    </bpmn:userTask>\n" +
    "    <bpmn:sequenceFlow id=\"Flow_0ets4df\" sourceRef=\"Activity_0jpftwp\" targetRef=\"Gateway_1tey6rk\" />\n" +
    "    <bpmn:serviceTask id=\"Activity_0jpftwp\" name=\"Retrieve Credit Score\">\n" +
    "      <bpmn:extensionElements>\n" +
    "        <zeebe:taskDefinition type=\"creditscore\" />\n" +
    "      </bpmn:extensionElements>\n" +
    "      <bpmn:incoming>Flow_15tj0hw</bpmn:incoming>\n" +
    "      <bpmn:outgoing>Flow_0ets4df</bpmn:outgoing>\n" +
    "    </bpmn:serviceTask>\n" +
    "    <bpmn:exclusiveGateway id=\"Gateway_1tey6rk\" name=\"Score?\">\n" +
    "      <bpmn:incoming>Flow_0ets4df</bpmn:incoming>\n" +
    "      <bpmn:outgoing>Flow_1h7lrn0</bpmn:outgoing>\n" +
    "      <bpmn:outgoing>Flow_1qbgho7</bpmn:outgoing>\n" +
    "      <bpmn:outgoing>Flow_14ms2nk</bpmn:outgoing>\n" +
    "    </bpmn:exclusiveGateway>\n" +
    "    <bpmn:sequenceFlow id=\"Flow_1h7lrn0\" name=\"[61..84]\" sourceRef=\"Gateway_1tey6rk\" targetRef=\"Activity_00anggv\">\n" +
    "      <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\">=score between 61 and 84</bpmn:conditionExpression>\n" +
    "    </bpmn:sequenceFlow>\n" +
    "    <bpmn:endEvent id=\"Event_0ipbr3b\" name=\"Application accepted\">\n" +
    "      <bpmn:incoming>Flow_1s1j6rm</bpmn:incoming>\n" +
    "    </bpmn:endEvent>\n" +
    "    <bpmn:sequenceFlow id=\"Flow_1qbgho7\" name=\"&#62;= 85\" sourceRef=\"Gateway_1tey6rk\" targetRef=\"Gateway_1at6691\">\n" +
    "      <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\">=score&gt;=85</bpmn:conditionExpression>\n" +
    "    </bpmn:sequenceFlow>\n" +
    "    <bpmn:endEvent id=\"Event_0hbt7qq\" name=\"Application rejected\">\n" +
    "      <bpmn:incoming>Flow_0q42znu</bpmn:incoming>\n" +
    "    </bpmn:endEvent>\n" +
    "    <bpmn:sequenceFlow id=\"Flow_14ms2nk\" name=\"&#60;=60\" sourceRef=\"Gateway_1tey6rk\" targetRef=\"Gateway_15zg7r6\">\n" +
    "      <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\">=score&lt;=60</bpmn:conditionExpression>\n" +
    "    </bpmn:sequenceFlow>\n" +
    "    <bpmn:exclusiveGateway id=\"Gateway_0o2m74l\" name=\"Decision?\">\n" +
    "      <bpmn:incoming>Flow_0n4rtd7</bpmn:incoming>\n" +
    "      <bpmn:outgoing>Flow_0n5bsuq</bpmn:outgoing>\n" +
    "      <bpmn:outgoing>Flow_19045iq</bpmn:outgoing>\n" +
    "    </bpmn:exclusiveGateway>\n" +
    "    <bpmn:exclusiveGateway id=\"Gateway_15zg7r6\">\n" +
    "      <bpmn:incoming>Flow_14ms2nk</bpmn:incoming>\n" +
    "      <bpmn:incoming>Flow_19045iq</bpmn:incoming>\n" +
    "      <bpmn:outgoing>Flow_0q42znu</bpmn:outgoing>\n" +
    "    </bpmn:exclusiveGateway>\n" +
    "    <bpmn:sequenceFlow id=\"Flow_0q42znu\" sourceRef=\"Gateway_15zg7r6\" targetRef=\"Event_0hbt7qq\" />\n" +
    "    <bpmn:exclusiveGateway id=\"Gateway_1at6691\">\n" +
    "      <bpmn:incoming>Flow_1qbgho7</bpmn:incoming>\n" +
    "      <bpmn:incoming>Flow_0n5bsuq</bpmn:incoming>\n" +
    "      <bpmn:outgoing>Flow_1s1j6rm</bpmn:outgoing>\n" +
    "    </bpmn:exclusiveGateway>\n" +
    "    <bpmn:sequenceFlow id=\"Flow_1s1j6rm\" sourceRef=\"Gateway_1at6691\" targetRef=\"Event_0ipbr3b\" />\n" +
    "    <bpmn:sequenceFlow id=\"Flow_0n5bsuq\" name=\"approved\" sourceRef=\"Gateway_0o2m74l\" targetRef=\"Gateway_1at6691\">\n" +
    "      <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\">=approved = true</bpmn:conditionExpression>\n" +
    "    </bpmn:sequenceFlow>\n" +
    "    <bpmn:sequenceFlow id=\"Flow_19045iq\" name=\"rejected\" sourceRef=\"Gateway_0o2m74l\" targetRef=\"Gateway_15zg7r6\">\n" +
    "      <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\">= approved = false</bpmn:conditionExpression>\n" +
    "    </bpmn:sequenceFlow>\n" +
    "  </bpmn:process>\n" +
    "  <bpmndi:BPMNDiagram id=\"BPMNDiagram_1\">\n" +
    "    <bpmndi:BPMNPlane id=\"BPMNPlane_1\" bpmnElement=\"ApplicationProcess\">\n" +
    "      <bpmndi:BPMNEdge id=\"Flow_0n4rtd7_di\" bpmnElement=\"Flow_0n4rtd7\">\n" +
    "        <di:waypoint x=\"790\" y=\"277\" />\n" +
    "        <di:waypoint x=\"895\" y=\"277\" />\n" +
    "      </bpmndi:BPMNEdge>\n" +
    "      <bpmndi:BPMNEdge id=\"Flow_15tj0hw_di\" bpmnElement=\"Flow_15tj0hw\">\n" +
    "        <di:waypoint x=\"198\" y=\"277\" />\n" +
    "        <di:waypoint x=\"320\" y=\"277\" />\n" +
    "      </bpmndi:BPMNEdge>\n" +
    "      <bpmndi:BPMNEdge id=\"Flow_0ets4df_di\" bpmnElement=\"Flow_0ets4df\">\n" +
    "        <di:waypoint x=\"420\" y=\"277\" />\n" +
    "        <di:waypoint x=\"495\" y=\"277\" />\n" +
    "      </bpmndi:BPMNEdge>\n" +
    "      <bpmndi:BPMNEdge id=\"Flow_1h7lrn0_di\" bpmnElement=\"Flow_1h7lrn0\">\n" +
    "        <di:waypoint x=\"545\" y=\"277\" />\n" +
    "        <di:waypoint x=\"690\" y=\"277\" />\n" +
    "        <bpmndi:BPMNLabel>\n" +
    "          <dc:Bounds x=\"599\" y=\"259\" width=\"37\" height=\"14\" />\n" +
    "        </bpmndi:BPMNLabel>\n" +
    "      </bpmndi:BPMNEdge>\n" +
    "      <bpmndi:BPMNEdge id=\"Flow_1qbgho7_di\" bpmnElement=\"Flow_1qbgho7\">\n" +
    "        <di:waypoint x=\"520\" y=\"252\" />\n" +
    "        <di:waypoint x=\"520\" y=\"110\" />\n" +
    "        <di:waypoint x=\"895\" y=\"110\" />\n" +
    "        <bpmndi:BPMNLabel>\n" +
    "          <dc:Bounds x=\"535\" y=\"178\" width=\"29\" height=\"14\" />\n" +
    "        </bpmndi:BPMNLabel>\n" +
    "      </bpmndi:BPMNEdge>\n" +
    "      <bpmndi:BPMNEdge id=\"Flow_14ms2nk_di\" bpmnElement=\"Flow_14ms2nk\">\n" +
    "        <di:waypoint x=\"520\" y=\"302\" />\n" +
    "        <di:waypoint x=\"520\" y=\"460\" />\n" +
    "        <di:waypoint x=\"895\" y=\"460\" />\n" +
    "        <bpmndi:BPMNLabel>\n" +
    "          <dc:Bounds x=\"537\" y=\"393\" width=\"26\" height=\"14\" />\n" +
    "        </bpmndi:BPMNLabel>\n" +
    "      </bpmndi:BPMNEdge>\n" +
    "      <bpmndi:BPMNEdge id=\"Flow_0q42znu_di\" bpmnElement=\"Flow_0q42znu\">\n" +
    "        <di:waypoint x=\"945\" y=\"460\" />\n" +
    "        <di:waypoint x=\"1002\" y=\"460\" />\n" +
    "      </bpmndi:BPMNEdge>\n" +
    "      <bpmndi:BPMNEdge id=\"Flow_1s1j6rm_di\" bpmnElement=\"Flow_1s1j6rm\">\n" +
    "        <di:waypoint x=\"945\" y=\"110\" />\n" +
    "        <di:waypoint x=\"1002\" y=\"110\" />\n" +
    "      </bpmndi:BPMNEdge>\n" +
    "      <bpmndi:BPMNEdge id=\"Flow_0n5bsuq_di\" bpmnElement=\"Flow_0n5bsuq\">\n" +
    "        <di:waypoint x=\"920\" y=\"252\" />\n" +
    "        <di:waypoint x=\"920\" y=\"135\" />\n" +
    "        <bpmndi:BPMNLabel>\n" +
    "          <dc:Bounds x=\"926\" y=\"191\" width=\"47\" height=\"14\" />\n" +
    "        </bpmndi:BPMNLabel>\n" +
    "      </bpmndi:BPMNEdge>\n" +
    "      <bpmndi:BPMNEdge id=\"Flow_19045iq_di\" bpmnElement=\"Flow_19045iq\">\n" +
    "        <di:waypoint x=\"920\" y=\"302\" />\n" +
    "        <di:waypoint x=\"920\" y=\"435\" />\n" +
    "        <bpmndi:BPMNLabel>\n" +
    "          <dc:Bounds x=\"930\" y=\"366\" width=\"40\" height=\"14\" />\n" +
    "        </bpmndi:BPMNLabel>\n" +
    "      </bpmndi:BPMNEdge>\n" +
    "      <bpmndi:BPMNShape id=\"Activity_0wp9a7e_di\" bpmnElement=\"Activity_0jpftwp\">\n" +
    "        <dc:Bounds x=\"320\" y=\"237\" width=\"100\" height=\"80\" />\n" +
    "      </bpmndi:BPMNShape>\n" +
    "      <bpmndi:BPMNShape id=\"Gateway_1tey6rk_di\" bpmnElement=\"Gateway_1tey6rk\" isMarkerVisible=\"true\">\n" +
    "        <dc:Bounds x=\"495\" y=\"252\" width=\"50\" height=\"50\" />\n" +
    "        <bpmndi:BPMNLabel>\n" +
    "          <dc:Bounds x=\"472\" y=\"243\" width=\"36\" height=\"14\" />\n" +
    "        </bpmndi:BPMNLabel>\n" +
    "      </bpmndi:BPMNShape>\n" +
    "      <bpmndi:BPMNShape id=\"Activity_11omzdt_di\" bpmnElement=\"Activity_00anggv\">\n" +
    "        <dc:Bounds x=\"690\" y=\"237\" width=\"100\" height=\"80\" />\n" +
    "      </bpmndi:BPMNShape>\n" +
    "      <bpmndi:BPMNShape id=\"Gateway_0o2m74l_di\" bpmnElement=\"Gateway_0o2m74l\" isMarkerVisible=\"true\">\n" +
    "        <dc:Bounds x=\"895\" y=\"252\" width=\"50\" height=\"50\" />\n" +
    "        <bpmndi:BPMNLabel>\n" +
    "          <dc:Bounds x=\"955\" y=\"270\" width=\"49\" height=\"14\" />\n" +
    "        </bpmndi:BPMNLabel>\n" +
    "      </bpmndi:BPMNShape>\n" +
    "      <bpmndi:BPMNShape id=\"Gateway_15zg7r6_di\" bpmnElement=\"Gateway_15zg7r6\" isMarkerVisible=\"true\">\n" +
    "        <dc:Bounds x=\"895\" y=\"435\" width=\"50\" height=\"50\" />\n" +
    "      </bpmndi:BPMNShape>\n" +
    "      <bpmndi:BPMNShape id=\"Gateway_1at6691_di\" bpmnElement=\"Gateway_1at6691\" isMarkerVisible=\"true\">\n" +
    "        <dc:Bounds x=\"895\" y=\"85\" width=\"50\" height=\"50\" />\n" +
    "      </bpmndi:BPMNShape>\n" +
    "      <bpmndi:BPMNShape id=\"Event_0ipbr3b_di\" bpmnElement=\"Event_0ipbr3b\">\n" +
    "        <dc:Bounds x=\"1002\" y=\"92\" width=\"36\" height=\"36\" />\n" +
    "        <bpmndi:BPMNLabel>\n" +
    "          <dc:Bounds x=\"993\" y=\"135\" width=\"54\" height=\"27\" />\n" +
    "        </bpmndi:BPMNLabel>\n" +
    "      </bpmndi:BPMNShape>\n" +
    "      <bpmndi:BPMNShape id=\"Event_0hbt7qq_di\" bpmnElement=\"Event_0hbt7qq\">\n" +
    "        <dc:Bounds x=\"1002\" y=\"442\" width=\"36\" height=\"36\" />\n" +
    "        <bpmndi:BPMNLabel>\n" +
    "          <dc:Bounds x=\"993\" y=\"485\" width=\"54\" height=\"27\" />\n" +
    "        </bpmndi:BPMNLabel>\n" +
    "      </bpmndi:BPMNShape>\n" +
    "      <bpmndi:BPMNShape id=\"_BPMNShape_StartEvent_2\" bpmnElement=\"ProcessStartedStartEvent\">\n" +
    "        <dc:Bounds x=\"162\" y=\"259\" width=\"36\" height=\"36\" />\n" +
    "        <bpmndi:BPMNLabel>\n" +
    "          <dc:Bounds x=\"155\" y=\"302\" width=\"53\" height=\"27\" />\n" +
    "        </bpmndi:BPMNLabel>\n" +
    "      </bpmndi:BPMNShape>\n" +
    "    </bpmndi:BPMNPlane>\n" +
    "  </bpmndi:BPMNDiagram>\n" +
    "</bpmn:definitions>"

export default bpmn