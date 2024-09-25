package com.magicvector.common.rest.swagger;

import com.magicvector.common.rest.annotation.SwaggerModule;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.springframework.stereotype.Component;
import springfox.documentation.service.ApiDescription;
import springfox.documentation.service.ApiListing;
import springfox.documentation.service.ResourceGroup;
import springfox.documentation.spring.web.plugins.DocumentationPluginsManager;
import springfox.documentation.spring.web.scanners.ApiListingScanner;
import springfox.documentation.spring.web.scanners.ApiListingScanningContext;
import springfox.documentation.spring.web.scanners.ApiModelReader;
import springfox.documentation.spring.web.scanners.ApiDescriptionReader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ApiListingScannerExt extends ApiListingScanner {

    public ApiListingScannerExt(ApiDescriptionReader apiDescriptionReader,
                                ApiModelReader apiModelReader,
                                DocumentationPluginsManager pluginsManager) {
        super(apiDescriptionReader, apiModelReader, pluginsManager);
    }

    @Override
    public Multimap<String, ApiListing> scan(ApiListingScanningContext context) {

        Map<String, ResourceGroup> groupMap = context.getRequestMappingsByResourceGroup()
                .entrySet().stream().collect(Collectors.toMap(
                        entry -> entry.getKey().getGroupName(),
                        Map.Entry::getKey));

        Multimap<String, ApiListing> superResult = super.scan(context);
        Multimap<String, ApiListing> result = ArrayListMultimap.create();

        superResult.asMap().forEach((groupName, apiListings) -> {
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
            result.putAll(moduleName, apiListings);
        });

        return result;
    }

    private String getModuleName(ResourceGroup resourceGroup) {
        // 使用 Guava 的 Optional 处理
        SwaggerModule swaggerModule = com.google.common.base.Optional
                .fromNullable(resourceGroup.getControllerClass().orNull())
                .transform(controllerClass -> controllerClass.getAnnotation(SwaggerModule.class))
                .orNull();

        if (swaggerModule != null) {
            return swaggerModule.name();
        }

        return resourceGroup.getGroupName();
    }
}