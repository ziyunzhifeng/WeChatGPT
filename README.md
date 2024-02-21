                                微信机器人智能助手
## 简述
    本项目是一个Xposed模块，实现一个微信聊天里能够引用大模型进行聊天消息的自动回复，实现一个工作助手，智能客服相关功能。
    这个工程不能独立运行，需要依赖具有Java Hook 核心的框架支持。自己制作了一个名字叫“玩应用”的双开工具也可以支持加载这个模块。

## 效果
<video src="https://www.bilibili.com/video/BV1eC4y1k7P4/?vd_source=34b822263a923ff99088638112b51c0a" width="宽度" height="高度" controls="controls"></video>

## 使用方法 
1.去讯飞官网上注册申请免费的3.0API 
2.将Api Key相关信息添加到BigModelNew的代码里 
````
public static final String appid = "";
public static final String apiSecret = "";
public static final String apiKey = "";
```` 
3.将工程编译输出Apk，安装到手机上，在《玩应用》双开工具中，制作微信双开时，选择添加模块即可，双开微信启动后，模块自动加载。 



## 支持版本： 
1.微信8.0.43版本。

## 原理
1.基于Xposed的 Java Hook 能力("玩应用基于LSPosed")，对微信的接收消息和发送消息接口进行Hook  
2.当Hook 微信接收消息时，启动异步线程，去请求讯飞星火大模型，将聊天消息及最近5条聊天消息发送给它，等待它返回接口   
3.收到大模型返回的消息后，主动调用聊天界面代码中的对象发送消息接口，将大模型内容通过聊天消息发送出去 

## 下载使用
欢迎关注公众号《子云之风》，回复"双开"两个字就可以获得双开工具的下载链接。

![公众号](files/公众号.jpg "公众号")

![微信号](files/微信号.jpg "微信号")
加微信号，入群交流更多玩法

## 免责声明：
1.本项目只用于学习研究，不要进行违法行为，违法行为与本项目无关。
