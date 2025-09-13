-- 数据源管理
CREATE TABLE gen_datasource (
    id bigint NOT NULL COMMENT 'id',
    db_type varchar(200) COMMENT '数据库类型 MySQL、Oracle、SQLServer、PostgreSQL',
    conn_name varchar(200) NOT NULL COMMENT '连接名',
    conn_url varchar(500) COMMENT 'URL',
    username varchar(200) COMMENT '用户名',
    password varchar(200) COMMENT '密码',
    status tinyint COMMENT '状态  0：启用   1：禁用',
    create_date datetime COMMENT '创建时间',
    primary key (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据源管理';

-- 字段类型管理
CREATE TABLE gen_field_type (
    id bigint NOT NULL COMMENT 'id',
    column_type varchar(200) COMMENT '字段类型',
    attr_type varchar(200) COMMENT '属性类型',
    package_name varchar(200) COMMENT '属性包名',
    create_date datetime COMMENT '创建时间',
    primary key (id),
    unique key (column_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字段类型管理';

-- 基类管理
CREATE TABLE gen_base_class (
    id bigint NOT NULL COMMENT 'id',
    package_name varchar(200) COMMENT '基类包名',
    code varchar(200) COMMENT '基类编码',
    fields varchar(500) COMMENT '基类字段，多个用英文逗号分隔',
    remark varchar(200) COMMENT '备注',
    create_date datetime COMMENT '创建时间',
    primary key (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='基类管理';

-- 代码生成表
CREATE TABLE gen_table_info (
    id bigint NOT NULL COMMENT 'id',
    table_name varchar(200) COMMENT '表名',
    class_name varchar(200) COMMENT '类名',
    table_comment varchar(200) COMMENT '功能名',
    author varchar(200) COMMENT '作者',
    email varchar(200) COMMENT '邮箱',
    package_name varchar(200) COMMENT '项目包名',
    version varchar(200) COMMENT '项目版本号',
    backend_path varchar(500) COMMENT '后端生成路径',
    frontend_path varchar(500) COMMENT '前端生成路径',
    module_name varchar(200) COMMENT '模块名',
    sub_module_name varchar(200) COMMENT '子模块名',
    datasource_id bigint COMMENT '数据源ID',
    baseclass_id bigint COMMENT '基类ID',
    create_date datetime COMMENT '创建时间',
    primary key (id),
    unique key (table_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='代码生成表';

-- 代码生成表列
CREATE TABLE gen_table_field (
    id bigint NOT NULL COMMENT 'id',
    table_id bigint COMMENT '表ID',
    table_name varchar(200) COMMENT '表名',
    column_name varchar(200) COMMENT '列名',
    column_type varchar(200) COMMENT '类型',
    column_comment varchar(200) COMMENT '列说明',
    attr_name varchar(200) COMMENT '属性名',
    attr_type varchar(200) COMMENT '属性类型',
    package_name varchar(200) COMMENT '属性包名',
    is_pk tinyint COMMENT '是否主键 0：否  1：是',
    is_required tinyint COMMENT '是否必填 0：否  1：是',
    is_form tinyint COMMENT '是否表单字段 0：否  1：是',
    is_list tinyint COMMENT '是否列表字段 0：否  1：是',
    is_query tinyint COMMENT '是否查询字段 0：否  1：是',
    query_type varchar(200) COMMENT '查询方式',
    form_type varchar(200) COMMENT '表单类型',
    dict_name varchar(200) COMMENT '字典名称',
    validator_type varchar(200) COMMENT '效验方式',
    sort int COMMENT '排序',
    primary key (id),
    key (table_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='代码生成表列';

-- 模板管理
CREATE TABLE gen_template (
    id bigint NOT NULL COMMENT 'id',
    name varchar(200) COMMENT '名称',
    file_name varchar(200) COMMENT '文件名',
    content mediumtext NOT NULL COMMENT '内容',
    path varchar(500) COMMENT '生成路径',
    status tinyint COMMENT '状态  0：启用   1：禁用',
    create_date datetime COMMENT '创建时间',
    primary key (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模板管理';


CREATE TABLE gen_test_data (
    id bigint NOT NULL COMMENT 'id',
    username varchar(50)  COMMENT '用户名',
    real_name varchar(50) COMMENT '姓名',
    gender tinyint unsigned COMMENT '性别',
    email varchar(100) COMMENT '邮箱',
    notice_type tinyint COMMENT '类型',
    content text COMMENT '内容',
    creator bigint COMMENT '创建者',
    create_date datetime COMMENT '创建时间',
    updater bigint COMMENT '更新者',
    update_date datetime COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测试功能';


INSERT INTO sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1302850622416084993, 0, '', '', 0, 0, 'icon-rocket', 3, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1302850783288614913, 1305532398162145281, 'devtools/datasource', '', 0, 0, 'icon-sync', 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1302862890696564738, 1305532398162145281, 'devtools/fieldtype', '', 0, 0, 'icon-eye', 1, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1302874751835848705, 1305532398162145281, 'devtools/baseclass', '', 0, 0, 'icon-info-circle', 3, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1304081184014635010, 1305532398162145281, 'devtools/template', '', 0, 0, 'icon-up-circle', 3, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1304802103569809410, 1302850622416084993, 'devtools/generator', '', 0, 0, 'icon-tags', 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1305513187675144193, 1305532398162145281, 'devtools/param', '', 0, 0, 'icon-setting', 5, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1305532398162145281, 1302850622416084993, '', '', 0, 0, 'icon-setting', 3, 1067246875800000001, now(), 1067246875800000001, now());

INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1302850622416084993, 'name', 'DevTools', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1302850622416084993, 'name', '开发者工具', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1302850622416084993, 'name', '開發者工具', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1302850783288614913, 'name', 'DataSource', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1302850783288614913, 'name', '数据源管理', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1302850783288614913, 'name', '數據源管理', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1302862890696564738, 'name', 'Field Management', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1302862890696564738, 'name', '字段管理', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1302862890696564738, 'name', '字段管理', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1302874751835848705, 'name', 'BaseClass', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1302874751835848705, 'name', '基类管理', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1302874751835848705, 'name', '基類管理', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1304081184014635010, 'name', 'Template', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1304081184014635010, 'name', '模板管理', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1304081184014635010, 'name', '模板管理', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1304802103569809410, 'name', 'Code Generation', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1304802103569809410, 'name', '代码生成工具', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1304802103569809410, 'name', '代碼生成工具', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1305513187675144193, 'name', 'Parameter Config', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1305513187675144193, 'name', '参数配置', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1305513187675144193, 'name', '參數配置', 'zh-TW');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1305532398162145281, 'name', 'Config Info', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1305532398162145281, 'name', '配置信息', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1305532398162145281, 'name', '配置信息', 'zh-TW');

INSERT INTO sys_params(id, param_code, param_value, param_type, remark, creator, create_date, updater, update_date) VALUES (1067246875800000072, 'DEV_TOOLS_PARAM_KEY', '{"packageName":"io.renren","version":"3.0","author":"Mark","email":"sunlightcs@gmail.com","backendPath":"D:\\\\renrenio\\\\security-enterprise\\\\renren-admin","frontendPath":"D:\\\\renrenio\\\\security-enterprise-admin"}', 0, '代码生成器配置信息', 1067246875800000001, now(), 1067246875800000001, now());

INSERT INTO gen_base_class(id, package_name, code, fields, remark, create_date) VALUES (1302875019642159105, '${package}.common.entity.BaseEntity', 'BaseEntity', 'id,creator,create_date', '专业版', now());
INSERT INTO gen_datasource(id, db_type, conn_name, conn_url, username, password, status, create_date) VALUES (1302855887882412034, 'MySQL', '本地', 'jdbc:mysql://localhost:3306/security_enterprise?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&nullCatalogMeansCurrent=true', 'renren', '123456', 0, now());
INSERT INTO gen_field_type(id, column_type, attr_type, package_name, create_date) VALUES (1152452777352425473, 'datetime', 'Date', 'java.util.Date', now());
INSERT INTO gen_field_type(id, column_type, attr_type, package_name, create_date) VALUES (1152453412995002369, 'date', 'Date', 'java.util.Date', now());
INSERT INTO gen_field_type(id, column_type, attr_type, package_name, create_date) VALUES (1152453603525455873, 'tinyint', 'Integer', NULL, now());
INSERT INTO gen_field_type(id, column_type, attr_type, package_name, create_date) VALUES (1152453660052090881, 'smallint', 'Integer', NULL, now());
INSERT INTO gen_field_type(id, column_type, attr_type, package_name, create_date) VALUES (1152453722136178689, 'mediumint', 'Integer', NULL, now());
INSERT INTO gen_field_type(id, column_type, attr_type, package_name, create_date) VALUES (1152453808874385409, 'int', 'Integer', NULL, now());
INSERT INTO gen_field_type(id, column_type, attr_type, package_name, create_date) VALUES (1152453849735294977, 'integer', 'Integer', NULL, now());
INSERT INTO gen_field_type(id, column_type, attr_type, package_name, create_date) VALUES (1152453895029583873, 'bigint', 'Long', NULL, now());
INSERT INTO gen_field_type(id, column_type, attr_type, package_name, create_date) VALUES (1152453931373228033, 'float', 'Float', NULL, now());
INSERT INTO gen_field_type(id, column_type, attr_type, package_name, create_date) VALUES (1152453967880450050, 'double', 'Double', NULL, now());
INSERT INTO gen_field_type(id, column_type, attr_type, package_name, create_date) VALUES (1152454047601586177, 'decimal', 'BigDecimal', 'java.math.BigDecimal', now());
INSERT INTO gen_field_type(id, column_type, attr_type, package_name, create_date) VALUES (1152454090760974338, 'bit', 'Boolean', NULL, now());
INSERT INTO gen_field_type(id, column_type, attr_type, package_name, create_date) VALUES (1152454147010785282, 'char', 'String', NULL, now());
INSERT INTO gen_field_type(id, column_type, attr_type, package_name, create_date) VALUES (1152454183136325633, 'varchar', 'String', NULL, now());
INSERT INTO gen_field_type(id, column_type, attr_type, package_name, create_date) VALUES (1152454312664821761, 'tinytext', 'String', NULL, now());
INSERT INTO gen_field_type(id, column_type, attr_type, package_name, create_date) VALUES (1152454343820111874, 'text', 'String', NULL, now());
INSERT INTO gen_field_type(id, column_type, attr_type, package_name, create_date) VALUES (1152454372077137921, 'mediumtext', 'String', NULL, now());
INSERT INTO gen_field_type(id, column_type, attr_type, package_name, create_date) VALUES (1152454401378545665, 'longtext', 'String', NULL, now());
INSERT INTO gen_field_type(id, column_type, attr_type, package_name, create_date) VALUES (1152454486267064322, 'timestamp', 'Date', 'java.util.Date', now());
INSERT INTO gen_field_type(id, column_type, attr_type, package_name, create_date) VALUES (1152454630295269378, 'NUMBER', 'Integer', NULL, now());
INSERT INTO gen_field_type(id, column_type, attr_type, package_name, create_date) VALUES (1152454715645161474, 'BINARY_INTEGER', 'Integer', NULL, now());
INSERT INTO gen_field_type(id, column_type, attr_type, package_name, create_date) VALUES (1152454778828156930, 'BINARY_FLOAT', 'Float', NULL, now());
INSERT INTO gen_field_type(id, column_type, attr_type, package_name, create_date) VALUES (1152454828987838466, 'BINARY_DOUBLE', 'Double', NULL, now());
INSERT INTO gen_field_type(id, column_type, attr_type, package_name, create_date) VALUES (1152454885745160193, 'VARCHAR2', 'String', NULL, now());
INSERT INTO gen_field_type(id, column_type, attr_type, package_name, create_date) VALUES (1152454919756771329, 'NVARCHAR', 'String', NULL, now());
INSERT INTO gen_field_type(id, column_type, attr_type, package_name, create_date) VALUES (1152454952568811521, 'NVARCHAR2', 'String', NULL, now());
INSERT INTO gen_field_type(id, column_type, attr_type, package_name, create_date) VALUES (1152454986349735938, 'CLOB', 'String', NULL, now());
INSERT INTO gen_field_type(id, column_type, attr_type, package_name, create_date) VALUES (1152455109695827970, 'int8', 'Long', NULL, now());
INSERT INTO gen_field_type(id, column_type, attr_type, package_name, create_date) VALUES (1152455153002016770, 'int4', 'Integer', NULL, now());
INSERT INTO gen_field_type(id, column_type, attr_type, package_name, create_date) VALUES (1152455184669011969, 'int2', 'Integer', NULL, now());
INSERT INTO gen_field_type(id, column_type, attr_type, package_name, create_date) VALUES (1152455217359417345, 'numeric', 'BigDecimal', 'java.math.BigDecimal', now());

INSERT INTO gen_table_info(id, table_name, class_name, table_comment, author, email, package_name, version, backend_path, frontend_path, module_name, sub_module_name, datasource_id, baseclass_id, create_date) VALUES (1308327671447859201, 'gen_test_data', 'TestData', '测试功能', 'Mark', 'sunlightcs@gmail.com', 'io.renren', '3.0', 'D:\\renrenio\\security-enterprise\\renren-admin', 'D:\\renrenio\\security-enterprise-admin', 'gen', NULL, 0, 1302875019642159105, now());

INSERT INTO gen_table_field(id, table_id, table_name, column_name, column_type, column_comment, attr_name, attr_type, package_name, is_pk, is_required, is_form, is_list, is_query, query_type, form_type, dict_name, validator_type, sort) VALUES (1308327671502385153, 1308327671447859201, 'gen_test_data', 'id', 'BIGINT', 'id', 'id', 'Long', NULL, 1, 0, 0, 0, 0, '=', 'text', NULL, NULL, 0);
INSERT INTO gen_table_field(id, table_id, table_name, column_name, column_type, column_comment, attr_name, attr_type, package_name, is_pk, is_required, is_form, is_list, is_query, query_type, form_type, dict_name, validator_type, sort) VALUES (1308327671510773761, 1308327671447859201, 'gen_test_data', 'username', 'VARCHAR', '用户名', 'username', 'String', NULL, 0, 1, 1, 1, 1, 'like', 'text', NULL, NULL, 1);
INSERT INTO gen_table_field(id, table_id, table_name, column_name, column_type, column_comment, attr_name, attr_type, package_name, is_pk, is_required, is_form, is_list, is_query, query_type, form_type, dict_name, validator_type, sort) VALUES (1308327671510773762, 1308327671447859201, 'gen_test_data', 'real_name', 'VARCHAR', '姓名', 'realName', 'String', NULL, 0, 1, 1, 1, 0, 'like', 'text', NULL, NULL, 2);
INSERT INTO gen_table_field(id, table_id, table_name, column_name, column_type, column_comment, attr_name, attr_type, package_name, is_pk, is_required, is_form, is_list, is_query, query_type, form_type, dict_name, validator_type, sort) VALUES (1308327671510773763, 1308327671447859201, 'gen_test_data', 'gender', 'TINYINT', '性别', 'gender', 'Integer', NULL, 0, 1, 1, 1, 1, '=', 'select', 'gender', NULL, 3);
INSERT INTO gen_table_field(id, table_id, table_name, column_name, column_type, column_comment, attr_name, attr_type, package_name, is_pk, is_required, is_form, is_list, is_query, query_type, form_type, dict_name, validator_type, sort) VALUES (1308327671510773764, 1308327671447859201, 'gen_test_data', 'email', 'VARCHAR', '邮箱', 'email', 'String', NULL, 0, 1, 1, 1, 0, '=', 'text', NULL, NULL, 4);
INSERT INTO gen_table_field(id, table_id, table_name, column_name, column_type, column_comment, attr_name, attr_type, package_name, is_pk, is_required, is_form, is_list, is_query, query_type, form_type, dict_name, validator_type, sort) VALUES (1308327671514968065, 1308327671447859201, 'gen_test_data', 'notice_type', 'TINYINT', '类型', 'noticeType', 'Integer', NULL, 0, 1, 1, 1, 0, '=', 'radio', 'notice_type', NULL, 5);
INSERT INTO gen_table_field(id, table_id, table_name, column_name, column_type, column_comment, attr_name, attr_type, package_name, is_pk, is_required, is_form, is_list, is_query, query_type, form_type, dict_name, validator_type, sort) VALUES (1308327671514968066, 1308327671447859201, 'gen_test_data', 'content', 'TEXT', '内容', 'content', 'String', NULL, 0, 1, 1, 0, 0, '=', 'editor', NULL, NULL, 6);
INSERT INTO gen_table_field(id, table_id, table_name, column_name, column_type, column_comment, attr_name, attr_type, package_name, is_pk, is_required, is_form, is_list, is_query, query_type, form_type, dict_name, validator_type, sort) VALUES (1308327671514968067, 1308327671447859201, 'gen_test_data', 'creator', 'BIGINT', '创建者', 'creator', 'Long', NULL, 0, 0, 0, 0, 0, '=', 'text', NULL, NULL, 7);
INSERT INTO gen_table_field(id, table_id, table_name, column_name, column_type, column_comment, attr_name, attr_type, package_name, is_pk, is_required, is_form, is_list, is_query, query_type, form_type, dict_name, validator_type, sort) VALUES (1308327671514968068, 1308327671447859201, 'gen_test_data', 'create_date', 'DATETIME', '创建时间', 'createDate', 'Date', 'java.util.Date', 0, 0, 0, 1, 1, '=', 'date', NULL, NULL, 8);
INSERT INTO gen_table_field(id, table_id, table_name, column_name, column_type, column_comment, attr_name, attr_type, package_name, is_pk, is_required, is_form, is_list, is_query, query_type, form_type, dict_name, validator_type, sort) VALUES (1308327671514968069, 1308327671447859201, 'gen_test_data', 'updater', 'BIGINT', '更新者', 'updater', 'Long', NULL, 0, 0, 0, 0, 0, '=', 'text', NULL, NULL, 9);
INSERT INTO gen_table_field(id, table_id, table_name, column_name, column_type, column_comment, attr_name, attr_type, package_name, is_pk, is_required, is_form, is_list, is_query, query_type, form_type, dict_name, validator_type, sort) VALUES (1308327671523356674, 1308327671447859201, 'gen_test_data', 'update_date', 'DATETIME', '更新时间', 'updateDate', 'Date', 'java.util.Date', 0, 0, 0, 0, 0, '=', 'text', NULL, NULL, 10);

INSERT INTO gen_template(id, name, file_name, content, path, status, create_date) VALUES (1144564443498168321, 'Entity.java', '${ClassName}Entity.java', '', '${backendPath}/src/main/java/${packagePath}/modules/${moduleName}/entity/${subModuleName!}', 0, now());
INSERT INTO gen_template(id, name, file_name, content, path, status, create_date) VALUES (1151104918417948674, 'Dao.java', '${ClassName}Dao.java', '', '${backendPath}/src/main/java/${packagePath}/modules/${moduleName}/dao/${subModuleName!}', 0, now());
INSERT INTO gen_template(id, name, file_name, content, path, status, create_date) VALUES (1222102542519422977, 'Dao.xml', '${ClassName}Dao.xml', '', '${backendPath}/src/main/resources/mapper/${moduleName}', 0, now());
INSERT INTO gen_template(id, name, file_name, content, path, status, create_date) VALUES (1222811087900602369, 'Service.java', '${ClassName}Service.java', '', '${backendPath}/src/main/java/${packagePath}/modules/${moduleName}/service/${subModuleName!}', 0, now());
INSERT INTO gen_template(id, name, file_name, content, path, status, create_date) VALUES (1222811738672033793, 'ServiceImpl.java', '${ClassName}ServiceImpl.java', '', '${backendPath}/src/main/java/${packagePath}/modules/${moduleName}/service/${subModuleName!}/impl', 0, now());
INSERT INTO gen_template(id, name, file_name, content, path, status, create_date) VALUES (1222814109661753346, 'Excel.java', '${ClassName}Excel.java', '', '${backendPath}/src/main/java/${packagePath}/modules/${moduleName}/excel/${subModuleName!}', 0, now());
INSERT INTO gen_template(id, name, file_name, content, path, status, create_date) VALUES (1222816187301851138, 'DTO.java', '${ClassName}DTO.java', '', '${backendPath}/src/main/java/${packagePath}/modules/${moduleName}/dto/${subModuleName!}', 0, now());
INSERT INTO gen_template(id, name, file_name, content, path, status, create_date) VALUES (1222817622663983106, 'Controller.java', '${ClassName}Controller.java', '', '${backendPath}/src/main/java/${packagePath}/modules/${moduleName}/controller/${subModuleName!}', 0, now());
INSERT INTO gen_template(id, name, file_name, content, path, status, create_date) VALUES (1304292858898309122, 'list.vue', '${classname}.vue', '', '${frontendPath}/src/views/${moduleName}', 0, now());
INSERT INTO gen_template(id, name, file_name, content, path, status, create_date) VALUES (1304293015698169857, 'add-or-update.vue', '${classname}-add-or-update.vue', '', '${frontendPath}/src/views/${moduleName}', 0, now());

INSERT INTO gen_test_data(id, username, real_name, gender, email, notice_type, content, creator, create_date, updater, update_date) VALUES (1067246875800000001, 'sunlightcs', 'Mark', 0, 'root@renren.io', 0, '<p>人人开源代码生成器！</p>', 1067246875800000001, now(), 1067246875800000001, now());


INSERT INTO sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1340949288542347266, 1302850622416084993, 'form-generator/form', '', 0, 0, 'icon-edit-square', 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1340949288542347266, 'name', 'Page Design', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1340949288542347266, 'name', '页面表单设计', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1340949288542347266, 'name', '頁面表單設計', 'zh-TW');

INSERT INTO sys_menu(id, pid, url, permissions, menu_type, open_style, icon, sort, creator, create_date, updater, update_date) VALUES (1304802103569809411, 1302850622416084993, 'devtools/project', '', 0, 0, 'icon-tags', 0, 1067246875800000001, now(), 1067246875800000001, now());
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1304802103569809411, 'name', 'Project Editor', 'en-US');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1304802103569809411, 'name', '项目名修改', 'zh-CN');
INSERT INTO sys_language(table_name, table_id, field_name, field_value, language) VALUES ('sys_menu', 1304802103569809411, 'name', '項目名修改', 'zh-TW');
