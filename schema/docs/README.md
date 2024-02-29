# README

## Top-level Schemas

*   [Artifact](./artifact.md "Artifact 的类型可以是 NodeOutput、Table、Variable，都可以作为工作流节点的输入或者输出使用。") – `https://dataworks.data.aliyun.com/schemas/1.1.0/artifact.schema.json`

*   [FileResource](./fileresource.md "定义了文件资源, 如: jar, python, text file, archive files等") – `https://dataworks.data.aliyun.com/schemas/1.1.0/fileResource.schema.json`

*   [Flow](./flow.md "DataWorks 通用的工作流描述规范") – `https://dataworks.data.aliyun.com/schemas/1.1.0/flow.schema.json`

*   [Function](./function.md "定义工作流节点使用的UDF") – `https://dataworks.data.aliyun.com/schemas/1.1.0/script.schema.json`

*   [Node](./node.md "工作流节点的定义描述") – `https://dataworks.data.aliyun.com/schemas/1.1.0/node.schema.json`

*   [RuntimeResource](./runtimeresource.md "节点的运行时资源定义或引用") – `https://dataworks.data.aliyun.com/schemas/1.1.0/runtimeResource.schema.json`

*   [Script](./script.md "节点所需的脚本定义或者引用") – `https://dataworks.data.aliyun.com/schemas/1.1.0/script.schema.json`

*   [Trigger](./trigger.md "定义了工作流的触发器") – `https://dataworks.data.aliyun.com/schemas/1.1.0/trigger.schema.json`

## Other Schemas

### Objects

*   [Artifact.NodeOutput](./artifact-artifactnodeoutput.md "节点使用的上游节点的预定义输出") – `https://dataworks.data.aliyun.com/schemas/1.1.0/artifact.schema.json#/nodeOutput`

*   [Artifact.Table](./artifact-artifacttable.md "节点使用的上游产出表") – `https://dataworks.data.aliyun.com/schemas/1.1.0/artifact.schema.json#/table`

*   [Artifact.Variable](./artifact-artifactvariable.md "Variable定义了工作流的变量, Variable可以被在工作流节点中引用") – `https://dataworks.data.aliyun.com/schemas/1.1.0/artifact.schema.json#/variable`

*   [Flow.Metedata](./flow-properties-flowmetedata.md "定义工作流的扩展元信息") – `https://dataworks.data.aliyun.com/schemas/1.1.0/flow.schema.json#/properties/metadata`

*   [Flow.WorkflowSpec](./flow-properties-flowworkflowspec.md "Workflow 的 Spec 定义") – `https://dataworks.data.aliyun.com/schemas/1.1.0/flow.schema.json#/properties/spec`

*   [Flow.WorkflowSpec.Edge](./flow-properties-flowworkflowspec-properties-flowworkflowspecedges-flowworkflowspecedge.md) – `https://dataworks.data.aliyun.com/schemas/1.1.0/flow.schema.json#/properties/spec/properties/flow/items`

*   [Flow.WorkflowSpec.Edge.NodeDepend](./flow-properties-flowworkflowspec-properties-flowworkflowspecedges-flowworkflowspecedge-properties-flowworkflowspecedgenodedepends-flowworkflowspecedgenodedepend.md) – `https://dataworks.data.aliyun.com/schemas/1.1.0/flow.schema.json#/properties/spec/properties/flow/items/properties/depends/items`

*   [Node.InputArtifact](./node-properties-nodeinputartifact.md "节点的输入") – `https://dataworks.data.aliyun.com/schemas/1.1.0/node.schema.json#/properties/inputs`

*   [Node.OutputArtifact](./node-properties-nodeoutputartifact.md "输出定义了工作流节点的产出信息") – `https://dataworks.data.aliyun.com/schemas/1.1.0/node.schema.json#/properties/outputs`

*   [Script.Runtime](./script-properties-scriptruntime.md "脚本语言") – `https://dataworks.data.aliyun.com/schemas/1.1.0/script.schema.json#/properties/runtime`

### Arrays

*   [Flow.WorkflowSpec.Artifacts](./flow-properties-flowworkflowspec-properties-flowworkflowspecartifacts.md "定义工作流的变量") – `https://dataworks.data.aliyun.com/schemas/1.1.0/flow.schema.json#/properties/spec/properties/artifacts`

*   [Flow.WorkflowSpec.Edge.NodeDepends](./flow-properties-flowworkflowspec-properties-flowworkflowspecedges-flowworkflowspecedge-properties-flowworkflowspecedgenodedepends.md "节点的唯一标识符，全局唯一，用于标识工作流中的节点") – `https://dataworks.data.aliyun.com/schemas/1.1.0/flow.schema.json#/properties/spec/properties/flow/items/properties/depends`

*   [Flow.WorkflowSpec.Edges](./flow-properties-flowworkflowspec-properties-flowworkflowspecedges.md "工作流节点之间的依赖关系") – `https://dataworks.data.aliyun.com/schemas/1.1.0/flow.schema.json#/properties/spec/properties/flow`

*   [Flow.WorkflowSpec.FileResources](./flow-properties-flowworkflowspec-properties-flowworkflowspecfileresources.md "文件资源列表") – `https://dataworks.data.aliyun.com/schemas/1.1.0/flow.schema.json#/properties/spec/properties/fileResources`

*   [Flow.WorkflowSpec.Functions](./flow-properties-flowworkflowspec-properties-flowworkflowspecfunctions.md "函数定义列表") – `https://dataworks.data.aliyun.com/schemas/1.1.0/flow.schema.json#/properties/spec/properties/functions`

*   [Flow.WorkflowSpec.Nodes](./flow-properties-flowworkflowspec-properties-flowworkflowspecnodes.md "任务节点列表") – `https://dataworks.data.aliyun.com/schemas/1.1.0/flow.schema.json#/properties/spec/properties/nodes`

*   [Flow.WorkflowSpec.RuntimeResources](./flow-properties-flowworkflowspec-properties-flowworkflowspecruntimeresources.md "运行时资源定义列表") – `https://dataworks.data.aliyun.com/schemas/1.1.0/flow.schema.json#/properties/spec/properties/runtimeResources`

*   [Flow.WorkflowSpec.Scripts](./flow-properties-flowworkflowspec-properties-flowworkflowspecscripts.md "定义工作流的变量") – `https://dataworks.data.aliyun.com/schemas/1.1.0/flow.schema.json#/properties/spec/properties/scripts`

*   [Flow.WorkflowSpec.Triggers](./flow-then-properties-spec-properties-flowworkflowspectriggers.md "周期调度定义列表") – `https://dataworks.data.aliyun.com/schemas/1.1.0/flow.schema.json#/then/properties/spec/properties/triggers`

*   [Flow.WorkflowSpec.Variables](./flow-properties-flowworkflowspec-properties-flowworkflowspecvariables.md "定义工作流的变量") – `https://dataworks.data.aliyun.com/schemas/1.1.0/flow.schema.json#/properties/spec/properties/variables`

*   [Node.FileResources](./node-properties-nodefileresources.md "节点所需的文件资源定义或者引用") – `https://dataworks.data.aliyun.com/schemas/1.1.0/node.schema.json#/properties/fileResources`

*   [Node.Functions](./node-properties-nodefunctions.md "节点所需的函数定义或者引用") – `https://dataworks.data.aliyun.com/schemas/1.1.0/node.schema.json#/properties/functions`

*   [Node.InputArtifact.NodeOutputs](./node-properties-nodeinputartifact-properties-nodeinputartifactnodeoutputs.md "节点使用的上游节点的预定义输出列表") – `https://dataworks.data.aliyun.com/schemas/1.1.0/node.schema.json#/properties/inputs/properties/nodeOutputs`

*   [Node.InputArtifact.Tables](./node-properties-nodeinputartifact-properties-nodeinputartifacttables.md "节点使用的上游产出表列表") – `https://dataworks.data.aliyun.com/schemas/1.1.0/node.schema.json#/properties/inputs/properties/tables`

*   [Node.InputArtifact.Variables](./node-properties-nodeinputartifact-properties-nodeinputartifactvariables.md "节点依赖的变量Variable列表") – `https://dataworks.data.aliyun.com/schemas/1.1.0/node.schema.json#/properties/inputs/properties/variables`

*   [Node.OutputArtifact.NodeOutputs](./node-properties-nodeoutputartifact-properties-nodeoutputartifactnodeoutputs.md "节点使用的上游节点的预定义输出列表") – `https://dataworks.data.aliyun.com/schemas/1.1.0/node.schema.json#/properties/outputs/properties/nodeOutputs`

*   [Node.OutputArtifact.Tables](./node-properties-nodeoutputartifact-properties-nodeoutputartifacttables.md "节点使用的上游产出表列表") – `https://dataworks.data.aliyun.com/schemas/1.1.0/node.schema.json#/properties/outputs/properties/tables`

*   [Node.OutputArtifact.Variables](./node-properties-nodeoutputartifact-properties-nodeoutputartifactvariables.md "节点依赖的变量Variable列表") – `https://dataworks.data.aliyun.com/schemas/1.1.0/node.schema.json#/properties/outputs/properties/variables`

*   [Script.Parameters](./script-properties-scriptparameters.md "脚本参数列表") – `https://dataworks.data.aliyun.com/schemas/1.1.0/script.schema.json#/properties/parameters`
