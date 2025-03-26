1. 我需要编写一个java项目，该项目以jar包的形式使用，执行时填写必要的数据库信息，就可以生成对应数据库的文档
2. 可以参考项目：https://github.com/pingfangushi/screw 使用相应的工具
3. 使用时封装为jar包
4. 对应项目存放到/Users/code2rich/home/project_repo/kyp-allinone/kyp-others/mykyp/事项/KnowYourDB下面
5. 导出的时候把word、markdown格式的支持上去，另外能否设计一个窗口允许的时候在页面上填写相信信息，并且如果是可以选择就让选择，比如驱动之类的，近可能的易用。
6. 我想增加以下功能：
         1.指定生成逻辑、当存在指定表、指定表前缀、指定表后缀时，将生成指定表，其余表不生成、并跳过忽略表配置	
		     2.根据名称指定表生成
		     3.根据表前缀生成
		     4.根据表后缀生成	
         5.忽略表名
         6.忽略表前缀
         7.忽略表后缀
7. 能否支持Excel版本的导出
8.生成的excel需要增加跳转表详情的链接，另外对于展示样式上进行优化，需要有颜色，实现的区分


# KnowYourDB - Database Documentation Generator

KnowYourDB is a tool for generating comprehensive database documentation based on the [screw](https://github.com/pingfangushi/screw) library. It supports multiple database systems including MySQL, PostgreSQL, Oracle, SQL Server, and more.

## Features

- Support for multiple database systems (MySQL, PostgreSQL, Oracle, SQL Server, etc.)
- Generate documentation in various formats: HTML, Word, Markdown
- Simple command-line interface
- Rich configuration options

## Requirements

- Java 8 or higher
- Maven 3.6 or higher (for building)

## Building

To build the project:

```bash
mvn clean package
