
export function validateTestCase(mode, testCase, interfaceId) {
    const errors = [];

    if (!interfaceId && mode === 'add') {
        errors.push("用例未关联到任何接口。");
    }

    // 1. 验证用例名称
    if (!testCase.name || testCase.name.trim() === '' || testCase.name.trim() === '未命名用例') {
        errors.push("请为测试用例命名！");
    }

    // 2. 验证 Host
    if (!testCase.host || testCase.host.trim() === '') {
        errors.push("用例没有填写域名信息！");
    }

    // 3. 验证 Path
    if (!testCase.path || testCase.path.includes('无法获取')) {
        errors.push("没有获取到请求地址，请刷新页面！");
    }

    // 4. 验证 Path Param 和 Query Params 的互斥性
    const hasActiveQueryParams = testCase.queryParams.some(p => p.enabled && p.key.trim() !== '');
    const hasPathParam = testCase.pathParam && testCase.pathParam.trim() !== '';

    if (hasActiveQueryParams && hasPathParam) {
        errors.push("路径参数和查询参数不能同时使用。");
    }

    // 5. 验证 JSON 格式
    if (testCase.requestBodyType === 'json' && !testCase.hasJsonFlag) {
        errors.push("请求体中的 JSON 格式错误。");
    }

    return errors;
}
