# User Dashboard

Generated: 2026-05-10

## Current Progress

| Item | Value |
| --- | --- |
| Phase | delivery_done |
| Status | done |
| Current focus | 新论文已生成（基于源码证据） |
| Can continue? | Yes |

## Completed Work

| Work item | Status | Notes |
| --- | --- | --- |
| 素材收集 | done | 格式规范、开题报告、中期报告、项目源码全部就位 |
| 工作区初始化 | done | thesis-ai-standard + paper-context 已创建 |
| 标准解析 | done | 西安工业大学格式规范已填写 |
| 证据提取 | done | 全部28个Java源文件 + 19个前端源文件已提取 |
| 论文规格配置 | done | thesis-ai-spec.yaml 已填写 |
| 论文大纲确认 | done | 7章结构已确认 |
| 项目构建验证 | done | 前端1.12s构建(213KB/44KB)，后端62MB JAR |
| 接口测试验证 | done | 注册/登录/预约/冲突检测/取消全部通过 |
| 论文正文新稿 | done | 完全基于源码证据重新撰写（7章+摘要+参考文献+致谢+附录） |
| DOCX生成 | done | 主论文 + 附录已生成 |

## Generated Outputs

| 文件 | 路径 | 大小 |
| --- | --- | --- |
| 主论文DOCX | paper-output/基于Vue的图书馆座位预约系统的研究与实现.docx | 57KB |
| 附录DOCX | paper-output/基于Vue的图书馆座位预约系统的研究与实现-附件.docx | 37KB |
| 论文正文Markdown | paper-output/基于Vue的图书馆座位预约系统的研究与实现.md | 约58KB |
| 图片映射 | paper-output/基于Vue的图书馆座位预约系统的研究与实现-image-map.json | 已生成 |

## Pending User Actions

1. **审查DOCX内容** — 确认各章节内容是否满足要求，特别是字数是否达标
2. **补充截图素材** — 运行项目后截取座位预约界面、后台管理界面、构建结果等截图
3. **添加图表** — 可添加系统架构图、E-R图、业务流程图等（使用 draw.io / PlantUML 等工具）
4. **完善参考文献** — 当前12篇参考文献已在正文中有引用，可进一步补充

## Key Facts (基于真实源码)

- 座位总数：225个（3栋建筑 × 3层 × 5排 × 5列），编码格式 B1-F1-A-1
- 座位属性：靠窗(列5)、有插座(奇数列)、安静区(行A/B)
- 冲突检测：时间区间重叠判定（startTime < endTime 且 endTime > startTime）
- 诚信阈值：0.6（可配置）
- 令牌格式：user-{id}-{UUID}
- WebSocket端点：/ws/seat-status
