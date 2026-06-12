-- SET NAMES utf8mb4
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- Create Database
CREATE DATABASE IF NOT EXISTS school_system CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE school_system;

-- (1) 课程介绍信息表 test
CREATE TABLE `test` (
    `tid` INT AUTO_INCREMENT PRIMARY KEY,
    `ttitle` VARCHAR(255) NOT NULL COMMENT '标题',
    `tcontent` TEXT COMMENT '课程信息描述'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- (2) 班级信息表 classes
CREATE TABLE `classes` (
    `cid` INT AUTO_INCREMENT PRIMARY KEY,
    `cname` VARCHAR(100) NOT NULL COMMENT '名称',
    `cdescript` VARCHAR(255) COMMENT '班级描述'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- (3) 教师用户信息表 teacher
CREATE TABLE `teacher` (
    `tid` INT AUTO_INCREMENT PRIMARY KEY,
    `tname` VARCHAR(50) NOT NULL COMMENT '姓名',
    `tpassword` VARCHAR(100) NOT NULL COMMENT '密码',
    `tdate` DATE COMMENT '出生日期',
    `tpic` VARCHAR(255) COMMENT '教师照片',
    `tdescript` TEXT COMMENT '教师描述',
    `tno` VARCHAR(50) UNIQUE NOT NULL COMMENT '教师编号'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- (4) 学生用户信息表 user
CREATE TABLE `user` (
    `uid` INT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL COMMENT '姓名',
    `userpassword` VARCHAR(100) NOT NULL COMMENT '密码',
    `usersex` VARCHAR(10) COMMENT '性别',
    `userno` VARCHAR(50) UNIQUE NOT NULL COMMENT '学生编号',
    `userdescript` TEXT COMMENT '学生描述',
    `class_id` INT COMMENT '班级id',
    `upic` VARCHAR(255) COMMENT '学生照片',
    `youxiuok` VARCHAR(10) DEFAULT '否' COMMENT '是否优先/优秀',
    `checkedok` VARCHAR(10) DEFAULT '待审核' COMMENT '是否审核通过',
    `classname` VARCHAR(100) COMMENT '班级名称',
    FOREIGN KEY (`class_id`) REFERENCES `classes`(`cid`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- (5) 教学内容信息表 course
CREATE TABLE `course` (
    `cid` INT AUTO_INCREMENT PRIMARY KEY,
    `ctitle` VARCHAR(255) NOT NULL COMMENT '教学内容名称',
    `ccontent` TEXT COMMENT '教学内容',
    `efile` VARCHAR(255) COMMENT '相关文件'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- (6) 实验内容信息表 experiment
CREATE TABLE `experiment` (
    `eid` INT AUTO_INCREMENT PRIMARY KEY,
    `etitle` VARCHAR(255) NOT NULL COMMENT '实验内容名称',
    `econtent` TEXT COMMENT '实验内容',
    `efile` VARCHAR(255) COMMENT '相关文件'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- (7) 互动交流信息表 interaction
CREATE TABLE `interaction` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(50) COMMENT '名字',
    `comask` TEXT COMMENT '提问内容',
    `asktime` DATETIME COMMENT '提问时间',
    `replname` VARCHAR(50) COMMENT '回答者姓名',
    `comrepl` TEXT COMMENT '回答内容',
    `repltime` DATETIME COMMENT '回答时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- (8) 技术动态信息表 news
CREATE TABLE `news` (
    `nid` INT AUTO_INCREMENT PRIMARY KEY,
    `newstitle` VARCHAR(255) NOT NULL COMMENT '标题',
    `newscontent` TEXT COMMENT '内容',
    `newsdate` DATETIME COMMENT '时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Seeding Data (30 records per table)

-- Classes
INSERT INTO `classes` (cname, cdescript) VALUES 
('计算机2101', '计算机科学与技术班'), ('计算机2102', '计算机科学与技术班'), ('软件2101', '软件工程实验班'), ('软件2102', '软件工程普通班'), ('网络2101', '网络工程班'),
('大数据2101', '大数据技术班'), ('AI2101', '人工智能实验班'), ('物联网2101', '物联网工程班'), ('安防2101', '信息安全班'), ('通信2101', '通信工程班'),
('自动化2101', '自动化技术班'), ('电子2101', '电子信息班'), ('机械2101', '机械制造班'), ('土木2101', '土木工程班'), ('建筑2101', '建筑设计班'),
('管理2101', '工商管理班'), ('会计2101', '财务会计班'), ('金融2101', '金融学班'), ('法律2101', '法学班'), ('外语2101', '英语专业班'),
('数学2101', '数学应用班'), ('物理2101', '物理实验班'), ('化学2101', '应用化学班'), ('生物2101', '生物工程班'), ('艺术2101', '美学设计班'),
('体育2101', '体育教育班'), ('音乐2101', '音乐表演班'), ('舞蹈2101', '舞蹈编导班'), ('表演2101', '戏剧影视班'), ('新闻2101', '新闻传播班');

-- Teachers
INSERT INTO `teacher` (tname, tpassword, tdate, tpic, tdescript, tno) VALUES 
('张老师', '123456', '1980-05-12', 't1.jpg', '高级讲师，擅长Java开发', 'T001'), 
('李老师', '123456', '1982-08-20', 't2.jpg', '副教授，专研数据库系统', 'T002'),
('王老师', '123456', '1975-03-15', 't3.jpg', '资深专家，区块链方向', 'T003'),
('赵老师', '123456', '1988-11-22', 't4.jpg', '博士，人工智能专家', 'T004'),
('孙老师', '123456', '1983-02-10', 't5.jpg', '软件工程师，实战经验丰富', 'T005'),
('周老师', '123456', '1979-06-30', 't6.jpg', '网络安全顾问', 'T006'),
('吴老师', '123456', '1985-09-05', 't7.jpg', '移动开发专家', 'T007'),
('郑老师', '123456', '1981-12-12', 't8.jpg', '由于云计算领域研究', 'T008'),
('冯老师', '123456', '1977-04-18', 't9.jpg', '计算机图形学专家', 'T009'),
('陈老师', '123456', '1984-07-25', 't10.jpg', '编译原理权威', 'T010'),
('褚老师', '123456', '1986-10-01', 't11.jpg', '操作系统研究员', 'T011'),
('卫老师', '123456', '1980-01-20', 't12.jpg', '嵌入式系统专家', 'T012'),
('蒋老师', '123456', '1978-05-15', 't13.jpg', '分布式计算专家', 'T013'),
('沈老师', '123456', '1982-03-03', 't14.jpg', '数据挖掘专家', 'T014'),
('韩老师', '123456', '1989-08-08', 't15.jpg', '机器学习研究员', 'T015'),
('杨老师', '123456', '1983-12-25', 't16.jpg', '计算机视觉专家', 'T016'),
('朱老师', '123456', '1987-06-12', 't17.jpg', 'NLP自然语言处理专家', 'T017'),
('秦老师', '123456', '1981-09-30', 't18.jpg', '信息检索专家', 'T018'),
('尤老师', '123456', '1976-11-11', 't19.jpg', '人机交互专家', 'T019'),
('许老师', '123456', '1985-02-14', 't20.jpg', '软件测试专家', 'T020'),
('何老师', '123456', '1980-04-01', 't21.jpg', '项目管理专家', 'T021'),
('吕老师', '123456', '1988-07-07', 't22.jpg', '敏捷开发教练', 'T022'),
('施老师', '123456', '1982-10-10', 't23.jpg', 'DevOps架构师', 'T023'),
('张三老师', '123456', '1984-05-20', 't24.jpg', '全栈开发工程师', 'T024'),
('孔老师', '123456', '1979-08-15', 't25.jpg', '系统架构师', 'T025'),
('曹老师', '123456', '1981-01-05', 't26.jpg', '微服务专家', 'T026'),
('严老师', '123456', '1983-09-18', 't27.jpg', '大数据分析师', 'T027'),
('华老师', '123456', '1986-12-30', 't28.jpg', '前端高级开发', 'T028'),
('金老师', '123456', '1980-02-28', 't29.jpg', 'UI/UX设计师', 'T029'),
('魏老师', '123456', '1987-04-12', 't30.jpg', '交互设计专家', 'T030');

-- Course Intro (test table)
INSERT INTO `test` (ttitle, tcontent) VALUES 
('Java程序设计', '学习Java基础语法、面向对象编程、异常处理及多线程技术。'),
('数据库原理', '掌握SQL语言、数据库设计范式、事务管理及索引优化。'),
('计算机网络', '深入理解OSI模型、TCP/IP协议族及网络应用开发。'),
('数据结构', '学习链表、树、图等基础结构及排序、查找算法。'),
('操作系统', '探讨进程管理、内存分配、文件系统及设备管理。'),
('软件工程', '学习生命周期模型、需求分析、设计模式及软件测试。'),
('离散数学', '计算机科学的逻辑基础，包括集合、图论、逻辑。'),
('人工智能导论', '探索机器学习、神经网络、深度学习等前沿技术。'),
('算法设计与分析', '掌握贪心、动态规划、回溯等复杂算法设计思路。'),
('计算机组成原理', '理解硬件构造、指令系统、中央处理器及存储架构。'),
('Web开发基础', 'HTML5, CSS3, JavaScript基础及网页布局实现。'),
('Python数据分析', '利用Python及NumPy, Pandas等库进行数据透视。'),
('区块链技术', '共识算法、智能合约、加密货币及去中心化应用。'),
('移动应用开发', 'Android或iOS应用架构设计与生命周期管理。'),
('云计算架构', '虚拟化技术、Docker容器、K8s编排及云原生应用。'),
('编译原理', '词法分析、语法分析、语义分析及代码生成过程。'),
('嵌入式系统', 'ARM架构、实时操作系统及其在物联网中的应用。'),
('网络安全', '加密解密、漏洞扫描、防火墙配置及安全协议。'),
('人机交互', '用户体验设计、交互模型及设计评价准则。'),
('软件测试', '黑盒测试、白盒测试、回归测试及自动化工具使用。'),
('分布式系统', '分布式一致性、RPC调用、消息队列及负载均衡。'),
('机器学习', '监督学习、非监督学习、强化学习及其算法实现。'),
('大数据技术', 'Hadoop, Spark生态圈及非关系型数据库应用。'),
('计算机视觉', '图像处理基础、特征检测、物体识别及OpenCV。'),
('自然语言处理', '文本分类、情感分析、机器翻译及预训练模型。'),
('信号与系统', '连续与离散信号的频域分析及系统稳定性研究。'),
('数字电路', '逻辑门控制、时序电路、触发器及FPGA基础。'),
('信息检索', '搜索引擎原理、倒排索引、TF-IDF及排序算法。'),
('多媒体技术', '视音频压缩、流媒体传输及交互式媒体制作。'),
('职业素质与规划', 'IT行业现状分析、面试技巧及职业道德准则。');

-- Users (Students)
INSERT INTO `user` (username, userpassword, usersex, userno, userdescript, class_id, upic, youxiuok, checkedok, classname) VALUES 
('小明', '123456', '男', 'S2021001', '勤奋好学的学生', 1, 's1.jpg', '是', '已通过', '计算机2101'),
('小红', '123456', '女', 'S2021002', '热衷于前端设计', 1, 's2.jpg', '否', '已通过', '计算机2101'),
('小王', '123456', '男', 'S2021003', '算法竞赛选手', 2, 's3.jpg', '是', '已通过', '计算机2102'),
('小李', '123456', '女', 'S2021004', '数学天才', 2, 's4.jpg', '否', '待审核', '计算机2102'),
('小赵', '123456', '男', 'S2021005', '喜欢开源社区', 3, 's5.jpg', '否', '已通过', '软件2101'),
('小孙', '123456', '女', 'S2021006', '追求极致交互', 3, 's6.jpg', '是', '已通过', '软件2101'),
('小周', '123456', '男', 'S2021007', '网络攻防专家', 4, 's7.jpg', '否', '已通过', '软件2102'),
('小吴', '123456', '女', 'S2021008', '擅长文档编写', 4, 's8.jpg', '否', '待审核', '软件2102'),
('小郑', '123456', '男', 'S2021009', '对硬件感兴趣', 5, 's9.jpg', '否', '已通过', '网络2101'),
('小冯', '123456', '女', 'S2021010', '热爱生活', 5, 's10.jpg', '否', '已通过', '网络2101'),
('小陈', '123456', '男', 'S2021011', '音乐发烧友', 6, 's11.jpg', '否', '已通过', '大数据2101'),
('小褚', '123456', '女', 'S2021012', '艺术特长生', 6, 's12.jpg', '否', '已通过', '大数据2101'),
('小卫', '123456', '男', 'S2021013', '体育健将', 7, 's13.jpg', '否', '已通过', 'AI2101'),
('小蒋', '123456', '女', 'S2021014', '深度学习迷', 7, 's14.jpg', '是', '已通过', 'AI2101'),
('小沈', '123456', '男', 'S2021015', '未来程序员', 8, 's15.jpg', '否', '已通过', '物联网2101'),
('小韩', '123456', '女', 'S2021016', '精通多种门语言', 8, 's16.jpg', '否', '待审核', '物联网2101'),
('小杨', '123456', '男', 'S2021017', '喜欢摄影', 9, 's17.jpg', '否', '已通过', '安防2101'),
('小朱', '123456', '女', 'S2021018', '擅长交流', 9, 's18.jpg', '否', '已通过', '安防2101'),
('小秦', '123456', '男', 'S2021019', '逻辑思维强', 10, 's19.jpg', '否', '已通过', '通信2101'),
('小尤', '123456', '女', 'S2021020', '英语达人', 10, 's20.jpg', '否', '已通过', '通信2101'),
('小许', '123456', '男', 'S2021021', '喜欢旅行', 11, 's21.jpg', '否', '已通过', '自动化2101'),
('小何', '123456', '女', 'S2021022', '绘画专家', 11, 's22.jpg', '否', '已通过', '自动化2101'),
('小吕', '123456', '男', 'S2021023', '科幻迷', 12, 's23.jpg', '否', '已通过', '电子2101'),
('小施', '123456', '女', 'S2021024', '美食家', 12, 's24.jpg', '否', '待审核', '电子2101'),
('小张', '123456', '男', 'S2021025', '热于助人', 13, 's25.jpg', '否', '已通过', '机械2101'),
('小孔', '123456', '女', 'S2021026', '沉稳冷静', 13, 's26.jpg', '否', '已通过', '机械2101'),
('小曹', '123456', '男', 'S2021027', '博览群书', 14, 's27.jpg', '否', '已通过', '土木2101'),
('小严', '123456', '女', 'S2021028', '细心严谨', 14, 's28.jpg', '否', '已通过', '土木2101'),
('小华', '123456', '男', 'S2021029', '自信大方', 15, 's29.jpg', '否', '已通过', '建筑2101'),
('小金', '123456', '女', 'S2021030', '活泼开朗', 15, 's30.jpg', '否', '待审核', '建筑2101');

-- Courses (Teaching Content)
INSERT INTO `course` (ctitle, ccontent, efile) VALUES 
('Java第1章：基础简介', 'Java的历史与开发环境搭建。', 'chapter1.pdf'),
('Java第2章：数据类型', '基本数据类型与引用数据类型详解。', 'chapter2.pdf'),
('Java第3章：控制流', 'if-else, switch, loop控制结构。', 'chapter3.pdf'),
('Java第4章：类与对象', '封装、继承、多态三大特性。', 'chapter4.pdf'),
('Java第5章：常用类', 'String, Date, Math等工具类。', 'chapter5.pdf'),
('Java第6章：集合框架', 'List, Set, Map接口及其实现类。', 'chapter6.pdf'),
('Java第7章：IO流', '字节流与字符流的操作。', 'chapter7.pdf'),
('Java第8章：多线程', '线程生命周期与同步机制。', 'chapter8.pdf'),
('数据库第1章：绪论', '数据库系统架构。', 'db1.pdf'),
('数据库第2章：关系模型', '关系代数与完整性约束。', 'db2.pdf'),
('数据库第3章：SQL语言', '增删改查语法基础。', 'db3.pdf'),
('数据库第4章：规范化', '第一、二、三范式推导。', 'db4.pdf'),
('网络第1章：概述', '分层协议模型简介。', 'net1.pdf'),
('网络第2章：物理层', '传输介质与信道技术。', 'net2.pdf'),
('网络第3章：链路层', '以太网协议与MAC地址。', 'net3.pdf'),
('网络第4章：网络层', 'IP地址划分与路由协议。', 'net4.pdf'),
('OS第1章：概述', '操作系统的功能与分类。', 'os1.pdf'),
('OS第2章：进程管理', '进程调度算法实现。', 'os2.pdf'),
('OS第3章：存储管理', '分页与分段技术。', 'os3.pdf'),
('OS第4章：文件管理', '目录结构与存储空间分配。', 'os4.pdf'),
('新一代Web技术', 'React与Vue框架对比。', 'web_new.pdf'),
('移动开发前沿', 'Flutter跨平台开发实践。', 'mobile_edge.pdf'),
('AI与生活', '人工智能在日常生活中的应用。', 'ai_life.pdf'),
('网络安全攻防', '常见Web攻击防御策略。', 'security_lab.pdf'),
('性能优化实战', '后端接口响应时间优化。', 'perf_opt.pdf'),
('微服务实战', 'Spring Cloud微服务治理。', 'microservice.pdf'),
('大数据挖掘', '用户画像系统构建。', 'bigdata_mining.pdf'),
('容器化技术', 'Docker镜像打包流程。', 'docker_pkg.pdf'),
('算法高级进阶', '图论最短路径算法分析。', 'algo_adv.pdf'),
('软件架构设计', '整齐架构与领域驱动设计。', 'arch_design.pdf');

-- Experiments
INSERT INTO `experiment` (etitle, econtent, efile) VALUES 
('Java实验1：环境搭建', '安装JDK并编写HelloWorld。', 'exp1.zip'),
('Java实验2：控制结构', '实现九九乘法表。', 'exp2.zip'),
('Java实验3：面向对象', '设计一个简单的图书管理系统类模型。', 'exp3.zip'),
('Java实验4：异常处理', '编写自定义异常并进行捕获。', 'exp4.zip'),
('Java实验5：文件读写', '通过IO流读取本地文档内容。', 'exp5.zip'),
('Java实验6：网络编程', '实现简单的Socket聊天程序。', 'exp6.zip'),
('数据库实验1：建表', '使用SQL语句创建学生成绩表。', 'dbexp1.zip'),
('数据库实验2：查询', '进行复杂的多表连接查询练习。', 'dbexp2.zip'),
('数据库实验3：视图', '创建视图并优化查询流程。', 'dbexp3.zip'),
('数据库实验4：触发器', '实现审计日志的自动记录。', 'dbexp4.zip'),
('网络实验1：抓包分析', '使用Wireshark抓取HTTP请求。', 'netexp1.zip'),
('网络实验2：路由配置', '静态路由与RIP协议配置。', 'netexp2.zip'),
('网络实验3：子网划分', '规划企业内网IP段。', 'netexp3.zip'),
('OS实验1：进程调度', '模拟FCFS调度算法过程。', 'osexp1.zip'),
('OS实验2：内存分配', '模拟首次适应分配算法。', 'osexp2.zip'),
('OS实验3：文件系统', '模拟FAT文件系统寻址。', 'osexp3.zip'),
('算法实验1：分治法', '实现大整数乘法。', 'algoexp1.zip'),
('算法实验2：动态规划', '解决01背包问题。', 'algoexp2.zip'),
('算法实验3：贪心算法', '实现赫夫曼编码。', 'algoexp3.zip'),
('AI实验1：逻辑回归', '通过Python实现手写数字识别。', 'aiexp1.zip'),
('AI实验2：神经网络', '搭建简单的卷积神经网络模型。', 'aiexp2.zip'),
('Web实验1：静态页面', '制作个人简介网页。', 'webexp1.zip'),
('Web实验2：动态交互', '使用JS实现轮播图效果。', 'webexp2.zip'),
('嵌入式实验1：LED', '编写代码控制开发板灯光。', 'embexp1.zip'),
('安防实验1：防火墙', '配置iptables规则过滤流量。', 'secu_exp1.zip'),
('系统综合实验1', '整合前后端开发。', 'comp1.zip'),
('系统综合实验2', '测试与性能调优。', 'comp2.zip'),
('系统综合实验3', '编写技术报告。', 'comp3.zip'),
('职业素质实验', '模拟面试。', 'prof_exp1.zip'),
('创新实验课题', '自主选定课题开发。', 'innov_exp1.zip');

-- Interactions
INSERT INTO `interaction` (`name`, `comask`, `asktime`, `replname`, `comrepl`, `repltime`) VALUES 
('小明', '老师，Java中的接口和抽象类有什么区别？', '2026-02-20 10:00:00', '张老师', '接口主要是行为的契约，而抽象类是模板的复用。', '2026-02-20 11:00:00'),
('小红', 'CSS中的Flex布局怎么水平居中？', '2026-02-20 14:00:00', '华老师', '设置justify-content: center; 即可。', '2026-02-20 15:00:00'),
('小王', '数据库索引失效的情况有哪些？', '2026-02-21 09:00:00', '李老师', '比如使用了like在前缀通配符，或者违反了最左匹配原则。', '2026-02-21 10:30:00'),
('小李', 'Spring Boot如何配置跨域？', '2026-02-21 16:00:00', '陈老师', '可以使用WebMvcConfigurer或者@CrossOrigin注解。', '2026-02-21 17:00:00'),
('小赵', 'Docker Compose的作用是什么？', '2026-02-22 08:30:00', '施老师', '用于定义和运行多容器Docker应用程序的工具。', '2026-02-22 09:15:00'),
('小孙', '算法时间复杂度怎么估算？', '2026-02-22 13:00:00', '周老师', '通常看最深层循环的执行次数。', '2026-02-22 14:00:00'),
('小周', 'Linux查看端口占用的命令是什么？', '2026-02-23 10:10:00', '褚老师', 'lsof -i :port 或 netstat -tunlp。', '2026-02-23 11:00:00'),
('小张', 'Vue3相比Vue2最大的变化是？', '2026-02-23 15:00:00', '华老师', '引入了Composition API和更快的渲染性能。', '2026-02-23 16:00:00'),
('小明', 'TCP三次握手的过程？', '2026-02-24 09:20:00', '卫老师', 'SYN -> SYN-ACK -> ACK。', '2026-02-24 10:00:00'),
('小红', '什么是死锁？怎么避免？', '2026-02-24 11:00:00', '蒋老师', '资源互相等待形成的环路；可以通过有序申请资源来避免。', '2026-02-24 12:00:00'),
('小明', '提问11', '2026-02-24 12:00:00', '陈老师', '回答11', '2026-02-24 13:00:00'),
('小王', '提问12', '2026-02-24 13:00:00', '陈老师', '回答12', '2026-02-24 14:00:00'),
('小李', '提问13', '2026-02-24 14:00:00', '陈老师', '回答13', '2026-02-24 15:00:00'),
('小赵', '提问14', '2026-02-24 15:00:00', '陈老师', '回答14', '2026-02-24 16:00:00'),
('小孙', '提问15', '2026-02-24 16:00:00', '陈老师', '回答15', '2026-02-24 17:00:00'),
('小周', '提问16', '2026-02-24 17:00:00', '陈老师', '回答16', '2026-02-24 18:00:00'),
('小吴', '提问17', '2026-02-25 09:00:00', NULL, NULL, NULL),
('小郑', '提问18', '2026-02-25 10:00:00', NULL, NULL, NULL),
('小冯', '提问19', '2026-02-25 11:00:00', NULL, NULL, NULL),
('小陈', '提问20', '2026-02-25 12:00:00', NULL, NULL, NULL),
('小褚', '提问21', '2026-02-25 13:00:00', NULL, NULL, NULL),
('小卫', '提问22', '2026-02-25 14:00:00', NULL, NULL, NULL),
('小蒋', '提问23', '2026-02-25 15:00:00', NULL, NULL, NULL),
('小沈', '提问24', '2026-02-25 16:00:00', NULL, NULL, NULL),
('小韩', '提问25', '2026-02-25 17:00:00', NULL, NULL, NULL),
('小杨', '提问26', '2026-02-26 09:00:00', NULL, NULL, NULL),
('小朱', '提问27', '2026-02-26 10:00:00', NULL, NULL, NULL),
('小秦', '提问28', '2026-02-26 11:00:00', NULL, NULL, NULL),
('小尤', '提问29', '2026-02-26 12:00:00', NULL, NULL, NULL),
('小许', '提问30', '2026-02-26 13:00:00', NULL, NULL, NULL);

-- (9) 成绩权重配置表 score_config
CREATE TABLE `score_config` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `course_id` INT NOT NULL COMMENT '课程id(test.tid)',
    `class_id` INT NOT NULL COMMENT '班级id(classes.cid)',
    `regular_weight` DECIMAL(5,2) DEFAULT 30.00 COMMENT '平时分权重(%)',
    `midterm_weight` DECIMAL(5,2) DEFAULT 30.00 COMMENT '期中权重(%)',
    `final_weight` DECIMAL(5,2) DEFAULT 40.00 COMMENT '期末权重(%)',
    `score_precision` TINYINT DEFAULT 1 COMMENT '总评成绩保留小数位数',
    `grade_excellent` DECIMAL(5,2) DEFAULT 90.00 COMMENT '优秀分数线',
    `grade_good` DECIMAL(5,2) DEFAULT 80.00 COMMENT '良好分数线',
    `grade_medium` DECIMAL(5,2) DEFAULT 70.00 COMMENT '中等分数线',
    `grade_pass` DECIMAL(5,2) DEFAULT 60.00 COMMENT '及格分数线',
    `is_locked` TINYINT DEFAULT 0 COMMENT '是否锁定：0-未锁定，1-已锁定',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_course_class` (`course_id`, `class_id`),
    FOREIGN KEY (`course_id`) REFERENCES `test`(`tid`) ON DELETE CASCADE,
    FOREIGN KEY (`class_id`) REFERENCES `classes`(`cid`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成绩权重配置表';

-- (10) 学生成绩表 score
CREATE TABLE `score` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `course_id` INT NOT NULL COMMENT '课程id(test.tid)',
    `class_id` INT NOT NULL COMMENT '班级id(classes.cid)',
    `student_id` INT NOT NULL COMMENT '学生id(user.uid)',
    `student_name` VARCHAR(50) COMMENT '学生姓名(冗余)',
    `student_no` VARCHAR(50) COMMENT '学号(冗余)',
    `regular_score` DECIMAL(5,2) DEFAULT NULL COMMENT '平时分',
    `midterm_score` DECIMAL(5,2) DEFAULT NULL COMMENT '期中分',
    `final_score` DECIMAL(5,2) DEFAULT NULL COMMENT '期末分',
    `total_score` DECIMAL(5,2) DEFAULT NULL COMMENT '总评分',
    `grade` VARCHAR(10) DEFAULT NULL COMMENT '等级：优/良/中/及格/不及格',
    `class_rank` INT DEFAULT NULL COMMENT '班级内排名',
    `status` VARCHAR(20) DEFAULT 'normal' COMMENT '状态：normal-正常，absent-缺考，unrecorded-缺录',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_course_class_student` (`course_id`, `class_id`, `student_id`),
    KEY `idx_course_class` (`course_id`, `class_id`),
    KEY `idx_student` (`student_id`),
    FOREIGN KEY (`course_id`) REFERENCES `test`(`tid`) ON DELETE CASCADE,
    FOREIGN KEY (`class_id`) REFERENCES `classes`(`cid`) ON DELETE CASCADE,
    FOREIGN KEY (`student_id`) REFERENCES `user`(`uid`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生成绩表';

-- News
INSERT INTO `news` (newstitle, newscontent, newsdate) VALUES 
('Java 21 正式发布', 'Java 21 带来了虚拟线程等重大特性。', '2026-02-01 10:00:00'),
('Spring Boot 3.2 特性概览', '支持虚拟线程和GraalVM。', '2026-02-02 11:00:00'),
('MySQL 8.4 版本更新', '安全性与性能的进一步提升。', '2026-02-03 12:00:00'),
('Bootstrap 6 开发动态', '全新的排版系统与组件。', '2026-02-04 13:00:00'),
('人工智能助力教育转型', 'AI在个性化教学中的应用。', '2026-02-05 14:00:00'),
('云计算行业趋势报告', '云原生已成为行业标准。', '2026-02-06 15:00:00'),
('区块链技术赋能供应链', '提高透明度与可追溯性。', '2026-02-07 16:00:00'),
('网络安全法新规解读', '加强数据保护与合规性。', '2026-02-08 17:00:00'),
('鸿蒙系统开发者大会', '共建全场景智慧生态。', '2026-02-09 18:00:00'),
('2026 IT 行业人才需求', '全栈开发与AI人才紧缺。', '2026-02-10 19:00:00'),
('技术前沿11', '内容详情11', '2026-02-11 08:00:00'),
('技术前沿12', '内容详情12', '2026-02-12 08:00:00'),
('技术前沿13', '内容详情13', '2026-02-13 08:00:00'),
('技术前沿14', '内容详情14', '2026-02-14 08:00:00'),
('技术前沿15', '内容详情15', '2026-02-15 08:00:00'),
('技术前沿16', '内容详情16', '2026-02-16 08:00:00'),
('技术前沿17', '内容详情17', '2026-02-17 08:00:00'),
('技术前沿18', '内容详情18', '2026-02-18 08:00:00'),
('技术前沿19', '内容详情19', '2026-02-19 08:00:00'),
('技术前沿20', '内容详情20', '2026-02-20 08:00:00'),
('技术前沿21', '内容详情21', '2026-02-21 08:00:00'),
('技术前沿22', '内容详情22', '2026-02-22 08:00:00'),
('技术前沿23', '内容详情23', '2026-02-23 08:00:00'),
('技术前沿24', '内容详情24', '2026-02-24 08:00:00'),
('技术前沿25', '内容详情25', '2026-02-25 08:00:00'),
('技术前沿26', '内容详情26', '2026-02-26 08:00:00'),
('技术前沿27', '内容详情27', '2026-02-27 08:00:00'),
('技术前沿28', '内容详情28', '2026-02-28 08:00:00'),
('技术前沿29', '内容详情29', '2026-03-01 08:00:00'),
('技术前沿30', '内容详情30', '2026-03-02 08:00:00');

-- (11) 评教活动表
CREATE TABLE `evaluation_activity` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `title` VARCHAR(200) NOT NULL COMMENT '活动标题',
    `description` TEXT COMMENT '活动说明',
    `start_time` DATETIME NOT NULL COMMENT '评教开始时间',
    `end_time` DATETIME NOT NULL COMMENT '评教结束时间',
    `status` VARCHAR(20) DEFAULT 'ongoing' COMMENT '状态：ongoing-进行中，ended-已结束',
    `creator_id` INT COMMENT '创建人ID',
    `creator_type` VARCHAR(20) DEFAULT 'teacher' COMMENT '创建人类型：teacher/admin',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY `idx_status` (`status`),
    KEY `idx_time` (`start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评教活动表';

-- (12) 评价维度表
CREATE TABLE `evaluation_dimension` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `activity_id` INT NOT NULL COMMENT '活动ID',
    `name` VARCHAR(100) NOT NULL COMMENT '维度名称',
    `weight` DECIMAL(5,2) NOT NULL COMMENT '权重(%)',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY `idx_activity` (`activity_id`),
    FOREIGN KEY (`activity_id`) REFERENCES `evaluation_activity`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价维度表';

-- (13) 评教活动-班级关联表
CREATE TABLE `evaluation_activity_class` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `activity_id` INT NOT NULL COMMENT '活动ID',
    `class_id` INT NOT NULL COMMENT '班级ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_activity_class` (`activity_id`, `class_id`),
    FOREIGN KEY (`activity_id`) REFERENCES `evaluation_activity`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`class_id`) REFERENCES `classes`(`cid`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评教活动班级关联表';

-- (14) 评教活动-教师关联表
CREATE TABLE `evaluation_activity_teacher` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `activity_id` INT NOT NULL COMMENT '活动ID',
    `teacher_id` INT NOT NULL COMMENT '教师ID',
    `teacher_name` VARCHAR(50) COMMENT '教师姓名(冗余)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_activity_teacher` (`activity_id`, `teacher_id`),
    KEY `idx_teacher` (`teacher_id`),
    FOREIGN KEY (`activity_id`) REFERENCES `evaluation_activity`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`teacher_id`) REFERENCES `teacher`(`tid`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评教活动教师关联表';

-- (15) 评教提交记录表（防重复评价）
CREATE TABLE `evaluation_submit` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `activity_id` INT NOT NULL COMMENT '活动ID',
    `student_id` INT NOT NULL COMMENT '学生ID',
    `teacher_id` INT NOT NULL COMMENT '教师ID',
    `anonymous_token` VARCHAR(64) NOT NULL COMMENT '匿名令牌，用于关联评价内容',
    `submit_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_activity_student_teacher` (`activity_id`, `student_id`, `teacher_id`),
    KEY `idx_student` (`student_id`),
    KEY `idx_teacher` (`teacher_id`),
    FOREIGN KEY (`activity_id`) REFERENCES `evaluation_activity`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`student_id`) REFERENCES `user`(`uid`) ON DELETE CASCADE,
    FOREIGN KEY (`teacher_id`) REFERENCES `teacher`(`tid`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评教提交记录表';

-- (16) 评教评分记录表（匿名）
CREATE TABLE `evaluation_score` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `activity_id` INT NOT NULL COMMENT '活动ID',
    `teacher_id` INT NOT NULL COMMENT '教师ID',
    `dimension_id` INT NOT NULL COMMENT '维度ID',
    `score` TINYINT NOT NULL COMMENT '评分(1-5)',
    `anonymous_token` VARCHAR(64) NOT NULL COMMENT '匿名令牌',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY `idx_activity_teacher` (`activity_id`, `teacher_id`),
    KEY `idx_dimension` (`dimension_id`),
    KEY `idx_token` (`anonymous_token`),
    FOREIGN KEY (`activity_id`) REFERENCES `evaluation_activity`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`teacher_id`) REFERENCES `teacher`(`tid`) ON DELETE CASCADE,
    FOREIGN KEY (`dimension_id`) REFERENCES `evaluation_dimension`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评教评分记录表';

-- (17) 评教评语表（匿名）
CREATE TABLE `evaluation_comment` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `activity_id` INT NOT NULL COMMENT '活动ID',
    `teacher_id` INT NOT NULL COMMENT '教师ID',
    `comment` TEXT COMMENT '文字评语',
    `anonymous_token` VARCHAR(64) NOT NULL COMMENT '匿名令牌',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY `idx_activity_teacher` (`activity_id`, `teacher_id`),
    KEY `idx_token` (`anonymous_token`),
    FOREIGN KEY (`activity_id`) REFERENCES `evaluation_activity`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`teacher_id`) REFERENCES `teacher`(`tid`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评教评语表';

-- Seeding evaluation data
INSERT INTO `evaluation_activity` (`title`, `description`, `start_time`, `end_time`, `status`, `creator_id`) VALUES 
('2026春季学期教学评价', '请同学们根据本学期各位老师的教学情况，实事求是地进行评价，您的意见对我们非常重要。', '2026-06-01 00:00:00', '2026-07-15 23:59:59', 'ongoing', 1),
('2025秋季学期教学评价', '2025秋季学期教师教学质量评价活动。', '2025-12-01 00:00:00', '2026-01-15 23:59:59', 'ended', 2);

INSERT INTO `evaluation_dimension` (`activity_id`, `name`, `weight`, `sort_order`) VALUES 
(1, '教学态度', 25.00, 1),
(1, '内容质量', 35.00, 2),
(1, '答疑及时性', 20.00, 3),
(1, '课堂互动', 20.00, 4),
(2, '教学态度', 30.00, 1),
(2, '内容质量', 40.00, 2),
(2, '答疑及时性', 30.00, 3);

INSERT INTO `evaluation_activity_class` (`activity_id`, `class_id`) VALUES 
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5),
(2, 1), (2, 2), (2, 3);

INSERT INTO `evaluation_activity_teacher` (`activity_id`, `teacher_id`, `teacher_name`) VALUES 
(1, 1, '张老师'), (1, 2, '李老师'), (1, 3, '王老师'), (1, 4, '赵老师'), (1, 5, '孙老师'),
(2, 1, '张老师'), (2, 2, '李老师'), (2, 3, '王老师');

INSERT INTO `evaluation_submit` (`activity_id`, `student_id`, `teacher_id`, `anonymous_token`) VALUES 
(2, 1, 1, 'tok_abc123def456'),
(2, 1, 2, 'tok_xyz789uvw012'),
(2, 2, 1, 'tok_rst345opq678'),
(2, 2, 2, 'tok_mno901jkl234');

INSERT INTO `evaluation_score` (`activity_id`, `teacher_id`, `dimension_id`, `score`, `anonymous_token`) VALUES 
(2, 1, 5, 5, 'tok_abc123def456'),
(2, 1, 6, 4, 'tok_abc123def456'),
(2, 1, 7, 5, 'tok_abc123def456'),
(2, 2, 5, 4, 'tok_xyz789uvw012'),
(2, 2, 6, 5, 'tok_xyz789uvw012'),
(2, 2, 7, 3, 'tok_xyz789uvw012'),
(2, 1, 5, 4, 'tok_rst345opq678'),
(2, 1, 6, 5, 'tok_rst345opq678'),
(2, 1, 7, 4, 'tok_rst345opq678'),
(2, 2, 5, 5, 'tok_mno901jkl234'),
(2, 2, 6, 4, 'tok_mno901jkl234'),
(2, 2, 7, 5, 'tok_mno901jkl234');

INSERT INTO `evaluation_comment` (`activity_id`, `teacher_id`, `comment`, `anonymous_token`) VALUES 
(2, 1, '老师讲课非常认真，知识点讲解清晰，很有耐心。', 'tok_abc123def456'),
(2, 2, '内容很充实，但希望课后答疑能更及时一些。', 'tok_xyz789uvw012'),
(2, 1, '课堂氛围很好，老师很有感染力。', 'tok_rst345opq678'),
(2, 2, '教学内容很有深度，收获很大。', 'tok_mno901jkl234');

-- (18) 教学日历事件表
CREATE TABLE `calendar_event` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `title` VARCHAR(255) NOT NULL COMMENT '事件标题',
    `event_type` VARCHAR(50) NOT NULL DEFAULT 'activity' COMMENT '事件类型：exam-考试, lecture-讲座, experiment-实验, activity-活动',
    `start_time` DATETIME NOT NULL COMMENT '开始时间',
    `end_time` DATETIME NOT NULL COMMENT '结束时间',
    `location` VARCHAR(255) DEFAULT NULL COMMENT '地点',
    `remark` TEXT COMMENT '备注',
    `creator_id` INT DEFAULT NULL COMMENT '创建人ID(教师)',
    `creator_name` VARCHAR(50) DEFAULT NULL COMMENT '创建人姓名(冗余)',
    `is_archived` TINYINT DEFAULT 0 COMMENT '是否归档：0-未归档，1-已归档(过期事件自动归档)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY `idx_time` (`start_time`, `end_time`),
    KEY `idx_type` (`event_type`),
    KEY `idx_archived` (`is_archived`),
    KEY `idx_creator` (`creator_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教学日历事件表';

-- (19) 事件-班级关联表
CREATE TABLE `calendar_event_class` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `event_id` INT NOT NULL COMMENT '事件ID',
    `class_id` INT NOT NULL COMMENT '班级ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_event_class` (`event_id`, `class_id`),
    FOREIGN KEY (`event_id`) REFERENCES `calendar_event`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`class_id`) REFERENCES `classes`(`cid`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教学日历事件班级关联表';

-- Seeding calendar event data
INSERT INTO `calendar_event` (`title`, `event_type`, `start_time`, `end_time`, `location`, `remark`, `creator_id`, `creator_name`, `is_archived`) VALUES
('Java程序设计期末考试', 'exam', '2026-06-20 09:00:00', '2026-06-20 11:00:00', '教学楼A-301', '闭卷考试，请携带学生证', 1, '张老师', 0),
('数据库原理期中考试', 'exam', '2026-06-15 14:00:00', '2026-06-15 16:00:00', '教学楼B-205', '开卷考试', 2, '李老师', 0),
('人工智能前沿讲座', 'lecture', '2026-06-18 15:00:00', '2026-06-18 17:00:00', '学术报告厅', '特邀清华大学教授主讲', 4, '赵老师', 0),
('网络安全攻防实验', 'experiment', '2026-06-22 08:30:00', '2026-06-22 12:00:00', '实验楼C-402', '请提前安装Kali Linux', 6, '周老师', 0),
('班级团建活动', 'activity', '2026-06-25 14:00:00', '2026-06-25 18:00:00', '学校操场', '班级户外团建，请穿着运动装', 5, '孙老师', 0),
('云计算架构专题讲座', 'lecture', '2026-06-28 10:00:00', '2026-06-28 12:00:00', '教学楼A-501', 'Docker与K8s实战分享', 8, '郑老师', 0),
('数据结构课程设计答辩', 'exam', '2026-07-01 09:00:00', '2026-07-02 17:00:00', '教学楼B-301', '分组答辩，每组20分钟', 1, '张老师', 0),
('机器学习实验展示', 'experiment', '2026-07-03 13:30:00', '2026-07-03 16:30:00', '实验楼D-201', '展示训练模型与预测结果', 4, '赵老师', 0),
('Web开发技术沙龙', 'activity', '2026-07-05 19:00:00', '2026-07-05 21:00:00', '创新实验室', 'React与Vue技术分享交流', 7, '吴老师', 0),
('计算机网络期末考试', 'exam', '2026-07-08 09:00:00', '2026-07-08 11:30:00', '教学楼A-201', '闭卷考试，范围1-8章', 3, '王老师', 0),
('操作系统课程实验', 'experiment', '2026-06-10 08:00:00', '2026-06-12 18:00:00', '实验楼C-301', '进程调度模拟实验', 11, '褚老师', 0),
('数据库设计大赛', 'activity', '2026-06-28 09:00:00', '2026-06-29 18:00:00', '计算机学院', '院级数据库设计竞赛', 2, '李老师', 0),
('算法竞赛校内选拔', 'exam', '2026-05-20 09:00:00', '2026-05-20 12:00:00', '教学楼A-401', 'ACM校内选拔赛', 1, '张老师', 1),
('春季读书分享会', 'activity', '2026-05-15 14:00:00', '2026-05-15 17:00:00', '图书馆报告厅', '好书推荐与读后感分享', 5, '孙老师', 1);

INSERT INTO `calendar_event_class` (`event_id`, `class_id`) VALUES
(1, 1), (1, 2),
(2, 1), (2, 2),
(3, 1), (3, 2), (3, 3), (3, 7),
(4, 5), (4, 9),
(5, 1),
(6, 3), (6, 7),
(7, 1), (7, 2),
(8, 7),
(9, 3), (9, 4),
(10, 5),
(11, 1), (11, 2),
(12, 1), (12, 2), (12, 3),
(13, 1), (13, 2), (13, 3),
(14, 1), (14, 2);

-- (20) 资料库目录表
CREATE TABLE `repository_folder` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(200) NOT NULL COMMENT '目录名称',
    `parent_id` INT DEFAULT 0 COMMENT '父目录ID，0表示根目录',
    `path` VARCHAR(500) DEFAULT '/' COMMENT '目录路径，如/课程资料/Java/',
    `depth` INT DEFAULT 1 COMMENT '目录深度，根目录为1',
    `visibility_type` VARCHAR(20) DEFAULT 'ALL' COMMENT '可见范围：ALL-全体学生，CLASSES-指定班级',
    `creator_id` INT COMMENT '创建人ID(教师)',
    `creator_name` VARCHAR(50) COMMENT '创建人姓名(冗余)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY `idx_parent` (`parent_id`),
    KEY `idx_path` (`path`(255)),
    KEY `idx_visibility` (`visibility_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资料库目录表';

-- (21) 资料库目录-班级关联表
CREATE TABLE `repository_folder_class` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `folder_id` INT NOT NULL COMMENT '目录ID',
    `class_id` INT NOT NULL COMMENT '班级ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_folder_class` (`folder_id`, `class_id`),
    KEY `idx_folder` (`folder_id`),
    KEY `idx_class` (`class_id`),
    FOREIGN KEY (`folder_id`) REFERENCES `repository_folder`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`class_id`) REFERENCES `classes`(`cid`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资料库目录班级关联表';

-- (22) 资料库文件表
CREATE TABLE `repository_file` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(255) NOT NULL COMMENT '文件显示名称',
    `original_name` VARCHAR(255) COMMENT '原始文件名',
    `file_type` VARCHAR(50) COMMENT '文件类型：pdf,doc,zip,video,audio,image,other',
    `file_size` BIGINT DEFAULT 0 COMMENT '文件大小(字节)',
    `file_path` VARCHAR(500) NOT NULL COMMENT '文件存储路径',
    `folder_id` INT DEFAULT 0 COMMENT '所属目录ID，0表示根目录',
    `visibility_type` VARCHAR(20) DEFAULT 'INHERIT' COMMENT '可见范围：INHERIT-继承目录，ALL-全体学生，CLASSES-指定班级',
    `uploader_id` INT COMMENT '上传人ID(教师)',
    `uploader_name` VARCHAR(50) COMMENT '上传人姓名(冗余)',
    `download_count` INT DEFAULT 0 COMMENT '下载次数',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY `idx_folder` (`folder_id`),
    KEY `idx_type` (`file_type`),
    KEY `idx_visibility` (`visibility_type`),
    KEY `idx_name` (`name`(100)),
    FOREIGN KEY (`folder_id`) REFERENCES `repository_folder`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资料库文件表';

-- (23) 资料库文件-班级关联表
CREATE TABLE `repository_file_class` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `file_id` INT NOT NULL COMMENT '文件ID',
    `class_id` INT NOT NULL COMMENT '班级ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_file_class` (`file_id`, `class_id`),
    KEY `idx_file` (`file_id`),
    KEY `idx_class` (`class_id`),
    FOREIGN KEY (`file_id`) REFERENCES `repository_file`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`class_id`) REFERENCES `classes`(`cid`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资料库文件班级关联表';

-- (24) 资料库文件置顶表
CREATE TABLE `repository_file_pin` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `file_id` INT NOT NULL COMMENT '文件ID',
    `student_id` INT NOT NULL COMMENT '学生ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_file_student` (`file_id`, `student_id`),
    KEY `idx_student` (`student_id`),
    FOREIGN KEY (`file_id`) REFERENCES `repository_file`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`student_id`) REFERENCES `user`(`uid`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资料库文件置顶表';

-- Seeding repository data
INSERT INTO `repository_folder` (`name`, `parent_id`, `path`, `depth`, `visibility_type`, `creator_id`, `creator_name`) VALUES 
('课程资料', 0, '/课程资料/', 1, 'ALL', 1, '张老师'),
('实验资料', 0, '/实验资料/', 1, 'ALL', 1, '张老师'),
('参考书籍', 0, '/参考书籍/', 1, 'CLASSES', 1, '张老师'),
('Java课程', 1, '/课程资料/Java课程/', 2, 'ALL', 1, '张老师'),
('数据库课程', 1, '/课程资料/数据库课程/', 2, 'ALL', 2, '李老师'),
('网络课程', 1, '/课程资料/网络课程/', 2, 'CLASSES', 3, '王老师'),
('Java实验', 2, '/实验资料/Java实验/', 2, 'ALL', 1, '张老师'),
('数据库实验', 2, '/实验资料/数据库实验/', 2, 'ALL', 2, '李老师');

INSERT INTO `repository_folder_class` (`folder_id`, `class_id`) VALUES 
(3, 1), (3, 2), (3, 3),
(6, 5), (6, 9);

INSERT INTO `repository_file` (`name`, `original_name`, `file_type`, `file_size`, `file_path`, `folder_id`, `visibility_type`, `uploader_id`, `uploader_name`) VALUES 
('Java第一章讲义.pdf', 'chapter1.pdf', 'pdf', 1024000, '/uploads/chapter1.pdf', 4, 'INHERIT', 1, '张老师'),
('Java第二章讲义.pdf', 'chapter2.pdf', 'pdf', 1124000, '/uploads/chapter2.pdf', 4, 'INHERIT', 1, '张老师'),
('Java第三章讲义.pdf', 'chapter3.pdf', 'pdf', 986000, '/uploads/chapter3.pdf', 4, 'INHERIT', 1, '张老师'),
('数据库第一章讲义.pdf', 'db1.pdf', 'pdf', 876000, '/uploads/db1.pdf', 5, 'INHERIT', 2, '李老师'),
('数据库第二章讲义.pdf', 'db2.pdf', 'pdf', 923000, '/uploads/db2.pdf', 5, 'INHERIT', 2, '李老师'),
('Java实验1指导书.zip', 'exp1.zip', 'zip', 5120000, '/uploads/exp1.zip', 7, 'INHERIT', 1, '张老师'),
('Java实验2指导书.zip', 'exp2.zip', 'zip', 4890000, '/uploads/exp2.zip', 7, 'INHERIT', 1, '张老师'),
('数据库实验1指导书.zip', 'dbexp1.zip', 'zip', 3450000, '/uploads/dbexp1.zip', 8, 'INHERIT', 2, '李老师'),
('网络协议分析教程.pdf', 'net1.pdf', 'pdf', 2340000, '/uploads/net1.pdf', 6, 'CLASSES', 3, '王老师'),
('Java核心技术卷I.pdf', 'java_core.pdf', 'pdf', 15670000, '/uploads/java_core.pdf', 3, 'CLASSES', 1, '张老师');

INSERT INTO `repository_file_class` (`file_id`, `class_id`) VALUES 
(9, 5), (9, 9),
(10, 1), (10, 2), (10, 3);

INSERT INTO `repository_file_pin` (`file_id`, `student_id`) VALUES 
(1, 1), (2, 1), (6, 1),
(1, 2), (4, 2);

-- (25) 班级相册表
CREATE TABLE `class_album` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `title` VARCHAR(200) NOT NULL COMMENT '相册标题',
    `description` TEXT COMMENT '相册描述',
    `activity_date` DATE COMMENT '活动日期',
    `class_id` INT NOT NULL COMMENT '所属班级ID',
    `class_name` VARCHAR(100) COMMENT '班级名称(冗余)',
    `cover_image` VARCHAR(500) COMMENT '封面图片路径',
    `creator_id` INT COMMENT '创建人ID',
    `creator_name` VARCHAR(50) COMMENT '创建人姓名(冗余)',
    `creator_type` VARCHAR(20) DEFAULT 'teacher' COMMENT '创建人类型：teacher/student',
    `like_count` INT DEFAULT 0 COMMENT '点赞数',
    `view_count` INT DEFAULT 0 COMMENT '浏览数',
    `comment_count` INT DEFAULT 0 COMMENT '评论数',
    `is_featured` TINYINT DEFAULT 0 COMMENT '是否精选：0-否，1-是',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY `idx_class` (`class_id`),
    KEY `idx_date` (`activity_date`),
    KEY `idx_featured` (`is_featured`),
    FOREIGN KEY (`class_id`) REFERENCES `classes`(`cid`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班级相册表';

-- (26) 相册图片表
CREATE TABLE `class_album_image` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `album_id` INT NOT NULL COMMENT '相册ID',
    `image_path` VARCHAR(500) NOT NULL COMMENT '图片存储路径',
    `image_name` VARCHAR(255) COMMENT '图片原始名称',
    `image_size` BIGINT DEFAULT 0 COMMENT '图片大小(字节)',
    `sort_order` INT DEFAULT 0 COMMENT '排序序号',
    `is_cover` TINYINT DEFAULT 0 COMMENT '是否封面：0-否，1-是',
    `uploader_id` INT COMMENT '上传人ID',
    `uploader_name` VARCHAR(50) COMMENT '上传人姓名(冗余)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY `idx_album` (`album_id`),
    FOREIGN KEY (`album_id`) REFERENCES `class_album`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='相册图片表';

-- (27) 相册评论表
CREATE TABLE `class_album_comment` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `album_id` INT NOT NULL COMMENT '相册ID',
    `user_id` INT NOT NULL COMMENT '评论人ID(学生或教师)',
    `user_name` VARCHAR(50) COMMENT '评论人姓名(冗余)',
    `user_type` VARCHAR(20) DEFAULT 'student' COMMENT '评论人类型：student/teacher',
    `content` TEXT NOT NULL COMMENT '评论内容',
    `image_path` VARCHAR(500) COMMENT '评论附图路径(可选)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY `idx_album` (`album_id`),
    KEY `idx_user` (`user_id`),
    FOREIGN KEY (`album_id`) REFERENCES `class_album`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='相册评论表';

-- (28) 相册点赞表（幂等）
CREATE TABLE `class_album_like` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `album_id` INT NOT NULL COMMENT '相册ID',
    `user_id` INT NOT NULL COMMENT '点赞人ID',
    `user_type` VARCHAR(20) DEFAULT 'student' COMMENT '点赞人类型：student/teacher',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_album_user` (`album_id`, `user_id`, `user_type`),
    KEY `idx_album` (`album_id`),
    KEY `idx_user` (`user_id`),
    FOREIGN KEY (`album_id`) REFERENCES `class_album`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='相册点赞表';

-- (29) 已保存的报表配置表
CREATE TABLE `saved_report` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `report_name` VARCHAR(200) NOT NULL COMMENT '报表名称',
    `teacher_id` INT NOT NULL COMMENT '教师ID',
    `filter_json` TEXT COMMENT '筛选配置JSON',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY `idx_teacher` (`teacher_id`),
    FOREIGN KEY (`teacher_id`) REFERENCES `teacher`(`tid`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='已保存的报表配置表';

-- (30) 答疑预约时段表
CREATE TABLE `consultation_slot` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `teacher_id` INT NOT NULL COMMENT '教师ID',
    `teacher_name` VARCHAR(50) COMMENT '教师姓名(冗余)',
    `slot_date` DATE NOT NULL COMMENT '预约日期',
    `start_time` TIME NOT NULL COMMENT '开始时间',
    `end_time` TIME NOT NULL COMMENT '结束时间',
    `location` VARCHAR(255) COMMENT '地点或线上方式(如腾讯会议号)',
    `location_type` VARCHAR(20) DEFAULT 'offline' COMMENT '地点类型：offline-线下，online-线上',
    `capacity` INT NOT NULL DEFAULT 1 COMMENT '可预约人数上限',
    `booked_count` INT NOT NULL DEFAULT 0 COMMENT '已预约人数',
    `version` INT NOT NULL DEFAULT 0 COMMENT '版本号，用于乐观锁',
    `status` VARCHAR(20) DEFAULT 'available' COMMENT '状态：available-可预约，full-已满，closed-已关闭，expired-已过期',
    `remark` VARCHAR(500) COMMENT '备注说明',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY `idx_teacher` (`teacher_id`),
    KEY `idx_date` (`slot_date`),
    KEY `idx_status` (`status`),
    KEY `idx_teacher_date` (`teacher_id`, `slot_date`),
    FOREIGN KEY (`teacher_id`) REFERENCES `teacher`(`tid`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='答疑预约时段表';

-- (31) 答疑预约记录表
CREATE TABLE `consultation_booking` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `slot_id` INT NOT NULL COMMENT '时段ID',
    `student_id` INT NOT NULL COMMENT '学生ID',
    `student_name` VARCHAR(50) COMMENT '学生姓名(冗余)',
    `student_no` VARCHAR(50) COMMENT '学号(冗余)',
    `question` TEXT COMMENT '想咨询的问题',
    `status` VARCHAR(20) DEFAULT 'booked' COMMENT '状态：booked-已预约，cancelled-已取消，completed-已完成，no_show-爽约',
    `cancel_time` DATETIME COMMENT '取消时间',
    `cancel_reason` VARCHAR(255) COMMENT '取消原因',
    `complete_time` DATETIME COMMENT '完成时间',
    `teacher_remark` VARCHAR(500) COMMENT '教师备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_slot_student` (`slot_id`, `student_id`),
    KEY `idx_student` (`student_id`),
    KEY `idx_slot` (`slot_id`),
    KEY `idx_status` (`status`),
    KEY `idx_student_status` (`student_id`, `status`),
    FOREIGN KEY (`slot_id`) REFERENCES `consultation_slot`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`student_id`) REFERENCES `user`(`uid`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='答疑预约记录表';

-- Seeding consultation data
INSERT INTO `consultation_slot` (`teacher_id`, `teacher_name`, `slot_date`, `start_time`, `end_time`, `location`, `location_type`, `capacity`, `booked_count`, `status`, `remark`) VALUES 
(1, '张老师', '2026-06-15', '14:00:00', '16:00:00', '教学楼A-301', 'offline', 5, 2, 'available', 'Java程序设计答疑'),
(1, '张老师', '2026-06-18', '09:00:00', '11:00:00', '腾讯会议：123-456-789', 'online', 3, 3, 'full', '数据结构答疑'),
(1, '张老师', '2026-06-20', '14:00:00', '17:00:00', '教学楼A-301', 'offline', 8, 1, 'available', '期末复习答疑'),
(2, '李老师', '2026-06-16', '10:00:00', '12:00:00', '实验楼B-205', 'offline', 4, 0, 'available', '数据库原理答疑'),
(2, '李老师', '2026-06-19', '15:00:00', '17:00:00', '腾讯会议：987-654-321', 'online', 6, 4, 'available', 'SQL优化专题'),
(3, '王老师', '2026-06-17', '13:30:00', '15:30:00', '教学楼C-101', 'offline', 5, 3, 'available', '计算机网络答疑'),
(4, '赵老师', '2026-06-21', '09:00:00', '12:00:00', '实验楼D-402', 'offline', 4, 2, 'available', '人工智能答疑'),
(4, '赵老师', '2026-06-25', '14:00:00', '16:00:00', '腾讯会议：111-222-333', 'online', 5, 0, 'available', '机器学习专题'),
(5, '孙老师', '2026-06-22', '10:00:00', '12:00:00', '教学楼A-201', 'offline', 6, 5, 'available', '软件工程答疑'),
(6, '周老师', '2026-06-23', '14:00:00', '16:00:00', '实验楼C-301', 'offline', 3, 1, 'available', '网络安全答疑'),
(7, '吴老师', '2026-06-24', '09:00:00', '11:00:00', '腾讯会议：444-555-666', 'online', 4, 0, 'available', '移动开发答疑'),
(1, '张老师', '2026-06-10', '14:00:00', '16:00:00', '教学楼A-301', 'offline', 5, 3, 'expired', '已过期的答疑时段');

INSERT INTO `consultation_booking` (`slot_id`, `student_id`, `student_name`, `student_no`, `question`, `status`, `create_time`) VALUES 
(1, 1, '小明', 'S2021001', '老师，Java多线程那块我不太懂', 'booked', '2026-06-12 10:00:00'),
(1, 2, '小红', 'S2021002', '想请教一下面向对象设计原则', 'booked', '2026-06-12 11:30:00'),
(2, 3, '小王', 'S2021003', '红黑树的删除操作', 'booked', '2026-06-11 09:00:00'),
(2, 4, '小李', 'S2021004', '图论最短路径算法', 'booked', '2026-06-11 10:00:00'),
(2, 5, '小赵', 'S2021005', '动态规划问题', 'booked', '2026-06-11 14:00:00'),
(3, 6, '小孙', 'S2021006', '期末复习重点', 'booked', '2026-06-12 15:00:00'),
(5, 7, '小周', 'S2021007', 'SQL注入防护', 'booked', '2026-06-12 09:00:00'),
(5, 8, '小吴', 'S2021008', '索引优化', 'booked', '2026-06-12 10:30:00'),
(5, 9, '小郑', 'S2021009', '事务隔离级别', 'booked', '2026-06-12 11:00:00'),
(5, 10, '小冯', 'S2021010', '存储过程', 'booked', '2026-06-12 14:00:00'),
(6, 11, '小陈', 'S2021011', 'TCP三次握手', 'booked', '2026-06-12 16:00:00'),
(6, 12, '小褚', 'S2021012', 'HTTP协议', 'booked', '2026-06-12 17:00:00'),
(6, 13, '小卫', 'S2021013', 'DNS解析过程', 'booked', '2026-06-13 09:00:00'),
(7, 14, '小蒋', 'S2021014', '神经网络反向传播', 'booked', '2026-06-13 10:00:00'),
(7, 15, '小沈', 'S2021015', '梯度下降优化', 'booked', '2026-06-13 11:00:00'),
(9, 16, '小韩', 'S2021016', '敏捷开发流程', 'booked', '2026-06-13 14:00:00'),
(9, 17, '小杨', 'S2021017', '需求分析方法', 'booked', '2026-06-13 15:00:00'),
(9, 18, '小朱', 'S2021018', 'UML建模', 'booked', '2026-06-13 16:00:00'),
(9, 19, '小秦', 'S2021019', '软件测试方法', 'booked', '2026-06-13 17:00:00'),
(9, 20, '小尤', 'S2021020', '项目管理', 'booked', '2026-06-14 09:00:00'),
(10, 21, '小许', 'S2021021', 'XSS攻击防护', 'booked', '2026-06-14 10:00:00'),
(12, 1, '小明', 'S2021001', '已过期的预约', 'completed', '2026-06-08 10:00:00'),
(12, 2, '小红', 'S2021002', '已过期的预约2', 'no_show', '2026-06-08 11:00:00'),
(12, 3, '小王', 'S2021003', '已过期的预约3', 'cancelled', '2026-06-08 09:00:00');

-- (32) 奖惩记录表
CREATE TABLE `discipline_record` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `student_id` INT NOT NULL COMMENT '学生ID(user.uid)',
    `student_name` VARCHAR(50) COMMENT '学生姓名(冗余)',
    `student_no` VARCHAR(50) COMMENT '学号(冗余)',
    `class_id` INT COMMENT '班级ID(classes.cid)',
    `class_name` VARCHAR(100) COMMENT '班级名称(冗余)',
    `category` VARCHAR(20) NOT NULL COMMENT '大类：reward-奖励，punishment-处分',
    `record_type` VARCHAR(50) NOT NULL COMMENT '类型：表彰/奖学金/荣誉称号/警告/记过/严重警告/留校察看',
    `reason` TEXT NOT NULL COMMENT '事由说明',
    `occur_date` DATE NOT NULL COMMENT '发生日期',
    `severity` VARCHAR(20) DEFAULT 'medium' COMMENT '影响程度：low-轻微，medium-中等，high-严重',
    `evidence_file` VARCHAR(500) DEFAULT NULL COMMENT '证明文件路径(可选)',
    `evidence_file_name` VARCHAR(255) DEFAULT NULL COMMENT '证明文件原始名称(可选)',
    `status` VARCHAR(20) DEFAULT 'active' COMMENT '状态：active-生效，revoked-已撤销',
    `revoke_reason` TEXT DEFAULT NULL COMMENT '撤销原因',
    `revoke_time` DATETIME DEFAULT NULL COMMENT '撤销时间',
    `revoke_teacher_id` INT DEFAULT NULL COMMENT '撤销操作教师ID',
    `revoke_teacher_name` VARCHAR(50) DEFAULT NULL COMMENT '撤销操作教师姓名(冗余)',
    `creator_id` INT NOT NULL COMMENT '登记教师ID(teacher.tid)',
    `creator_name` VARCHAR(50) COMMENT '登记教师姓名(冗余)',
    `batch_id` VARCHAR(64) DEFAULT NULL COMMENT '批量登记批次ID，同一批次共享',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY `idx_student` (`student_id`),
    KEY `idx_class` (`class_id`),
    KEY `idx_category` (`category`),
    KEY `idx_record_type` (`record_type`),
    KEY `idx_status` (`status`),
    KEY `idx_occur_date` (`occur_date`),
    KEY `idx_batch_id` (`batch_id`),
    KEY `idx_student_status` (`student_id`, `status`),
    FOREIGN KEY (`student_id`) REFERENCES `user`(`uid`) ON DELETE CASCADE,
    FOREIGN KEY (`class_id`) REFERENCES `classes`(`cid`) ON DELETE SET NULL,
    FOREIGN KEY (`creator_id`) REFERENCES `teacher`(`tid`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='奖惩记录表';

-- Seeding discipline records
INSERT INTO `discipline_record` (`student_id`, `student_name`, `student_no`, `class_id`, `class_name`, `category`, `record_type`, `reason`, `occur_date`, `severity`, `status`, `creator_id`, `creator_name`, `batch_id`) VALUES
(1, '小明', 'S2021001', 1, '计算机2101', 'reward', '表彰', '在省级程序设计竞赛中获得一等奖，为学校和班级争光', '2026-03-15', 'high', 'active', 1, '张老师', NULL),
(1, '小明', 'S2021001', 1, '计算机2101', 'reward', '奖学金', '2025-2026学年一等奖学金，综合成绩排名专业前5%', '2026-04-01', 'medium', 'active', 1, '张老师', NULL),
(3, '小王', 'S2021003', 2, '计算机2102', 'reward', '奖学金', '2025-2026学年特等奖学金，综合成绩排名专业第1', '2026-04-01', 'high', 'active', 2, '李老师', NULL),
(6, '小孙', 'S2021006', 3, '软件2101', 'reward', '荣誉称号', '被评为2025-2026学年三好学生', '2026-05-10', 'medium', 'active', 5, '孙老师', NULL),
(14, '小蒋', 'S2021014', 7, 'AI2101', 'reward', '表彰', '在全国人工智能创新大赛中获得银奖', '2026-03-20', 'high', 'active', 4, '赵老师', NULL),
(2, '小红', 'S2021002', 1, '计算机2101', 'punishment', '警告', '连续三次未按时提交作业', '2026-02-28', 'low', 'active', 1, '张老师', NULL),
(4, '小李', 'S2021004', 2, '计算机2102', 'punishment', '记过', '考试作弊行为，违反学术诚信', '2026-05-05', 'high', 'active', 2, '李老师', NULL),
(7, '小周', 'S2021007', 4, '软件2102', 'punishment', '严重警告', '在实验室内违反安全操作规程', '2026-04-18', 'medium', 'revoked', 5, '孙老师', NULL),
(16, '小韩', 'S2021016', 8, '物联网2101', 'punishment', '警告', '多次旷课，累计超过规定次数', '2026-03-10', 'low', 'active', 8, '郑老师', NULL);

-- Update revoke info for record id 8
UPDATE `discipline_record` SET `revoke_reason`='经核实，该学生当时已获得安全操作授权，属于误判', `revoke_time`='2026-05-20 14:30:00', `revoke_teacher_id`=5, `revoke_teacher_name`='孙老师' WHERE `id`=8;

-- (33) 书目表
CREATE TABLE `book` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `title` VARCHAR(255) NOT NULL COMMENT '书名',
    `author` VARCHAR(200) NOT NULL COMMENT '作者',
    `isbn` VARCHAR(50) COMMENT 'ISBN编号',
    `category` VARCHAR(100) COMMENT '分类',
    `total_count` INT NOT NULL DEFAULT 0 COMMENT '馆藏总数',
    `available_count` INT NOT NULL DEFAULT 0 COMMENT '可借数量',
    `cover_image` VARCHAR(500) COMMENT '封面图路径',
    `description` TEXT COMMENT '书籍描述',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    `borrow_count` INT NOT NULL DEFAULT 0 COMMENT '累计借阅次数(热度)',
    `creator_id` INT COMMENT '创建人ID(教师)',
    `creator_name` VARCHAR(50) COMMENT '创建人姓名(冗余)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY `idx_category` (`category`),
    KEY `idx_title` (`title`(100)),
    KEY `idx_isbn` (`isbn`),
    KEY `idx_available` (`available_count`),
    KEY `idx_borrow_count` (`borrow_count`),
    FOREIGN KEY (`creator_id`) REFERENCES `teacher`(`tid`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='书目表';

-- (34) 借阅记录表
CREATE TABLE `borrow_record` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `book_id` INT NOT NULL COMMENT '书目ID',
    `book_title` VARCHAR(255) COMMENT '书名(冗余)',
    `student_id` INT NOT NULL COMMENT '学生ID',
    `student_name` VARCHAR(50) COMMENT '学生姓名(冗余)',
    `student_no` VARCHAR(50) COMMENT '学号(冗余)',
    `borrow_time` DATETIME NOT NULL COMMENT '借阅时间',
    `due_date` DATE NOT NULL COMMENT '应还日期',
    `return_time` DATETIME COMMENT '归还时间',
    `status` VARCHAR(20) DEFAULT 'borrowing' COMMENT '状态：borrowing-在借，returned-已归还，overdue-逾期',
    `is_overdue` TINYINT DEFAULT 0 COMMENT '是否逾期：0-否，1-是',
    `renew_count` INT DEFAULT 0 COMMENT '续借次数',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_book_student_borrowing` (`book_id`, `student_id`, `status`),
    KEY `idx_student` (`student_id`),
    KEY `idx_book` (`book_id`),
    KEY `idx_status` (`status`),
    KEY `idx_due_date` (`due_date`),
    KEY `idx_student_status` (`student_id`, `status`),
    KEY `idx_overdue` (`is_overdue`),
    FOREIGN KEY (`book_id`) REFERENCES `book`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`student_id`) REFERENCES `user`(`uid`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='借阅记录表';

-- Seeding book data
INSERT INTO `book` (`title`, `author`, `isbn`, `category`, `total_count`, `available_count`, `cover_image`, `description`, `borrow_count`, `creator_id`, `creator_name`) VALUES
('Java核心技术卷I', 'Cay S. Horstmann', '978-7-111-66434-8', '计算机/编程', 5, 3, '/uploads/book_java_core.jpg', 'Java领域经典参考书，全面深入介绍Java语言特性与核心API。', 12, 1, '张老师'),
('算法导论', 'Thomas H. Cormen', '978-7-111-40701-0', '计算机/算法', 3, 0, '/uploads/book_algorithm.jpg', '算法领域权威教材，全面涵盖算法设计与分析。', 8, 1, '张老师'),
('深入理解计算机系统', 'Randal E. Bryant', '978-7-111-54493-7', '计算机/系统', 4, 2, '/uploads/book_csapp.jpg', '从程序员视角理解计算机系统的经典著作。', 15, 2, '李老师'),
('数据库系统概念', 'Abraham Silberschatz', '978-7-111-68213-4', '计算机/数据库', 4, 4, '/uploads/book_db_concept.jpg', '数据库领域经典教材，介绍数据库设计与实现原理。', 6, 2, '李老师'),
('计算机网络：自顶向下方法', 'James F. Kurose', '978-7-111-59935-7', '计算机/网络', 5, 5, '/uploads/book_network.jpg', '从应用层开始逐层深入讲解计算机网络原理。', 9, 3, '王老师'),
('设计模式：可复用面向对象软件的基础', 'Erich Gamma', '978-7-111-21126-5', '计算机/软件', 3, 2, '/uploads/book_design_pattern.jpg', '23种经典设计模式的权威著作。', 11, 1, '张老师'),
('人工智能：一种现代方法', 'Stuart Russell', '978-7-111-56823-8', '计算机/AI', 4, 1, '/uploads/book_ai.jpg', '人工智能领域经典教材，全面介绍AI理论与应用。', 7, 4, '赵老师'),
('机器学习', '周志华', '978-7-302-42328-7', '计算机/AI', 5, 4, '/uploads/book_ml_zhou.jpg', '国内机器学习经典入门教材，俗称"西瓜书"。', 10, 4, '赵老师'),
('数据结构与算法分析', 'Mark Allen Weiss', '978-7-111-49464-6', '计算机/算法', 3, 3, '/uploads/book_data_structure.jpg', '数据结构与算法分析的经典教材。', 5, 1, '张老师'),
('操作系统概念', 'Abraham Silberschatz', '978-7-111-60545-8', '计算机/系统', 4, 4, '/uploads/book_os_concept.jpg', '操作系统领域经典教材，深入讲解操作系统原理。', 4, 11, '褚老师'),
('软件工程：实践者的研究方法', 'Roger S. Pressman', '978-7-111-55501-8', '计算机/软件', 3, 3, '/uploads/book_se.jpg', '软件工程领域权威教材，介绍软件开发全过程。', 3, 5, '孙老师'),
('编译原理', 'Alfred V. Aho', '978-7-111-25121-7', '计算机/系统', 2, 2, '/uploads/book_compiler.jpg', '编译原理领域经典教材，俗称"龙书"。', 2, 10, '陈老师'),
('线性代数及其应用', 'David C. Lay', '978-7-111-60257-6', '数学', 4, 4, '/uploads/book_linear_algebra.jpg', '线性代数经典教材，强调应用与几何直观。', 2, 1, '张老师'),
('离散数学及其应用', 'Kenneth H. Rosen', '978-7-111-63728-6', '数学', 5, 5, '/uploads/book_discrete_math.jpg', '离散数学经典教材，涵盖计算机科学所需的数学基础。', 3, 1, '张老师'),
('代码整洁之道', 'Robert C. Martin', '978-7-115-21748-6', '计算机/编程', 3, 1, '/uploads/book_clean_code.jpg', '编写高质量代码的实践指南。', 8, 21, '何老师'),
('重构：改善既有代码的设计', 'Martin Fowler', '978-7-115-36939-2', '计算机/软件', 3, 2, '/uploads/book_refactoring.jpg', '代码重构领域的经典著作。', 6, 22, '吕老师'),
('人月神话', 'Frederick P. Brooks', '978-7-115-36257-5', '计算机/管理', 2, 2, '/uploads/book_mythical_man_month.jpg', '软件工程领域经典随笔集。', 4, 21, '何老师'),
('浪潮之巅', '吴军', '978-7-115-46301-3', '科技/历史', 5, 5, '/uploads/book_wave.jpg', '讲述科技产业发展历程的畅销书。', 1, 1, '张老师'),
('数学之美', '吴军', '978-7-115-37355-9', '科普', 4, 4, '/uploads/book_math_beauty.jpg', '用通俗的语言介绍数学在信息技术中的应用。', 2, 1, '张老师'),
('黑客与画家', 'Paul Graham', '978-7-115-25799-4', '计算机/文化', 3, 3, '/uploads/book_hacker_painter.jpg', '硅谷创业教父Paul Graham的文集。', 3, 24, '张三老师');

-- Seeding borrow records
INSERT INTO `borrow_record` (`book_id`, `book_title`, `student_id`, `student_name`, `student_no`, `borrow_time`, `due_date`, `return_time`, `status`, `is_overdue`) VALUES
(1, 'Java核心技术卷I', 1, '小明', 'S2021001', '2026-06-01 10:00:00', '2026-06-15', '2026-06-12 15:30:00', 'returned', 0),
(1, 'Java核心技术卷I', 2, '小红', 'S2021002', '2026-06-05 14:00:00', '2026-06-19', NULL, 'borrowing', 0),
(1, 'Java核心技术卷I', 3, '小王', 'S2021003', '2026-06-08 09:00:00', '2026-06-22', NULL, 'borrowing', 0),
(2, '算法导论', 1, '小明', 'S2021001', '2026-06-03 11:00:00', '2026-06-17', NULL, 'borrowing', 0),
(2, '算法导论', 4, '小李', 'S2021004', '2026-06-04 13:00:00', '2026-06-18', '2026-06-10 10:00:00', 'returned', 0),
(2, '算法导论', 5, '小赵', 'S2021005', '2026-06-06 10:00:00', '2026-06-20', NULL, 'borrowing', 0),
(3, '深入理解计算机系统', 1, '小明', 'S2021001', '2026-05-20 10:00:00', '2026-06-03', '2026-06-02 14:00:00', 'returned', 0),
(3, '深入理解计算机系统', 2, '小红', 'S2021002', '2026-06-02 09:00:00', '2026-06-16', NULL, 'borrowing', 0),
(7, '人工智能：一种现代方法', 14, '小蒋', 'S2021014', '2026-06-01 14:00:00', '2026-06-15', NULL, 'borrowing', 0),
(7, '人工智能：一种现代方法', 7, '小周', 'S2021007', '2026-06-03 10:00:00', '2026-06-17', NULL, 'borrowing', 0),
(7, '人工智能：一种现代方法', 15, '小沈', 'S2021015', '2026-06-05 11:00:00', '2026-06-19', NULL, 'borrowing', 0),
(15, '代码整洁之道', 1, '小明', 'S2021001', '2026-05-15 10:00:00', '2026-05-29', NULL, 'overdue', 1),
(16, '重构：改善既有代码的设计', 2, '小红', 'S2021002', '2026-05-20 14:00:00', '2026-06-03', NULL, 'overdue', 1);

-- (35) 教师任课关联表
CREATE TABLE `teacher_class` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `teacher_id` INT NOT NULL COMMENT '教师ID(teacher.tid)',
    `teacher_name` VARCHAR(50) COMMENT '教师姓名(冗余)',
    `class_id` INT NOT NULL COMMENT '班级ID(classes.cid)',
    `class_name` VARCHAR(100) COMMENT '班级名称(冗余)',
    `course_name` VARCHAR(200) COMMENT '任课课程名称',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_teacher_class` (`teacher_id`, `class_id`, `course_name`),
    KEY `idx_teacher` (`teacher_id`),
    KEY `idx_class` (`class_id`),
    FOREIGN KEY (`teacher_id`) REFERENCES `teacher`(`tid`) ON DELETE CASCADE,
    FOREIGN KEY (`class_id`) REFERENCES `classes`(`cid`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师任课关联表';

-- Seeding teacher_class data (30 teachers x relevant classes)
INSERT INTO `teacher_class` (`teacher_id`, `teacher_name`, `class_id`, `class_name`, `course_name`) VALUES
-- 计算机2101班(1) / 计算机2102班(2) 的任课老师
(1, '张老师', 1, '计算机2101', 'Java程序设计'),
(1, '张老师', 2, '计算机2102', 'Java程序设计'),
(1, '张老师', 1, '计算机2101', '数据结构'),
(2, '李老师', 1, '计算机2101', '数据库原理'),
(2, '李老师', 2, '计算机2102', '数据库原理'),
(3, '王老师', 1, '计算机2101', '计算机网络'),
(3, '王老师', 2, '计算机2102', '计算机网络'),
(11, '褚老师', 1, '计算机2101', '操作系统'),
(11, '褚老师', 2, '计算机2102', '操作系统'),
(10, '陈老师', 1, '计算机2101', '编译原理'),
(21, '何老师', 1, '计算机2101', '软件工程'),
(21, '何老师', 2, '计算机2102', '软件工程'),
(28, '华老师', 1, '计算机2101', 'Web开发技术'),
(28, '华老师', 2, '计算机2102', 'Web开发技术'),

-- 软件2101班(3) / 软件2102班(4) 的任课老师
(5, '孙老师', 3, '软件2101', '软件工程'),
(5, '孙老师', 4, '软件2102', '软件工程'),
(22, '吕老师', 3, '软件2101', '敏捷开发'),
(22, '吕老师', 4, '软件2102', '敏捷开发'),
(23, '施老师', 3, '软件2101', 'DevOps实践'),
(23, '施老师', 4, '软件2102', 'DevOps实践'),
(24, '张三老师', 3, '软件2101', '全栈开发'),
(24, '张三老师', 4, '软件2102', '全栈开发'),
(25, '孔老师', 3, '软件2101', '系统架构设计'),
(26, '曹老师', 3, '软件2101', '微服务架构'),
(26, '曹老师', 4, '软件2102', '微服务架构'),

-- 网络2101班(5) / 安防2101班(9) 的任课老师
(6, '周老师', 5, '网络2101', '网络安全'),
(6, '周老师', 9, '安防2101', '网络安全'),
(3, '王老师', 5, '网络2101', '计算机网络'),
(12, '卫老师', 5, '网络2101', '嵌入式系统'),
(12, '卫老师', 9, '安防2101', '嵌入式系统'),
(8, '郑老师', 5, '网络2101', '云计算架构'),
(8, '郑老师', 9, '安防2101', '云计算架构'),

-- 大数据2101班(6) 的任课老师
(27, '严老师', 6, '大数据2101', '大数据分析'),
(14, '沈老师', 6, '大数据2101', '数据挖掘'),
(13, '蒋老师', 6, '大数据2101', '分布式系统'),
(18, '秦老师', 6, '大数据2101', '信息检索'),

-- AI2101班(7) / 物联网2101班(8) 的任课老师
(4, '赵老师', 7, 'AI2101', '人工智能导论'),
(4, '赵老师', 8, '物联网2101', '人工智能导论'),
(15, '韩老师', 7, 'AI2101', '机器学习'),
(16, '杨老师', 7, 'AI2101', '计算机视觉'),
(17, '朱老师', 7, 'AI2101', '自然语言处理'),
(17, '朱老师', 8, '物联网2101', '自然语言处理'),
(7, '吴老师', 8, '物联网2101', '移动应用开发'),
(12, '卫老师', 8, '物联网2101', '嵌入式系统'),

-- 通信2101班(10) / 自动化2101班(11) / 电子2101班(12) 的任课老师
(3, '王老师', 10, '通信2101', '计算机网络'),
(19, '尤老师', 10, '通信2101', '人机交互设计'),
(19, '尤老师', 11, '自动化2101', '人机交互设计'),
(13, '蒋老师', 11, '自动化2101', '分布式系统'),
(9, '冯老师', 12, '电子2101', '计算机图形学'),
(20, '许老师', 10, '通信2101', '软件测试'),
(20, '许老师', 11, '自动化2101', '软件测试'),
(20, '许老师', 12, '电子2101', '软件测试'),

-- 机械2101班(13) / 土木2101班(14) / 建筑2101班(15) 的任课老师
(25, '孔老师', 13, '机械2101', '系统架构设计'),
(29, '金老师', 15, '建筑2101', 'UI/UX设计'),
(30, '魏老师', 15, '建筑2101', '交互设计'),
(30, '魏老师', 13, '机械2101', '交互设计'),

-- 其余班级也配置一些老师
(1, '张老师', 16, '管理2101', 'Java程序设计'),
(2, '李老师', 17, '会计2101', '数据库原理'),
(28, '华老师', 18, '金融2101', 'Web开发技术'),
(4, '赵老师', 19, '法律2101', '人工智能导论'),
(27, '严老师', 20, '外语2101', '大数据分析'),
(1, '张老师', 21, '数学2101', 'Java程序设计'),
(9, '冯老师', 22, '物理2101', '计算机图形学'),
(15, '韩老师', 23, '化学2101', '机器学习'),
(16, '杨老师', 24, '生物2101', '计算机视觉'),
(29, '金老师', 25, '艺术2101', 'UI/UX设计'),
(30, '魏老师', 25, '艺术2101', '交互设计'),
(21, '何老师', 26, '体育2101', '软件工程'),
(29, '金老师', 27, '音乐2101', 'UI/UX设计'),
(30, '魏老师', 28, '舞蹈2101', '交互设计'),
(19, '尤老师', 29, '表演2101', '人机交互设计'),
(18, '秦老师', 30, '新闻2101', '信息检索');
