---
title: 关于
author: 归零幻想
publishDate: 2020-10-22
editDate: 2020-10-22
tags: [关于, kmdblog]
---
-

# 关于

[TOC]

## 关于kmdblog

### kmdblog是什么

本项目是一个自用的`Kotlin`练手项目，期望能根据`markdown`源文件自动生成一个可以直接部署的静态网站。

<!--more-->

### kmdblog名字的含义

`k`指项目用`Kotlin`开发，`md`指通过`markdown`文件生成文章。

### 问题和求助

本项目没有任何担保，遇到问题请用意志力扛过去。

### 开源项目

本项目的诞生离不开以下开源项目的支持。

- [commons-io](https://commons.apache.org/proper/commons-io/)
- [dom4j](https://dom4j.github.io/)
- [flexmark-java](https://github.com/vsch/flexmark-java)
- [kotlin-argparser](https://github.com/xenomachina/kotlin-argparser)
- [kotlinpoet](https://square.github.io/kotlinpoet/)
- [Slf4j](http://www.slf4j.org/)
- [Thymeleaf](https://www.thymeleaf.org/)

> flexmark-java对于我这个小项目实在太庞大了，因此只引用了其中的一部分组件。具体引入的插件有：
>
> - 表格插件`TablesExtension`
> - 目录插件`TocExtension`
> - 删除线插件`StrikethroughExtension`
> - 脚注插件`FootnoteExtension`
> - 任务列表插件`TaskListExtension`

文章参数的保存参考了 [Hexo](https://hexo.io/zh-cn/) 的设计。
