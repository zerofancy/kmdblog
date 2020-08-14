# kmdblog项目

> 对于一个新手来说，做一个博客常常是很有吸引力的，因为难度不大（最简单的只要CRUD）而且还能获得一定的成就感。
>
> 但稍微多学点之后就发现之前做的东西处处用起来不方便，于是换个想法再做一个。
>
> 自用。

## 构想与设计

首先最近在学Kotlin，那就用Kotlin做了，最后打成一个fatJar。

不连接数据库，所有配置都放到配置文件里，配置文件用`xml`，保证能比较方便阅读和编辑。根目录下应有一个配置文件，保存全局配置，并指定其他文件的位置。

一个`input`文件夹，里面放置的是所有md文件，用户可以直接用编辑器编辑。每个文件还有一个同名的配置文件，存放该文章的个性设置，比如发布时间、tag等。

一个`templates`文件夹，用于存放thymeleaf模板。

一个`output`文件夹，里面是导出的html文件，就是一个直接的站点结构了。

一个`static`文件夹，里面放网站必要的静态资源。静态资源应该被自动复制到`input`、`templates`和`output`文件夹中（需要跑脚本）。这样直接写相对路径，无论编辑md文件还是模板，抑或预览结果都不会找不到资源。

## 使用

下载打包好的程序，（要求有java环境）启动即可。

```shell script
java -jar kmdblog-fat.jar
```

然后当前目录生成了如上四个文件夹和一个`config.xml`文件，参照这些生成的文件自己创建和修改其他文章、模板或静态资源。

修改完成后，重新执行上述指令，在`output`文件夹下就是输出的静态站点了。

## FAQ

### 我能否指定output到根目录

`output`文件夹每次构建是会自动删除的，但考虑到用户会有类似使用`Github Pages`的需求，程序允许在`config.xml`中指定不删除的文件（夹），这样你就可以把`md`文件夹等放到子目录了。这时`config.xml`将类似如下表示：

```xml
<?xml version="1.0" encoding="UTF-8" ?>

<root>
    <!--系统重要路径信息-->
    <path input="./input" output="./" static="./static" templates="./templates" />

    <!--这里的变量将传递给thymeleaf-->
    <attributes>
        <attr ID="sitename">归零幻想的博客</attr>
        <varible ID="defaultAuthor">归零幻想</varible>
    </attributes>
    <!--
    配置不会被自动清理的文件（夹）。
    有时你可能想把输出文件改为根目录，把源码文件等放到子目录。
    此时，因为源码文件没有『依赖』会被自动删除，你可以把他们添加到这里。
    -->
    <noclean>
        <file>input</file>
        <file>static</file>
        <file>templates</file>
        <file>config.xml</file>
        <file>kmdblog-fat.jar</file>
    </noclean>
</root>        
```

## LICENSE

本项目遵循[MIT License](LICENSE)，项目中引用第三方组件的情况见[本项目初次启动默认生成的关于文件](src/main/resources/input/about.md)。
