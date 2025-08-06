<div align="center">
	<p align="center">
		<a href="https://cn.vuejs.org/" target="_blank">
			<img src="https://img.shields.io/badge/vue.js-vue2.x-green" alt="vue">
		</a>
		<a href="https://element.eleme.cn/#/zh-CN/component/changelog" target="_blank">
			<img src="https://img.shields.io/badge/element--ui-%3E2.15.6-blue" alt="element ui">
		</a>
		<a href="https://v4.webpack.docschina.org/concepts/" target="_blank">
		  <img src="https://img.shields.io/badge/webpack-%3E1.0.0-success" alt="webpack">
		</a>
		<a href="https://gitee.com/lyt-top/vue-next-admin/blob/vue-prev-admin/LICENSE" target="_blank">
		  <img src="https://img.shields.io/badge/license-MIT-yellow" alt="license">
		</a>
	</p>
</div>

#### 🌈 介绍
基于Springboot3.4.6 + vue2.x + element-ui + mybatis-plus + redis + Redisson + WebSocket + Maven 开发的接口自动化测试平台

---
#### ⚡ 使用说明
推荐使用nvm管理node.js版本

npm配置淘宝镜像
```bash
npm config set registry=https://registry.npm.taobao.org
```
> 注意：`node` 需大于 `12.xxx` 小于等于 `v16.14.0`，否则安装依赖将报错，项目中使用的node版本为`v16.14.0`

npm如果出现依赖报错问题的话，使用强制安装依赖即可
```bash
npm i --legacy-peer-deps
```
---

---
#### 🌈 git 规范
#### type: commit 的类型

- 🎯 feat: 新功能、新特性
- 🎯 fix: 修改 bug
- 🎯 perf: 更改代码，以提高性能
- 🎯 refactor: 代码重构（重构，在不影响代码内部行为、功能下的代码修改）
- 🎯 docs: 文档修改
- 🎯 style: 代码格式修改, 注意不是 css 修改（例如分号修改）
- 🎯 ci: 持续集成相关文件修改
- 🎯 chore: 其他修改（不在上述类型中的修改）
---

#### 🎉 目前已实现功能

- 💯  支持批量添加测试接口和测试用例
- 💯 后端通过http客户端发送http请求，避免跨域问题
- 💯  测试用例支持前置脚本、后置脚本、脚本断言，前置脚本支持修改请求体、请求头、query参数，后置脚本支持获取响应头、响应体、cookies
- 💯  每个用户独立的环境变量
- 💯  能够根据参数类型、自定义限定条件自动生成测试用例
- 💯  前后置脚本支持新增、修改、删除环境变量
- 💯  环境变量可以通过`{{params}}`进行参数引用
- 💯  支持多接口、多测试用例、自定义链路请求
- 💯  接口请求记录
- 💯  请求数据看板
- 💯  链路请求异步执行过程中，通过分布式锁确保执行过程中不会被误删
- 💯  链路接口执行完成WebSocket发送实时消息通知

#### 💕 特别感谢
感谢<a target="_blank" href="https://github.com/lyt-Top/vue-next-admin">vue-next-admin</a>框架，助力我顺利开发！
