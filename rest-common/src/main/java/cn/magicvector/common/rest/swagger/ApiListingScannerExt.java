package cn.magicvector.common.rest.swagger;

import cn.magicvector.common.rest.annotation.SwaggerModule;
import org.springframework.stereotype.Component;
import springfox.documentation.service.ApiDescription;
import springfox.documentation.service.ApiListing;
import springfox.documentation.service.ResourceGroup;
import springfox.documentation.spring.web.plugins.DocumentationPluginsManager;
import springfox.documentation.spring.web.scanners.*;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class ApiListingScannerExt extends ApiListingScanner {

    public ApiListingScannerExt(ApiDescriptionReader apiDescriptionReader, ApiModelReader apiModelReader, ApiModelSpecificationReader modelSpecificationReader, DocumentationPluginsManager pluginsManager) {
        super(apiDescriptionReader, apiModelReader, modelSpecificationReader, pluginsManager);
    }

    @Override
    public Map<String, List<ApiListing>> scan(ApiListingScanningContext context) {

        Map<String, ResourceGroup> groupMap = context.getRequestMappingsByResourceGroup()
                .entrySet().stream().collect(Collectors.toMap(
                        entry -> entry.getKey().getGroupName(),
                        Map.Entry::getKey));

        Map<String, List<ApiListing>> superResult = super.scan(context);
        Map<String,  List<ApiListing>> result = new HashMap<>();

        superResult.forEach((groupName, apiListings) -> {
            String moduleName = groupName;
            if (groupMap.containsKey(groupName)) {
                moduleName = getModuleName(groupMap.get(groupName));
            }
            final String finalModuleName = moduleName;

            apiListings.forEach(apiListing -> {
                apiListing.getTags().clear();
                List<ApiDescription> newList = new ArrayList<>();
                apiListing.getApis().forEach(innerApi -> {
                    // 修改构造函数，移除 summary
                    ApiDescription tempApiDescription = new ApiDescription(
                            finalModuleName,
                            innerApi.getPath(),
                            innerApi.getSummary(),
                            innerApi.getDescription(), // 使用 description 字段
                            innerApi.getOperations(),
                            innerApi.isHidden());
                    tempApiDescription.getOperations().forEach(operation -> {
                        operation.getTags().clear();
                        operation.getTags().add(finalModuleName);
                    });
                    newList.add(tempApiDescription);
                });
                apiListing.getApis().clear();
                apiListing.getApis().addAll(newList);
            });
            result.put(moduleName, apiListings);
        });

        return result;
    }

    private String getModuleName(ResourceGroup resourceGroup) {
        // 使用 Guava 的 Optional 处理
        SwaggerModule swaggerModule = resourceGroup.getControllerClass().map(controllerClass -> controllerClass.getAnnotation(SwaggerModule.class)).orElse(null);

        if (swaggerModule != null) {
            return swaggerModule.name();
        }

        return resourceGroup.getGroupName();
    }
}