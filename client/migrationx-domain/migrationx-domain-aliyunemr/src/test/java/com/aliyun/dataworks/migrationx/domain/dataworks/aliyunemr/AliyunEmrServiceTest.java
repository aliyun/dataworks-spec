package com.aliyun.dataworks.migrationx.domain.dataworks.aliyunemr;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.emr.model.v20160408.ListFlowProjectResponse;
import com.aliyuncs.emr.model.v20160408.ListFlowProjectResponse.Project;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author sam.liux
 * @date 2019/06/27
 */
public class AliyunEmrServiceTest {
    private final String accessId = System.getenv("ALIYUN_EMR_ACCESS_ID");
    private final String accessKey = System.getenv("ALIYUN_EMR_ACCESS_KEY");
    private final String endpoint = "emr.aliyuncs.com";
    private final String regionId = "cn-shanghai";

    private DefaultAcsClient getDefaultAcsClient() {
        DefaultProfile.addEndpoint(regionId, "emr", endpoint);
        IClientProfile profile = DefaultProfile.getProfile(regionId, accessId, accessKey);
        return new DefaultAcsClient(profile);
    }

    @Test
    public void testSupplyPath() throws ClientException, IOException {
        AliyunEmrService service = new AliyunEmrService(accessId, accessKey, endpoint, regionId);
        String flowId = "FC-761D677371F646A2";
        String projectId = "FP-C8B0B85B7AB70D51";
        ListFlowProjectResponse.Project project = new Project();
        project.setId(projectId);
        List<String> paths = new ArrayList<>();
        service.supplyFlowCategoryPath(paths, project, flowId);
        System.out.println(StringUtils.join(paths, File.separator));

        AliyunEmrExportRequest request = new AliyunEmrExportRequest();
        File dir = new File(Objects.requireNonNull(AliyunEmrServiceTest.class.getClassLoader().getResource(".")).getFile());
        request.setFolder(new File(Objects.requireNonNull(AliyunEmrServiceTest.class.getClassLoader().getResource(".")).getFile()));
        request.setProjects(Collections.singletonList("Default"));
        service.dump(request);
        Assert.assertTrue(dir.listFiles() != null && Objects.requireNonNull(dir.listFiles()).length > 0);
    }
}
