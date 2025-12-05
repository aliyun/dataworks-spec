package com.aliyun.dataworks.migrationx.domain.dataworks.service.spec.entity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.common.spec.domain.enums.VariableScopeType;
import com.aliyun.dataworks.common.spec.domain.ref.SpecScript;
import com.aliyun.dataworks.common.spec.domain.ref.SpecVariable;
import com.aliyun.dataworks.common.spec.domain.ref.component.SpecComponent;
import com.aliyun.dataworks.common.spec.domain.ref.runtime.SpecScriptRuntime;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.NodeUseType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class SpecComponentEntityAdapterTest {

    @Test
    public void testGetUuid_withComponent() {
        // Given
        SpecComponentEntityAdapter adapter = new SpecComponentEntityAdapter();
        SpecComponent component = new SpecComponent();
        component.setId("test-uuid");
        adapter.setComponent(component);

        // When
        String uuid = adapter.getUuid();

        // Then
        assertEquals("test-uuid", uuid);
    }

    @Test
    public void testGetUuid_withoutComponent() {
        // Given
        SpecComponentEntityAdapter adapter = new SpecComponentEntityAdapter();

        // When
        String uuid = adapter.getUuid();

        // Then
        assertNull(uuid);
    }

    @Test
    public void testGetName_withComponent() {
        // Given
        SpecComponentEntityAdapter adapter = new SpecComponentEntityAdapter();
        SpecComponent component = new SpecComponent();
        component.setName("test-name");
        adapter.setComponent(component);

        // When
        String name = adapter.getName();

        // Then
        assertEquals("test-name", name);
    }

    @Test
    public void testGetName_withoutComponent() {
        // Given
        SpecComponentEntityAdapter adapter = new SpecComponentEntityAdapter();

        // When
        String name = adapter.getName();

        // Then
        assertNull(name);
    }

    @Test
    public void testGetType_withRuntimeCommand() {
        // Given
        SpecComponentEntityAdapter adapter = new SpecComponentEntityAdapter();
        SpecComponent component = new SpecComponent();
        SpecScript script = new SpecScript();
        SpecScriptRuntime runtime = new SpecScriptRuntime();

        runtime.setCommand("SHELL");
        script.setRuntime(runtime);
        component.setScript(script);

        adapter.setComponent(component);

        // When
        String type = adapter.getType();

        // Then
        assertEquals("SHELL", type);
    }

    @Test
    public void testGetType_withCommandTypeId() {
        // Given
        SpecComponentEntityAdapter adapter = new SpecComponentEntityAdapter();
        SpecComponent component = new SpecComponent();
        SpecScript script = new SpecScript();
        SpecScriptRuntime runtime = new SpecScriptRuntime();

        runtime.setCommandTypeId(CodeProgramType.ODPS_SQL.getCode());
        runtime.setCommand(null); // Ensure command is null to test commandTypeId fallback

        script.setRuntime(runtime);
        component.setScript(script);

        adapter.setComponent(component);

        // When
        String type = adapter.getType();

        // Then
        assertEquals(CodeProgramType.ODPS_SQL.name(), type);
    }

    @Test
    public void testGetType_withoutComponent() {
        // Given
        SpecComponentEntityAdapter adapter = new SpecComponentEntityAdapter();

        // When
        String type = adapter.getType();

        // Then
        assertNull(type);
    }

    @Test
    public void testGetTypeId_withCommandTypeId() {
        // Given
        SpecComponentEntityAdapter adapter = new SpecComponentEntityAdapter();
        SpecComponent component = new SpecComponent();
        SpecScript script = new SpecScript();
        SpecScriptRuntime runtime = new SpecScriptRuntime();

        runtime.setCommandTypeId(5); // ODPS_SQL code

        script.setRuntime(runtime);
        component.setScript(script);

        adapter.setComponent(component);

        // When
        Integer typeId = adapter.getTypeId();

        // Then
        assertEquals(Integer.valueOf(5), typeId);
    }

    @Test
    public void testGetTypeId_withCommand() {
        // Given
        SpecComponentEntityAdapter adapter = new SpecComponentEntityAdapter();
        SpecComponent component = new SpecComponent();
        SpecScript script = new SpecScript();
        SpecScriptRuntime runtime = new SpecScriptRuntime();

        runtime.setCommand("ODPS_SQL");
        runtime.setCommandTypeId(null); // Ensure commandTypeId is null to test command fallback

        script.setRuntime(runtime);
        component.setScript(script);

        adapter.setComponent(component);

        // When
        Integer typeId = adapter.getTypeId();

        // Then
        assertEquals(Integer.valueOf(CodeProgramType.ODPS_SQL.getCode()), typeId);
    }

    @Test
    public void testGetTypeId_withoutComponent() {
        // Given
        SpecComponentEntityAdapter adapter = new SpecComponentEntityAdapter();

        // When
        Integer typeId = adapter.getTypeId();

        // Then
        assertNull(typeId);
    }

    @Test
    public void testGetFolder_withPath() {
        // Given
        SpecComponentEntityAdapter adapter = new SpecComponentEntityAdapter();
        SpecComponent component = new SpecComponent();
        SpecScript script = new SpecScript();

        script.setPath("/path/to/component/file.sql");

        component.setScript(script);

        adapter.setComponent(component);

        // When
        String folder = adapter.getFolder();

        // Then
        assertEquals("/path/to/component", folder);
    }

    @Test
    public void testGetFolder_withoutScript() {
        // Given
        SpecComponentEntityAdapter adapter = new SpecComponentEntityAdapter();
        SpecComponent component = new SpecComponent();

        component.setScript(null);

        adapter.setComponent(component);

        // When
        String folder = adapter.getFolder();

        // Then
        assertNull(folder);
    }

    @Test
    public void testGetFolder_withoutComponent() {
        // Given
        SpecComponentEntityAdapter adapter = new SpecComponentEntityAdapter();

        // When
        String folder = adapter.getFolder();

        // Then
        assertNull(folder);
    }

    @Test
    public void testGetCode_withComponent() {
        // Given
        SpecComponentEntityAdapter adapter = new SpecComponentEntityAdapter();
        SpecComponent component = new SpecComponent();
        SpecScript script = new SpecScript();

        script.setContent("SELECT * FROM table;");
        component.setName("test-component");
        component.setOwner("test-owner");

        component.setScript(script);

        adapter.setComponent(component);

        // When
        String code = adapter.getCode();

        // Then
        // Verify that code is not null and not empty
        assertNotNull(code);
        assertTrue(code.startsWith("{"));
        assertTrue(code.contains("SELECT"));  // Content should be present
    }

    @Test
    public void testGetCode_withoutComponent() {
        // Given
        SpecComponentEntityAdapter adapter = new SpecComponentEntityAdapter();

        // When
        String code = adapter.getCode();

        // Then
        assertEquals("{}", code); // When component is null, an empty object is returned
    }

    @Test
    public void testGetParameter_withNodeParameters() {
        // Given
        SpecComponentEntityAdapter adapter = new SpecComponentEntityAdapter();
        SpecComponent component = new SpecComponent();
        SpecScript script = new SpecScript();

        SpecVariable param1 = new SpecVariable();
        param1.setName("param1");
        param1.setValue("value1");
        param1.setScope(VariableScopeType.NODE_PARAMETER);

        SpecVariable param2 = new SpecVariable();
        param2.setName("param2");
        param2.setValue("value2");
        param2.setScope(VariableScopeType.NODE_PARAMETER);

        List<SpecVariable> parameters = Arrays.asList(param1, param2);

        script.setParameters(parameters);
        component.setScript(script);

        adapter.setComponent(component);

        // When
        String parameter = adapter.getParameter();

        // Then
        // The order might be different, so check if both parameters are present
        assertTrue(parameter.contains("param1=value1"));
        assertTrue(parameter.contains("param2=value2"));
        assertTrue(parameter.contains(" "));
    }

    @Test
    public void testGetParameter_withNonNodeParameter() {
        // Given
        SpecComponentEntityAdapter adapter = new SpecComponentEntityAdapter();
        SpecComponent component = new SpecComponent();
        SpecScript script = new SpecScript();

        SpecVariable param1 = new SpecVariable();
        param1.setName("param1");
        param1.setValue("value1");
        param1.setScope(VariableScopeType.TENANT); // Not a node parameter

        List<SpecVariable> parameters = Collections.singletonList(param1);

        script.setParameters(parameters);
        component.setScript(script);

        adapter.setComponent(component);

        // When
        String parameter = adapter.getParameter();

        // Then
        assertEquals("", parameter); // When no node parameters exist, an empty string is returned
    }

    @Test
    public void testGetParameter_withoutComponent() {
        // Given
        SpecComponentEntityAdapter adapter = new SpecComponentEntityAdapter();

        // When
        String parameter = adapter.getParameter();

        // Then
        assertNull(parameter);
    }

    @Test
    public void testGetParameter_withoutScript() {
        // Given
        SpecComponentEntityAdapter adapter = new SpecComponentEntityAdapter();
        SpecComponent component = new SpecComponent();

        component.setScript(null);

        adapter.setComponent(component);

        // When
        String parameter = adapter.getParameter();

        // Then
        assertNull(parameter);
    }

    @Test
    public void testGetDescription_withComponent() {
        // Given
        SpecComponentEntityAdapter adapter = new SpecComponentEntityAdapter();
        SpecComponent component = new SpecComponent();

        component.setDescription("test description");

        adapter.setComponent(component);

        // When
        String description = adapter.getDescription();

        // Then
        assertEquals("test description", description);
    }

    @Test
    public void testGetDescription_withoutComponent() {
        // Given
        SpecComponentEntityAdapter adapter = new SpecComponentEntityAdapter();

        // When
        String description = adapter.getDescription();

        // Then
        assertNull(description);
    }

    @Test
    public void testGetOwner_withComponent() {
        // Given
        SpecComponentEntityAdapter adapter = new SpecComponentEntityAdapter();
        SpecComponent component = new SpecComponent();

        component.setOwner("test-owner");

        adapter.setComponent(component);

        // When
        String owner = adapter.getOwner();

        // Then
        assertEquals("test-owner", owner);
    }

    @Test
    public void testGetOwner_withoutComponent() {
        // Given
        SpecComponentEntityAdapter adapter = new SpecComponentEntityAdapter();

        // When
        String owner = adapter.getOwner();

        // Then
        assertNull(owner);
    }

    @Test
    public void testGetUseType() {
        // Given
        SpecComponentEntityAdapter adapter = new SpecComponentEntityAdapter();

        // When
        NodeUseType nodeUseType = adapter.getNodeUseType();

        // Then
        assertEquals(NodeUseType.COMPONENT, nodeUseType);
    }

    @Test
    public void testGetComponent_withComponent() {
        // Given
        SpecComponentEntityAdapter adapter = new SpecComponentEntityAdapter();
        SpecComponent component = new SpecComponent();

        adapter.setComponent(component);

        // When
        SpecComponent retrievedComponent = adapter.getComponent();

        // Then
        assertEquals(component, retrievedComponent);
    }

    @Test
    public void testAllMethodsThatReturnNull() {
        // Given
        SpecComponentEntityAdapter adapter = new SpecComponentEntityAdapter();

        // When & Then - Testing all methods that return null
        assertNull(adapter.getBizId());
        assertNull(adapter.getBizName());
        assertNull(adapter.getCronExpress());
        assertNull(adapter.getStartEffectDate());
        assertNull(adapter.getIsAutoParse());
        assertNull(adapter.getEndEffectDate());
        assertNull(adapter.getResourceGroup());
        assertNull(adapter.getResourceGroupName());
        assertNull(adapter.getDiResourceGroup());
        assertNull(adapter.getDiResourceGroupName());
        assertNull(adapter.getCodeMode());
        assertNull(adapter.getStartRightNow());
        assertNull(adapter.getRerunMode());
        assertNull(adapter.getNodeType());
        assertNull(adapter.getPauseSchedule());
        assertNull(adapter.getRef());
        assertNull(adapter.getRoot());
        assertNull(adapter.getConnection());
        assertNull(adapter.getInputContexts());
        assertNull(adapter.getOutputContexts());
        assertNull(adapter.getInputs());
        assertNull(adapter.getOutputs());
        assertNull(adapter.getInnerNodes());
        assertNull(adapter.getTaskRerunTime());
        assertNull(adapter.getTaskRerunInterval());
        assertNull(adapter.getDependentType());
        assertNull(adapter.getCycleType());
        assertNull(adapter.getLastModifyTime());
        assertNull(adapter.getLastModifyUser());
        assertNull(adapter.getMultiInstCheckType());
        assertNull(adapter.getPriority());
        assertNull(adapter.getDependentDataNode());
        assertNull(adapter.getOwnerName());
        assertNull(adapter.getExtraConfig());
        assertNull(adapter.getExtraContent());
        assertNull(adapter.getTtContent());
        assertNull(adapter.getAdvanceSettings());
        assertNull(adapter.getExtend());
        assertNull(adapter.getStreamLaunchMode());
        assertNull(adapter.getIgnoreBranchConditionSkip());
        assertNull(adapter.getAlisaTaskKillTimeout());
        assertNull(adapter.getParentId());
        assertNull(adapter.getCu());
        assertNull(adapter.getImageId());
    }
}