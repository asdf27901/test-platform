<template>
    <div class="script-editor-wrapper">
        <!-- 使用 vue-codemirror 组件 -->
        <codemirror
                ref="cmEditor"
                :value="value"
                :options="finalOptions"
                @input="onInput"
        />

        <!-- 语法错误提示面板 -->
        <div v-if="scriptError" class="script-error-panel">
            <i class="el-icon-error"></i>
            <span>{{ scriptError }}</span>
        </div>
    </div>
</template>

<script>
import { codemirror } from 'vue-codemirror'
import CodeMirror from 'codemirror';
import 'codemirror/lib/codemirror.css'
//引入代码补全的基础CSS和JS
import 'codemirror/addon/hint/show-hint.css'
import 'codemirror/addon/hint/show-hint.js'
// 引入针对特定语言的提示逻辑，这里是JavaScript
import 'codemirror/addon/hint/javascript-hint.js'
// 引入JS语言模式
import 'codemirror/mode/javascript/javascript.js'
// 引入代码折叠功能
import 'codemirror/addon/fold/foldgutter.css'
import 'codemirror/addon/fold/foldcode.js'
import 'codemirror/addon/fold/brace-fold.js'
import 'codemirror/addon/display/placeholder.js'
import 'codemirror/addon/edit/closebrackets.js'
import 'codemirror/addon/edit/matchbrackets.js'
import 'codemirror/addon/hint/anyword-hint.js';
import * as acorn from 'acorn';
export default {
    name: "scriptEditor",
    components: {
        codemirror
    },
    props: {
        // 支持 v-model
        value: {
            type: String,
            default: ''
        },
        scriptType: {
            type: String,
            required: true
        }
    },
    data() {
        return {
            scriptError: '',
            // 定义默认的CodeMirror配置
            cmOptions: {
                tabSize: 2,
                mode: 'application/javascript',
                theme: 'default',
                lineNumbers: true,
                line: true,
                foldGutter: true,
                gutters: ['CodeMirror-linenumbers', 'CodeMirror-foldgutter'],
                styleActiveLine: true,
                matchBrackets: true,
                placeholder: "在此输入JavaScript脚本...\n",
                autoCloseBrackets: true
            }
        };
    },
    computed: {
        // 将 data 中的 options 暴露出去，方便模板使用
        finalOptions() {
            return this.cmOptions;
        },
        customHintsTree() {
            if (this.scriptType === 'preRequestScript') {
                return {
                    // 键是提示词，值是显示在提示框右侧的描述或类型
                    "lt": {
                        _type: "object",
                        _doc: "L-Testing对象", // 文档说明
                        // 它的下一级属性或方法
                        request: {
                            _type: "object",
                            _doc: "当前请求对象",
                            headers: {
                                _type: "object",
                                _doc: "请求头",
                                remove: {
                                    _type: "method",
                                    _doc: "删除请求头"
                                },
                                clear: {
                                    _type: "method",
                                    _doc: "清空请求头"
                                },
                                upsert: {
                                    _type: "method",
                                    _doc: "添加/更新请求头"
                                }
                            },
                            queryParams: {
                                _type: "object",
                                _doc: "查询参数",
                                remove: {
                                    _type: "method",
                                    _doc: "删除查询参数"
                                },
                                clear: {
                                    _type: "method",
                                    _doc: "清空查询参数"
                                },
                                upsert: {
                                    _type: "method",
                                    _doc: "添加/更新查询参数"
                                }
                            },
                            body: {
                                _type: "object",
                                _doc: "请求体对象",
                                getJsonStr: {
                                    _type: "method",
                                    _doc: "获取请求体json(字符串)"
                                },
                                setJson: {
                                    _type: "method",
                                    _doc: "设置请求体json"
                                },
                                getForm: {
                                    _type: "method",
                                    _doc: "获取请求表单数据"
                                },
                                upsertForm: {
                                    _type: "method",
                                    _doc: "添加/更新请求表单数据"
                                },
                                removeForm: {
                                    _type: "method",
                                    _doc: "删除请求表单数据"
                                },
                                clearForm: {
                                    _type: "method",
                                    _doc: "清空请求表单数据"
                                }
                            }
                        },
                        expect: {
                            _type: "method",
                            _doc: "期望函数",
                            to: {
                                _type: "field",
                                _doc: "链式属性",
                                be: {
                                    _type: "field",
                                    _doc: "链式属性",
                                    string: {_type: "method", _doc: "验证是不是字符串类型"},
                                    number: {_type: "method", _doc: "验证是不是数字类型"},
                                    bool: {_type: "method", _doc: "验证是不是布尔类型"},
                                    truthy: {_type: "method", _doc: "验证是不是true"},
                                    falsy: {_type: "method", _doc: "验证是不是false"},
                                    nullObj: {_type: "method", _doc: "验证是不是null"},
                                    equal: {_type: "method", _doc: "断言数值相等于 =="},
                                    above: { _type: "method", _doc: "断言数值大于 >" },
                                    below: { _type: "method", _doc: "断言数值小于 <" },
                                    gte: { _type: "method", _doc: "断言数值大于等于 >=" },
                                    lte: { _type: "method", _doc: "断言数值小于等于 <=" }
                                },
                                have: {
                                    _type: "field",
                                    _doc: "链式属性",
                                    property: {
                                        _type: "method",
                                        _doc: "验证对象是否包含key",
                                    },
                                    include: {
                                        _type: "method",
                                        _doc: "验证数组、字符串是否包含",
                                    }
                                },
                                deepEqual: {
                                    _type: "method",
                                    _doc: "深度校验对象",
                                },
                                equal: {
                                    _type: "method",
                                    _doc: "校验对象",
                                },
                                lengthOf: {
                                    _type: "method",
                                    _doc: "验证长度"
                                }
                            }
                        },
                        test: {
                            _type: "method",
                            _doc: "测试函数"
                        },
                        environment: {
                            _type: "object",
                            _doc: "当前所选环境对象",
                            set: {
                                _type: "method",
                                _doc: "设置环境变量"
                            },
                            get: {
                                _type: "method",
                                _doc: "获取环境变量"
                            },
                            clear: {
                                _type: "method",
                                _doc: "清空环境变量"
                            },
                            del: {
                                _type: "method",
                                _doc: "删除环境变量"
                            }
                        }
                    }
                }
            } else {
                return {
                    // 键是提示词，值是显示在提示框右侧的描述或类型
                    "lt": {
                        _type: "object",
                        _doc: "L-Testing对象", // 文档说明
                        // 它的下一级属性或方法
                        response: {
                            _type: "object",
                            _doc: "当前请求响应对象",
                            statusCode: { _type: "number", _doc: "响应状态码" },
                            body: { _type: "string", _doc: "响应体" },
                            headers: { _type: "object", _doc: "响应头" },
                            cookies: { _type: "object", _doc: "cookies"}
                        },
                        expect: {
                            _type: "method",
                            _doc: "期望函数",
                            to: {
                                _type: "field",
                                _doc: "链式属性",
                                be: {
                                    _type: "field",
                                    _doc: "链式属性",
                                    string: {_type: "method", _doc: "验证是不是字符串类型"},
                                    number: {_type: "method", _doc: "验证是不是数字类型"},
                                    bool: {_type: "method", _doc: "验证是不是布尔类型"},
                                    truthy: {_type: "method", _doc: "验证是不是true"},
                                    falsy: {_type: "method", _doc: "验证是不是false"},
                                    nullObj: {_type: "method", _doc: "验证是不是null"},
                                    equal: {_type: "method", _doc: "断言数值相等于 =="},
                                    above: { _type: "method", _doc: "断言数值大于 >" },
                                    below: { _type: "method", _doc: "断言数值小于 <" },
                                    gte: { _type: "method", _doc: "断言数值大于等于 >=" },
                                    lte: { _type: "method", _doc: "断言数值小于等于 <=" }
                                },
                                have: {
                                    _type: "field",
                                    _doc: "链式属性",
                                    property: {
                                        _type: "method",
                                        _doc: "验证对象是否包含key",
                                    },
                                    include: {
                                        _type: "method",
                                        _doc: "验证数组、字符串是否包含",
                                    }
                                },
                                deepEqual: {
                                    _type: "method",
                                    _doc: "深度校验对象",
                                },
                                equal: {
                                    _type: "method",
                                    _doc: "校验对象",
                                },
                                lengthOf: {
                                    _type: "method",
                                    _doc: "验证长度"
                                }
                            }
                        },
                        test: {
                            _type: "method",
                            _doc: "测试函数"
                        },
                        environment: {
                            _type: "object",
                            _doc: "当前所选环境对象",
                            set: {
                                _type: "method",
                                _doc: "设置环境变量"
                            },
                            get: {
                                _type: "method",
                                _doc: "获取环境变量"
                            },
                            clear: {
                                _type: "method",
                                _doc: "清空环境变量"
                            },
                            del: {
                                _type: "method",
                                _doc: "删除环境变量"
                            }
                        }
                    }
                }
            }
        }
    },
    mounted() {
        this.$nextTick(() => {
            const editorInstance = this.$refs.cmEditor ? this.$refs.cmEditor.codemirror : null;

            if (editorInstance) {

                // 移除所有旧的hintOptions设置
                editorInstance.setOption('hintOptions', null);

                editorInstance.on('inputRead', (cm) => {

                    // 尝试调用showHint
                    cm.showHint({
                        hint: this.mergedHint, // 我们将在这里诊断mergedHint
                        completeSingle: false
                    });
                });
            }
        });
    },
    methods: {
        onInput(newValue) {
            // 触发 input 事件，以支持 v-model
            this.$emit('input', newValue);
            // 同时进行语法校验
            this.validateScript(newValue);
        },
        validateScript(script) {
            if (!script || script.trim() === '') {
                this.scriptError = '';
                return;
            }
            try {
                acorn.parse(script, { ecmaVersion: 'latest' });
                this.scriptError = '';
            } catch (e) {
                this.scriptError = `语法错误: ${e.message}`;
            }
        },
        // 公共方法，用于父组件在需要时刷新编辑器布局
        refresh() {
            if (this.codemirrorInstance) {
                this.codemirrorInstance.refresh();
            }
        },
        mergedHint(cm, options) {
            // 1. **获取内置JS提示器的结果，这是我们分析的基础**
            const jsHinter = CodeMirror.hint.javascript;
            const jsHints = jsHinter ? jsHinter(cm, options) : null;
            let jsHintList = (jsHints && jsHints.list) ? [...jsHints.list] : []; // 使用扩展运算符创建副本

            // 2. **计算我们自己的自定义提示**
            const cursor = cm.getCursor();
            const line = cm.getLine(cursor.line);
            const textBefore = line.slice(0, cursor.ch);

            // a. 解析路径
            // eslint-disable-next-line no-useless-escape
            const match = textBefore.match(/[\w.()\[\]'"]*$/);
            const expression = match ? match[0] : '';
            const parts = expression.split('.');
            let wordToComplete = expression.endsWith('.') ? '' : parts.pop() || '';
            const path = parts.filter(p => p);

            // b. 查找上下文并生成自定义提示
            let contextNode = this.customHintsTree;
            for (const part of path) {
                const cleanPart = part.replace(/\(.*\)|\[.*\]/g, '').trim();
                if (contextNode && contextNode[cleanPart]) {
                    contextNode = contextNode[cleanPart];
                } else {
                    // 如果路径在我们的树中找不到，就重置上下文，这样就不会提供嵌套提示了
                    contextNode = null;
                    break;
                }
            }

            let customHintList = [];
            if (contextNode) {
                // 如果是链式调用，就用找到的上下文
                if(path.length > 0) {
                    customHintList = Object.keys(contextNode)
                        .filter(key => !key.startsWith('_') && key.startsWith(wordToComplete))
                        .map(key => {
                            const node = contextNode[key];
                            const displayText = `${key}  (${node._type || 'any'}) - ${node._doc || ''}`;
                            return { text: key, displayText, className: 'custom-hint-item' };
                        });
                }
                // 如果是顶层输入，就从最顶层的customHintsTree开始提示
                else {
                    customHintList = Object.keys(this.customHintsTree)
                        .filter(key => key.startsWith(wordToComplete))
                        .map(key => {
                            const node = this.customHintsTree[key];
                            const displayText = `${key}  (${node._type || 'any'}) - ${node._doc || ''}`;
                            return { text: key, displayText, className: 'custom-hint-item' };
                        });
                }
            }

            // --- 3. 最终合并 ---

            const existingHints = new Set(jsHintList.map(h => typeof h === 'string' ? h : h.text));

            // 从后向前遍历我们的自定义提示，把不存在的项加到最终列表的开头
            for (let i = customHintList.length - 1; i >= 0; i--) {
                const hint = customHintList[i];
                if (!existingHints.has(hint.text)) {
                    jsHintList.unshift(hint);
                }
            }

            if (jsHintList.length === 0) return null;

            // 4. 返回结果
            // 使用jsHinter返回的范围，如果它存在且有效的话
            const from = jsHints ? jsHints.from : CodeMirror.Pos(cursor.line, cursor.ch - wordToComplete.length);
            const to = jsHints ? jsHints.to : cursor;

            return { list: jsHintList, from, to };
        }
    },
}
</script>

<style scoped>
/* 样式和之前的原生集成版本完全一样 */
.script-editor-wrapper {
    position: relative;
    border: 1px solid #dcdfe6;
    border-radius: 4px;
}

.script-editor-wrapper >>> .CodeMirror {
    height: 400px;
    font-size: 14px;
    border-radius: 4px;
}
.script-editor-wrapper >>> .CodeMirror-placeholder {
    color: #888 !important;
}

.script-error-panel {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    background-color: #fef0f0;
    color: #f56c6c;
    padding: 8px 15px;
    font-size: 13px;
    display: flex;
    align-items: center;
    border-top: 1px solid #fde2e2;
    z-index: 10;
}
.script-error-panel i {
    margin-right: 8px;
    font-size: 16px;
}
</style>

<style>
/*
  由于是全局样式，最好给它一个唯一的父级选择器来限制范围，
  但对于动态添加到 body 的元素，这比较困难，所以直接这样写。
  这可能会有潜在的全局冲突风险，但通常能解决问题。
*/
.CodeMirror-hints .custom-hint-item {
    color: #c76e25;
    font-weight: bold;
}
.CodeMirror-hints .custom-hint-item.CodeMirror-hint-active {
    background-color: #e3e1e2;
}
</style>
