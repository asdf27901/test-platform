import {Message} from "element-ui";

/**
 * 验证并准备测试用例数据，以便保存到数据库。
 * @param {object} testCase - 前端UI的完整 testCase 对象。
 * @returns {object | null} - 如果验证通过，返回一个符合后端API格式的对象；否则返回 null。
 */
export function prepareDataForSave(testCase) {
    // 验证 Path Param 和 Query Params 的互斥性
    // 这里只筛选出启用的参数进行判断
    const hasActiveQueryParams = testCase.queryParams.some(p => p.enabled);
    const hasPathParam = testCase.pathParam && testCase.pathParam.trim() !== '';

    if (hasActiveQueryParams && hasPathParam) {
        Message.error("路径参数 (Path Param) 和请求参数 (Query Params) 不能同时使用，请只保留其一！");
        return null;
    }

    // 如果是JSON请求，验证JSON格式是否正确
    if (testCase.requestBodyType === 'json' && !testCase.hasJsonFlag) {
        Message.error("请求体中的 JSON 格式错误，请检查后再保存！");
        return null;
    }

    // --- 2. 数据清洗与格式化 ---

    // 清洗和过滤 Query Params
    const filteredQueryParams = testCase.queryParams
        .map(p => ({
            key: p.key.trim(),
            value: p.value,
            description: p.description,
            enabled: p.enabled // 保留启用状态
        }));

    // 清洗和过滤 Headers
    const filteredHeaders = testCase.headers
        .map(h => ({
            key: h.key.trim(),
            value: h.value,
            description: h.description,
            enabled: h.enabled
        }));

    // 构造“复合请求体”对象
    const compositeBody = {
        jsonBody: testCase.jsonBody,
        // 清洗 form-data，注意：文件本身不保存，只保存元信息
        formDataParams: testCase.formDataParams
            .map(p => ({
                key: p.key.trim(),
                value: p.type === 'file' ? p.file?.name || '' : p.value, // 对文件类型，只保存文件名
                type: p.type,
                description: p.description,
                enabled: p.enabled
            })),
        // 清洗 x-www-form-urlencoded
        urlEncodedParams: testCase.urlEncodedParams
            .map(p => ({
                key: p.key.trim(),
                value: p.value,
                description: p.description,
                enabled: p.enabled
            }))
    };


    // --- 3. 组装最终的API负载 (Payload) ---

    return {
        id: testCase.id,
        name: testCase.name.trim(),
        priority: testCase.priority, // 如果未选择，给个默认值，例如 '1' 代表中
        host: testCase.host.trim().startsWith('http://') ?
            testCase.host.trim() : testCase.host.trim().startsWith('https://') ?
                testCase.host.trim() : 'https://' + testCase.host.trim(),

        // 请求相关
        method: testCase.method,
        // 注意：这里的 pathParam 是为了适配数据库的 request_path_params 字段
        // 这里假设 pathParam 是一个简单的字符串，如果需要结构化，需要调整
        pathParam: testCase.pathParam.trim(),
        queryParams: filteredQueryParams,
        headers: filteredHeaders,

        // 请求体核心部分
        requestBodyType: testCase.requestBodyType,
        requestBody: compositeBody, // 直接存储复合对象

        // 断言和其他高级功能（如果前端有相应输入框，则在这里添加）
        // expected_status_code: testCase.expectedStatusCode || null,
        // expected_response_body_assertions: testCase.assertions || [],
    };
}
