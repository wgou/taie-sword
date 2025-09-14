<template>
  <el-dialog v-model="detailDialogVisible" :title="`${screenInfo.appName}(${connectType})`" width="90%" @close="hide"
    :close-on-click-modal="false">
    <div class="screen-container">
      <!-- <div class="roll-modal" :style="{ width: `${device.screenWidth}px`, height: `${device.screenHeight}px`, transform: `scale(${ratio})`, 'transform-origin': 'left top' }">
        </div> -->
      <div class="screen"
        :style="{ width: `${device.screenWidth}px`, height: `${device.screenHeight}px`, transform: `scale(${ratio})`, 'transform-origin': 'left top' }">
        <!-- <div class="screen" :style="{ width: `${device.screenWidth}px`, height: `${device.screenHeight}px`}"> -->
        <span v-show="rollVisible" class="roll-modal" ref="trackArea" @mousedown="startTracking"
          @mousemove="onMouseMove" @mouseup="stopTracking" @mouseleave="stopTracking">
          <!-- 显示鼠标拖动轨迹 -->
          <svg class="track-svg">
            <polyline :points="trackPoints" fill="none" stroke="red" stroke-width="2" />
          </svg>
        </span>
        <template v-for="item in screenInfo.items" :key="item.uniqueId">
          <!-- <span class="label"></span> -->

          <span @click="click(item)" :item-data="JSON.stringify(item)"
            v-show="(item.text && item.text.length > 0) || item.isClickable" class="label rect"
            :class="{ 'ui-selected': item.isSelected }"
            :style="{ top: `${item.y}px`, left: `${item.x}px`, height: `${item.height}px`, width: `${item.width}px` }">{{
              item.text }}</span>
          <span v-if="item.isScrollable" :class="{ 'ui-selected': item.isSelected }" class="scrollable"
            :style="{ top: `${item.y}px`, left: `${item.x}px`, height: `${item.height}px`, width: `${item.width}px` }">
            <el-button @click="scroll(item)" class="major" type="info" size="large">滚动</el-button>
          </span>
          <span @click="click(item)" :class="{ 'ui-selected': item.isSelected }" v-else-if="item.isCheckable"
            class="checkable" :style="{ top: `${item.y}px`, left: `${item.x}px` }">{{
              item.isChecked ? "✓" : "✕"
            }}</span>
          <span :class="{ 'ui-selected': item.isSelected }" @click="input(item)"
            v-else-if="item.isEditable && item.isFocusable" class="editable"
            :style="{ top: `${item.y}px`, left: `${item.x}px`, height: `${item.height}px`, width: `${item.width}px` }">
          </span>
        </template>
      </div>

      <div class="operate" :style="{ left: `${operateLeft}` }">
        <el-row :gutter="5">
          <el-col :span="8"><el-button :type="rollVisible ? 'danger' : 'success'" @click="rollSwitch">
              {{ rollVisible ? "退出滚动" : "进入滚动" }}
            </el-button>
          </el-col>
          <el-col :span="8"><el-button type="success" @click="screenReq">刷新</el-button></el-col>
          <el-col :span="8"><el-button type="success" @click="wakeup">解锁(唤醒重连)</el-button></el-col>
        </el-row>

        <!-- <el-divider content-position="left">启动指定app</el-divider> -->

        <!-- <el-row :gutter="20">
          <el-col :span="14">
            <el-select v-model="startApp" placeholder="已安装app" clearable filterable>
              <el-option v-for="installApp in installAppList" :key="installApp.packageName" :label="`${installApp.appName}(${installApp.packageName})`" :value="installApp.packageName"></el-option>
            </el-select>
          </el-col>
          <el-col :span="6">
            <el-button type="success" @click="installAppReq">刷新</el-button>
          </el-col>

          <el-col :span="6">
            <el-button type="success" @click="startAppReq">启动</el-button>
          </el-col>
        </el-row> -->
        <el-row :gutter="20">
          <el-col :span="4">
            <div class="grid-content ep-bg-purple" />
          </el-col>
          <el-col :span="16">
            <div class="grid-content ep-bg-purple" />
          </el-col>
          <el-col :span="4">
            <div class="grid-content ep-bg-purple" />
          </el-col>
        </el-row>
      </div>
    </div>

    <div class="operate-bottom" :style="{ width: `${operateLeft}` }">
      <div class="button-container">
        <el-tooltip class="box-item" effect="dark" content="正在运行的APP" placement="top">
          <el-button type="success" @click="recents">
            <el-icon>
              <Menu />
            </el-icon>
          </el-button>
        </el-tooltip>
        <el-tooltip class="box-item" effect="dark" content="主页" placement="top">
          <el-button type="success" @click="home">
            <el-icon>
              <House />
            </el-icon>
          </el-button>
        </el-tooltip>
        <el-tooltip class="box-item" effect="dark" content="回退" placement="top">
          <el-button type="success" @click="back">
            <el-icon>
              <ArrowLeftBold />
            </el-icon>
          </el-button>
        </el-tooltip>
      </div>
    </div>
  </el-dialog>

  <el-dialog title="关联输入" v-model="inputDialogVisible" width="300px">
    <el-autocomplete clearable v-model="inputText" :fetch-suggestions="inputAsync" placeholder="请输入"
      @select="inputSelect" class="custom-autocomplete">
      <template #default="{ item }">
        <div class="autocomplete-item" @click="inputSelect(item)">
          <div class="item-text">{{ item.value }}</div>
          <div class="item-time">{{ item.label }}</div>
        </div>
      </template>
    </el-autocomplete>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="inputDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="sendInput"> 确定 </el-button>
      </div>
    </template>
  </el-dialog>

  <el-dialog :title="`滚动`" draggable width="250px" v-model="scrollDialogVisible" :close-on-click-modal="false"
    :modal="true">
    <!-- <div class="scroll-speed">
      <el-radio-group v-model="scrollSpeed" size="large">
        <el-radio-button label="慢" value="1000" />
        <el-radio-button label="正常" value="600" />
        <el-radio-button label="快" value="200" />
      </el-radio-group>
    </div> -->

    <div class="scroll-buttons">
      <el-button @click="trundle('up')" type="primary"><el-icon>
          <ArrowUpBold />
        </el-icon></el-button>
      <div class="horizontal-buttons">
        <el-button @click="trundle('left')" type="primary"><el-icon>
            <ArrowLeftBold />
          </el-icon></el-button>
        <el-button @click="trundle('right')" type="primary"><el-icon>
            <ArrowRightBold />
          </el-icon></el-button>
      </div>
      <el-button @click="trundle('down')" type="primary"><el-icon>
          <ArrowDownBold />
        </el-icon></el-button>
    </div>
  </el-dialog>
</template>

<script lang="ts">
import { defineComponent, reactive, toRefs, ref, onMounted, defineEmits } from "vue";
import { encode, decode, encodeWsMessage, decodeWsMessage, MessageType, App, NotifyMessage } from "@/utils/message";
import WebRTCClient from "@/utils/webrtc-client";
import { WebSocketClient, ROOM_EVENT_CLIENT_JOINED, ROOM_EVENT_CLIENT_LEFT, ROOM_EVENT_CLIENT_ERROR, ROOM_EVENT_ROOM_MEMBER_COUNT } from "@/utils/websocket-client";
import { ElNotification, ElMessageBox, ElMessage } from "element-plus";
import baseService from "@/service/baseService";

import { ElLoading } from "element-plus";
import { ScreenInfo } from "@/utils/message";
import { InstallAppResp } from "@/utils/message";

export default defineComponent({
  props: {},
  setup(propers, { emit }) {
    const detailDialogVisible = ref(false);
    const scrollDialogVisible = ref(false);
    const inputDialogVisible = ref(false);
    const installAppList = ref<App[]>([]);
    const rollVisible = ref(false);
    const operateLeft = ref("600px");
    const closed = ref(true);
    const scrollSpeed = ref("正常");
    const inputText = ref("");
    const ratio = ref(1);
    const deviceId = ref("");
    const connectType = ref("转发");
    const scrollItem = ref({
      height: 0,
      width: 0,
      x: 0,
      y: 0
    });
    const inputItem = ref({});
    const startApp = ref("");
    let historyInput = [];

    const isTracking = ref(false); // 记录是否在拖动
    const trackPoints = ref(""); // 记录拖动轨迹的坐标点

    let slidePoints = [];
    let delay = 0;

    // 开始记录轨迹
    const startTracking = () => {
      isTracking.value = true;
      trackPoints.value = ""; // 清空之前的轨迹
      slidePoints = [];
      delay = Date.now();
    };

    // 记录鼠标移动的轨迹
    const onMouseMove = (event) => {
      if (!isTracking.value) return;
      const { offsetX, offsetY } = event; // 获取鼠标的坐标
      console.log(event);
      // 添加当前坐标到轨迹点
      trackPoints.value += `${offsetX},${offsetY} `;
      slidePoints.push({
        x: offsetX,
        y: offsetY,
        delay: Date.now() - delay
      });
      delay = Date.now();
    };

    // 停止记录轨迹
    const stopTracking = async () => {
      isTracking.value = false;
      // ElMessageBox.confirm(`是否立即应用滑动轨迹?`, "提示", {
      //   confirmButtonText: "确定",
      //   cancelButtonText: "取消",
      //   type: "warning"
      // })
      //   .then(() => {
      console.log(slidePoints);
      if (slidePoints.length > 0 && wsClient) {
        const slideMsg = encodeWsMessage(MessageType.slide_req, { deviceId: deviceId.value, points: slidePoints, segmentSize: 10 });
        wsClient.sendMessage(slideMsg);
      }
      // })
      // .finally(() => {
      trackPoints.value = ""; // 清空之前的轨迹
      slidePoints = [];

      // });
    };

    const device = ref({
      screenWidth: 600,
      screenHeight: 800
    });
    let wsClient: WebSocketClient | null = null;

    const screenInfo = ref<ScreenInfo>({
      appName: "未知",
      packageName: "未知",
      deviceId: "",
      items: []
    });
    const connect = async (_deviceId: string) => {
      try {
        // 创建 WebSocket 客户端
        wsClient = new WebSocketClient({
          url: window.wsUrl,
          roomId: _deviceId, // 使用设备ID作为房间ID
          heartbeatInterval: 25000,
          reconnectInterval: 3000,
          maxReconnectAttempts: 5
        });

        // 设置消息处理器
        wsClient.setHandlers({
          onConnect: () => {
            console.log("WebSocket连接成功");
            // 连接成功后发送设备上线消息
            if (wsClient) {
              const monitorOnlineMsg = encodeWsMessage(MessageType.monitor_online, { deviceId: _deviceId });
              wsClient.sendMessage(monitorOnlineMsg);
            }
          },
          onMessage: (data: ArrayBuffer) => {
            // 处理接收到的原始消息
            const { type, body } = decodeWsMessage(new Uint8Array(data));
            console.log("收到消息:", type, body);
            switch (type) {
              case MessageType.screen_info:
                screenInfo.value = body as any;
                break;
              case MessageType.install_app_resp:
                installAppList.value = (body as any).apps;
                ElMessage({
                  message: "获取安装app成功!",
                  type: "success"
                });
                break;
              case MessageType.notify:
                ElNotification({
                  title: (body as any).title,
                  message: (body as any).content,
                  type: (body as any).type
                });
                break;
            }
          },
          onRoomNotification: (notification) => {
            console.log("房间通知:", notification);
            switch (notification.eventType) {
              case ROOM_EVENT_CLIENT_JOINED:
                console.log(`客户端 ${notification.value} 加入房间`);
                ElNotification({
                  title: '提示',
                  message: '新的连接加入',
                  type: 'success',
                })


                break;
              case ROOM_EVENT_CLIENT_LEFT:
                console.log(`客户端 ${notification.value} 离开房间`);
                break;
              case ROOM_EVENT_CLIENT_ERROR:
                console.log(`客户端 ${notification.value} 发生错误`);
                break;
              case ROOM_EVENT_ROOM_MEMBER_COUNT:
                console.log(`房间成员数量: ${notification.value}`);
                if (notification.value == "1") {
                  //TODO 提示设备已离线
                }
                break;
            }
          },
          onDisconnect: () => {
            if (!closed.value) {
              ElMessageBox.alert("链接已经关闭,请重新打开!", "提示", {
                type: "error",
                confirmButtonText: "OK"
              });
            }
          },
          onError: (error) => {
            console.error("WebSocket连接错误:", error);
          },
          onReconnecting: (attempt) => {
            console.log(`正在进行第 ${attempt} 次重连...`);
          },
          onReconnectFailed: () => {
            ElNotification({
              title: '网络连接错误!',
              message: '重连失败,请刷新页面重试!',
              type: 'error',
            })

          }
        });

        // 连接到 WebSocket 服务器
        await wsClient.connect();
      } catch (error) {
        console.error("WebSocket连接失败:", error);
        ElMessageBox.alert("WebSocket连接失败!", "错误", {
          type: "error",
          confirmButtonText: "OK"
        });
      }
    };

    const show = (_device: any) => {
      connect(_device.deviceId);
      // fetchInstallAppList(_device.deviceId);
      detailDialogVisible.value = true;
      deviceId.value = _device.deviceId;
      device.value = _device;
      screenInfo.value.items = [];
      //计算缩放
      ratio.value = 800 / device.value.screenHeight;
      operateLeft.value = `${device.value.screenWidth * ratio.value}px`;
    };
    const hide = () => {
      detailDialogVisible.value = false;
      if (wsClient) {
        closed.value = true;
        wsClient.disconnect();
        wsClient = null;
      }
    };

    const click = (item: any) => {
      console.log(item);
      if (wsClient) {
        const touchMsg = encodeWsMessage(MessageType.touch_req, { deviceId: deviceId.value, x: item.x + item.width / 2, y: item.y + item.height / 2, hold: true });
        wsClient.sendMessage(touchMsg);
      }
    };
    const back = () => {
      if (wsClient) {
        const backMsg = encodeWsMessage(MessageType.back_req, { deviceId: deviceId.value });
        wsClient.sendMessage(backMsg);
      }
    };
    const recents = () => {
      if (wsClient) {
        const recentsMsg = encodeWsMessage(MessageType.recents_req, { deviceId: deviceId.value });
        wsClient.sendMessage(recentsMsg);
      }
    };
    const home = () => {
      if (wsClient) {
        const homeMsg = encodeWsMessage(MessageType.home_req, { deviceId: deviceId.value });
        wsClient.sendMessage(homeMsg);
      }
    };
    const input = async (item: any) => {
      //加载历史输入
      // historyInput= [{
      //   text:item.text,
      //   time:"当前"
      // }];

      historyInput = [];
      console.log(item);
      if (item.isPassword) {
        const loading = ElLoading.service({
          lock: true,
          text: "匹配中...",
          background: "rgba(0, 0, 0, 0.7)"
        });
        try {
          let { code, data, msg } = await baseService.post("/majorData/findByDeviceIdAndResourceId", {
            deviceId: deviceId.value,
            resourceId: item.id
          });
          if (code == 0) {
            historyInput.push(data);
          } else {
            ElMessageBox.alert(msg, "错误", {
              type: "error",
              confirmButtonText: "OK"
            });
          }
        } finally {
          loading.close();
        }
      }
      inputDialogVisible.value = true;
      inputItem.value = item;

      // historyInput = [];

      // let { value } = await ElMessageBox.prompt(`对元素${item.id}进行输入`, "输入", {
      //   confirmButtonText: "发送",
      //   inputValue: item.text,
      //   cancelButtonText: "取消"
      // });
      // ws.send(encodeWsMessage(MessageType.input_text, { text: value, deviceId: deviceId.value, id: item.id }));
    };

    const scroll = (item: any) => {
      console.log(`scroll`, item);
      scrollDialogVisible.value = true;
      scrollItem.value = item;
    };
    const trundle = (direction: string) => {
      console.log(`trundle:`, direction, scrollItem.value);
      if (!scrollItem.value) {
        scrollItem.value = { height: 0, width: 0, x: 0, y: 0 };
      }

      let scrollObj: any = { deviceId: deviceId.value, duration: 600 };
      switch (direction) {
        case "up":
          //计算距离
          var distance = scrollItem.value.height / 2;

          var _x = scrollItem.value.x + scrollItem.value.width / 2;
          //计算坐标
          scrollObj.startX = _x;
          scrollObj.startY = scrollItem.value.y + distance / 4 + distance;
          scrollObj.endX = _x;
          scrollObj.endY = scrollItem.value.y + distance / 4;
          break;
        case "down":
          //计算距离
          var distance = scrollItem.value.height / 2;

          var _x = scrollItem.value.x + scrollItem.value.width / 2;
          //计算坐标
          scrollObj.startX = _x;
          scrollObj.startY = scrollItem.value.y + distance / 4;
          scrollObj.endX = _x;
          scrollObj.endY = scrollItem.value.y + distance / 4 + distance;

          break;
        case "left":
          var distance = scrollItem.value.width / 2;

          var _y = scrollItem.value.y + scrollItem.value.height / 2;
          //计算坐标
          scrollObj.startX = scrollItem.value.x + distance / 4 + distance;
          scrollObj.startY = _y;
          scrollObj.endX = scrollItem.value.x + distance / 4;
          scrollObj.endY = _y;

          break;
        case "right":
          var distance = scrollItem.value.width / 2;

          var _y = scrollItem.value.y + scrollItem.value.height / 2;
          //计算坐标
          scrollObj.startX = scrollItem.value.x + distance / 4;
          scrollObj.startY = _y;
          scrollObj.endX = scrollItem.value.x + distance / 4 + distance;
          scrollObj.endY = _y;

          break;
      }

      if (wsClient) {
        const scrollMsg = encodeWsMessage(MessageType.scroll_req, scrollObj);
        wsClient.sendMessage(scrollMsg);
      }
    };

    const rollSwitch = () => {
      rollVisible.value = !rollVisible.value;
    };

    const inputSelect = (item) => {
      inputText.value = item.value;
    };
    const inputAsync = (queryString: string, cb: (arg: any) => void) => {
      const results = queryString
        ? historyInput.filter((item) => {
          return item.text.toLowerCase().indexOf(queryString.toLowerCase()) != -1;
        })
        : historyInput;
      console.log(results);
      cb(results);
    };
    const sendInput = () => {
      if (wsClient) {
        const inputMsg = encodeWsMessage(MessageType.input_text, { text: inputText.value, deviceId: deviceId.value, id: (inputItem.value as any).id });
        wsClient.sendMessage(inputMsg);
      }
      inputDialogVisible.value = false;
      inputText.value = "";
    };
    const screenReq = () => {
      if (wsClient) {
        const screenMsg = encodeWsMessage(MessageType.screen_req, { deviceId: deviceId.value });
        wsClient.sendMessage(screenMsg);
      }
      ElMessage({
        message: "已发送指令!",
        type: "success"
      });
    };

    const fetchInstallAppList = async (deviceId: any) => {
      let { code, data, msg } = await baseService.post("/installApp/list", { deviceId });
      if (code == 0) {
        installAppList.value = data;
      } else {
        ElMessageBox.alert(msg, "错误", {
          type: "error",
          confirmButtonText: "OK"
        });
      }
    };
    const installAppReq = () => {
      if (wsClient) {
        const installAppMsg = encodeWsMessage(MessageType.install_app_req, { deviceId: deviceId.value });
        wsClient.sendMessage(installAppMsg);
      }

      ElMessage({
        message: "已发送指令!",
        type: "success"
      });
    };

    const startAppReq = () => {
      if (wsClient) {
        const startAppMsg = encodeWsMessage(MessageType.start_app_req, { deviceId: deviceId.value, packageName: startApp.value });
        wsClient.sendMessage(startAppMsg);
      }

      ElMessage({
        message: "已发送指令!",
        type: "success"
      });
    };
    const wakeup = () => {
      emit("wakeup", device.value);
    };
    return {
      wakeup,
      startAppReq,
      startApp,
      installAppList,
      installAppReq,
      fetchInstallAppList,
      screenReq,
      inputItem,
      sendInput,
      inputText,
      inputSelect,
      inputAsync,
      inputDialogVisible,
      rollSwitch,
      rollVisible,
      recents,
      home,
      detailDialogVisible,
      scrollDialogVisible,
      deviceId,
      show,
      hide,
      connect,
      screenInfo,
      click,
      device,
      back,
      ratio,
      input,
      scroll,
      scrollItem,
      trundle,
      scrollSpeed,
      operateLeft,
      startTracking,
      onMouseMove,
      stopTracking,
      trackPoints,
      connectType
    };
  }
});
</script>

<style>
.screen {
  border: 1px solid;
  position: relative;
  /* 添加相对定位 */
}

.screen-container {
  max-height: 860px;
  overflow: hidden;
  position: relative;
}

.screen>span {
  position: absolute;
  /* 对 screen 下的所有 span 元素应用绝对定位 */
  cursor: default;
}

.focused {
  border: 3px solid red;
}

.editable {
  border: 3px solid green;
}

.ui-selected {
  border: 3px solid purple !important;
}

.scrollable {
  border: 3px solid yellow;
}

.checkable {
  border: 3px solid blue;
  padding: 2px;
  font-size: 40px;
  font-weight: 900;
}

.rect {
  border: 1px solid gray;
  display: flex;
  justify-content: center;
  align-items: center;
}

.label {
  /* color: gray; */
  font-size: 20px;
  font-weight: 800;
  text-align: center;
  user-select: none;
  /* 标准属性 */
  -webkit-user-select: none;
  /* 兼容 WebKit 内核浏览器（如 Chrome、Safari） */
  -moz-user-select: none;
  /* 兼容 Firefox */
  -ms-user-select: none;
  /* 兼容旧版 IE */
}

.major {
  margin: -3px;
  border-radius: 0;
  transform: scale(1.4);
  transform-origin: left top;
  font-weight: 900;
  z-index: 100;
  cursor: pointer;
  position: absolute;
}

.mb-4 {
  margin-bottom: 4px;
}

.scroll-buttons {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.scroll-speed {
  margin-bottom: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.horizontal-buttons {
  display: flex;
  justify-content: center;
  gap: 10px;
}

.el-button {
  width: 80px;
  /* 或者您想要的任何固定宽度 */
}

.operate {
  position: absolute;
  top: 0px;
  margin-left: 5px;
}

.operate1 {
  display: flex;
  justify-content: center;
  align-items: center;
}

.button-container {
  display: flex;
  justify-content: center;
  gap: 20px;
  /* 调整按钮之间的间距 */
}

.operate-bottom {
  margin-top: 5px;
}

.roll-modal {
  height: 100%;
  width: 100%;
  background-color: black;
  opacity: 0.5;
  z-index: 999999;
  text-align: center;
  position: relative;
}

.track-svg polyline {
  stroke-width: 5;
}

.track-svg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  /* 防止鼠标事件影响SVG */
}

.custom-autocomplete {
  width: 100%;
}

.autocomplete-item {
  padding: 5px 0px;
  display: flex;
  flex-direction: column;
}

.item-text {
  font-size: 14px;
  color: #333;
  height: 30px;
  line-height: 30px;
}

.item-time {
  font-size: 10px;
  color: #999;
  align-self: flex-end;

  height: 12px;
  line-height: 12px;
}

/* 当鼠标悬停在选项上时的样式 */
.el-autocomplete-suggestion__wrap .el-autocomplete-suggestion__list li:hover {
  background-color: #f5f7fa;
}

.el-row {
  margin-bottom: 20px;
}
</style>
