这份文档将作为您的开发蓝图。
🚀 dwcli 命令行工具最终设计规范（V1.0）
1. 核心目标与技术选型
| 目标 | 描述 |
|---|---|
| 最终目标 | 成为一个高效的、基于本地文件目录结构和 JSON Path 的 配置即代码 (CaC) 管理工具。 |
| 操作范围 | 纯本地文件系统操作。所有远程 API 交互（create, delete, list, get）均已移除。 |
| 核心约束 | 跨平台兼容性、安装便捷性、AI 友好度、严格的规范校验。 |
| 推荐技术栈 | Go (Golang) + Cobra + SJSON/GJSON + JSON Schema 校验库。 |
1.1 目录结构约定
CLI 的操作单位是 对象目录 (<dirpath>)。所有命令都将接收此目录作为目标。
 * Spec JSON: 目录下必须且仅包含一个 *.schedule.json 文件。
 * Code File: 目录下必须且仅包含一个非 Spec 的代码文件 (*.<ext>)。
2. 功能模块 A：对象生命周期管理 (Scaffolding)
该模块负责初始化新的领域对象目录，并确保新 Spec 的完整性。
A.1 dwcli <entity> create <dirpath>
| 规范点 | 描述 |
|---|---|
| 功能 | 创建一个符合规范的领域对象目录骨架（Scaffolding）。 |
| 输入 | <dirpath> (新目录路径)，--name (必需，对象ID/名称)。 |
| Flags | --template <name> (可选，指定内置模板，如 odps-sql-daily)。 |
| 生成逻辑 | 1. 读取内部模板库（Go text/template）。 2. 将 --name 替换到 Spec 中的 id, name 和代码文件的路径 (script.path)。 3. 创建 Spec JSON (*.schedule.json) 和 Code File (*.<ext>)。 4. 强制校验： 确保生成的 Spec 必须通过其对应的 JSON Schema 校验。 |
3. 功能模块 B：Spec JSON 配置管理 (JSON Path)
该模块是工具的核心，用于对 Spec 文件进行精确的结构化修改和查询。
B.1 dwcli <entity> set <dirpath> <path>=<value>...
| 规范点 | 描述 |
|---|---|
| 功能 | 通过 JSON Path 批量修改 Spec JSON 中的字段值。 |
| 输入 | <dirpath>，<path>=<value> 表达式。 |
| 核心逻辑 | 1. 原子性： 在修改前备份文件，失败时回滚。 2. JSON Path： 支持复杂路径和数组追加 ([-1])。 3. 类型安全： 支持 --type 强制转换，并自动推断类型。 4. 强制校验 (集成 C.1)： 修改成功后，必须执行 Schema 校验。如果校验失败，必须回滚文件并报告错误。 |
| Flags | --type (string, int, bool, float), --json, -i, --in-place (默认 true)，--dry-run。 |
B.2 dwcli <entity> unset <dirpath> <json-path>...
| 规范点 | 描述 |
|---|---|
| 功能 | 通过 JSON Path 批量删除 Spec JSON 中的字段或数组元素。 |
| 核心逻辑 | 1. 遵循原子性原则。 2. 强制校验 (集成 C.1)： 删除操作后必须执行 Schema 校验，失败则回滚。 |
| Flags | -i, --in-place，--dry-run。 |
B.3 dwcli <entity> inspect <dirpath> <json-path>...
| 规范点 | 描述 |
|---|---|
| 功能 | 通过 JSON Path 查询并打印 Spec JSON 中的特定值或子结构。 |
| 核心逻辑 | 使用 GJSON 等库进行高性能查询，支持通配符和批量路径。 |
| Flags | --raw-output，-o, --output (json, yaml, tsv)。 |
4. 功能模块 C：规范校验与错误报告 (Critical)
该模块是工具的质量保障核心，所有校验均需严格执行。
C.1 校验执行与集成
| 规范点 | 描述 |
|---|---|
| Schema 管理 | 约定路径： 规范文件存储在 ~/.config/dwcli/schemas/ 目录。 加载： CLI 启动时预编译所有 Schema (*.schema.json) 到内存映射中，键为 kind。 |
| 校验触发 | 默认开启： set 和 unset 默认触发校验。 |
| 显式校验 | 新增命令：dwcli <entity> validate <dirpath>，用于手动检查文件合规性。 |
C.2 校验失败错误报告（严格如编译程序）
当校验失败时，dwcli 必须提供以下三个关键信息，并以高亮、结构化的格式输出。
| 报告要素 | 描述 | 查找实现 |
|---|---|---|
| 错误定位 (Path) | 发生错误的精确 JSON Path（如 spec.timeout）。 | Schema 校验器直接返回。 |
| 错误行号 (Line) | 错误字段在原始 JSON 文件中的行号。 | 增强实现： 必须使用特殊的 JSON 解析器或预处理步骤来构建 Path ↔ LineNumber Map。 |
| 修正方案 (Guidance) | 基于 Schema 定义和错误类型，给出包含 dwcli set 语法的可执行修正建议。 | 动态逻辑：根据错误类型 (type, required, enum) 构造修正字符串。 |
错误报告格式示例:
❌ VALIDATION FAILED: Changes Reverted!
--------------------------------------------------------------------------
#1. 错误类型: Type Mismatch (类型不匹配)
   - 错误路径: spec.timeout
   - 错误原因: Expected an integer (number), got string.
   - 错误行号: 6 
   - 解决方案: Use 'dwcli node set ./daily_agg/ spec.timeout=3600 --type int'
--------------------------------------------------------------------------

5. 功能模块 D：代码文件内容管理
该模块专注于操作目录下的代码文件 (*.<ext>)。
D.1 dwcli <entity> code edit <dirpath>
| 规范点 | 描述 |
|---|---|
| 功能 | 使用外部编辑器打开代码文件。 |
| 核心逻辑 | 优先使用 $EDITOR 环境变量，然后是系统默认编辑器。CLI 进程必须阻塞，等待编辑器关闭。 |
D.2 dwcli <entity> code set <dirpath>
| 规范点 | 描述 |
|---|---|
| 功能 | 替换代码文件的内容。 |
| 输入支持 | 必须支持 -f, --file、--content 字符串和 标准输入。 |
| 核心逻辑 | 遵循原子写入原则（写入前备份）。 |
D.3 dwcli <entity> code get <dirpath>
| 规范点 | 描述 |
|---|---|
| 功能 | 将代码文件的完整内容输出到 Stdout。 |
6. 用户体验与部署规范
| 规范点 | 描述 |
|---|---|
| 跨平台分发 | Go 语言静态编译：提供单一二进制文件，支持 Windows, macOS, Linux，无需任何运行时依赖。 |
| 易上手性 | 必须使用 Cobra 框架，自动生成 -h, --help 帮助文档和 Shell 自动补全脚本。 |
| 错误报告 | 所有输出均应清晰、高亮（使用颜色），确保用户在控制台中能够快速定位信息。 |
| 全局 Flags | -h, --help, --version, --verbose。 |
这份设计文档是完整的，已涵盖您所有的功能、技术和用户体验要求，可以直接作为 AI 编码的基础蓝图。


