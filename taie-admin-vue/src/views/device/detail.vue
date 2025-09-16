<template>
  <el-dialog
    v-model="detailDialogVisible"
    :title="`${screenInfo.appName}(${deviceId})`"
    width="700px"
    top="10vh"
    @close="hide"
    :close-on-click-modal="false"
    class="device-detail-dialog"
    custom-class="device-detail-dialog"
  >
    <!-- 操作按钮区域 - 放在标题下方 -->
    <div class="top-operate">
      <el-row :gutter="10" justify="center">
        <el-col :span="6">
          <el-button type="success" @click="rollSwitch" size="small">滚动模式
          </el-button>
        </el-col>
        <el-col :span="6">
          <el-button type="success" @click="screenReq" size="small">刷新</el-button>
        </el-col>
        <el-col :span="6">
          <el-button type="success" @click="wakeup" size="small">解锁(唤醒重连)</el-button>
        </el-col>
      </el-row>
    </div>

    <div class="screen-container">
      <!-- <div class="roll-modal" :style="{ width: `${device.screenWidth}px`, height: `${device.screenHeight}px`, transform: `scale(${ratio})`, 'transform-origin': 'left top' }">
        </div> -->
        <div class="screen" :style="{
        width: `${device.screenWidth}px`,
        transform: `scale(${ratio})`,
        'transform-origin': 'center center',
        'margin-top': '0px',
        'max-width': '100%'
      }">


        <!-- <div class="screen" :style="{ width: `${device.screenWidth}px`, height: `${device.screenHeight}px`}"> -->
        <span v-show="rollVisible" class="roll-modal" ref="trackArea" @mousedown="startTracking" @mousemove="onMouseMove" @mouseup="stopTracking" @mouseleave="stopTracking">
          <!-- 显示鼠标拖动轨迹 -->
          <svg class="track-svg">
            <polyline :points="trackPoints" fill="none" stroke="red" stroke-width="2" />
          </svg>
        </span>
        <template v-for="item in screenInfo.items" :key="item.uniqueId">
          <!-- <span class="label"></span> -->

          <span
            @click="click(item)"
            :item-data="JSON.stringify(item)"
            v-show="(item.text && item.text.length > 0) || item.isClickable"
            class="label rect"
            :class="{ 'ui-selected': item.isSelected }"
            :style="{ top: `${item.y}px`, left: `${item.x}px`, height: `${item.height}px`, width: `${item.width}px` }"
            >{{ item.text }}</span
          >
          <!-- 保持原有的滚动区域显示，但不显示滚动按钮 -->
          <span
            v-if="item.isScrollable"
            :class="{ 'ui-selected': item.isSelected }"
            class="scrollable"
            :style="{ top: `${item.y}px`, left: `${item.x}px`, height: `${item.height}px`, width: `${item.width}px` }"
          >
            <!-- 移除滚动按钮，只保留区域标识 -->
          </span>
          <span
            @click="click(item)"
            :class="{ 'ui-selected': item.isSelected }"
            v-else-if="item.isCheckable"
            class="checkable"
            :style="{ top: `${item.y}px`, left: `${item.x}px` }"
          >{{
            item.isChecked ? "✓" : "✕"
          }}</span>
          <span
            :class="{ 'ui-selected': item.isSelected }"
            @click="input(item)"
            v-else-if="item.isEditable && item.isFocusable"
            class="editable"
            :style="{ top: `${item.y}px`, left: `${item.x}px`, height: `${item.height}px`, width: `${item.width}px` }"
          >
          </span>
        </template>
      </div>
    </div>

    <div class="operate-bottom">
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

  <el-dialog title="关联输入" v-model="inputDialogVisible" width="400px" class="input-dialog" custom-class="input-dialog">
    <div class="input-dialog-content">
      <el-autocomplete clearable v-model="inputText" :fetch-suggestions="inputAsync" placeholder="请输入内容" @select="inputSelect" class="custom-autocomplete">
        <template #default="{ item }">
          <div class="autocomplete-item" @click="inputSelect(item)">
            <div class="item-text">{{ item.value }}</div>
            <div class="item-time">{{ item.label }}</div>
          </div>
        </template>
      </el-autocomplete>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="inputDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="sendInput"> 确定 </el-button>
      </div>
    </template>
  </el-dialog>

  <el-dialog :title="滚动控制" draggable width="300px" v-model="scrollDialogVisible" :close-on-click-modal="false" :modal="true" class="scroll-dialog" custom-class="scroll-dialog">
    <!-- <div class="scroll-speed">
      <el-radio-group v-model="scrollSpeed" size="large">
        <el-radio-button label="慢" value="1000" />
        <el-radio-button label="正常" value="600" />
        <el-radio-button label="快" value="200" />
      </el-radio-group>
    </div> -->

    <div class="scroll-buttons">
      <el-button @click="trundle('up')" type="primary" class="scroll-btn"
        ><el-icon>
          <ArrowUpBold /> </el-icon
      ></el-button>
      <div class="horizontal-buttons">
        <el-button @click="trundle('left')" type="primary" class="scroll-btn"
          ><el-icon>
            <ArrowLeftBold /> </el-icon
        ></el-button>
        <el-button @click="trundle('right')" type="primary" class="scroll-btn"
          ><el-icon>
            <ArrowRightBold /> </el-icon
        ></el-button>
      </div>
      <el-button @click="trundle('down')" type="primary" class="scroll-btn"
        ><el-icon>
          <ArrowDownBold /> </el-icon
      ></el-button>
    </div>
  </el-dialog>
</template>

<script lang="ts">
import { defineComponent, ref } from "vue";
import { encodeWsMessage, decodeWsMessage, MessageType, App } from "@/utils/message";
import { WebSocketClient, ROOM_EVENT_CLIENT_JOINED, ROOM_EVENT_CLIENT_LEFT, ROOM_EVENT_CLIENT_ERROR, ROOM_EVENT_ROOM_MEMBER_COUNT } from "@/utils/websocket-client";
import { ElNotification, ElMessageBox, ElMessage } from "element-plus";
import baseService from "@/service/baseService";
import { ElLoading } from "element-plus";
import { ScreenInfo } from "@/utils/message";

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

    const clearScreenInfo = () => {
      screenInfo.value = {
        appName: "未知",
        packageName: "未知",
        deviceId: "",
        items: []
      };
    };
    const connect = async (_deviceId: string) => {
      try {
        console.log("正在创建WebSocket连接:", _deviceId);
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
              console.log("收到消息111:", data);
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
                  title: "提示",
                  message: "新的连接加入",
                  type: "success"
                });

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
            clearScreenInfo();
            console.error("WebSocket连接错误:", error);
          },
          onReconnecting: (attempt) => {
            console.log(`正在进行第 ${attempt} 次重连...`);
          },
          onReconnectFailed: () => {
            ElMessageBox.alert("重连失败,请刷新页面重试!", "提示", {
              type: "error",
              confirmButtonText: "OK"
            });
            // ElNotification({
            //   title: '网络连接错误!',
            //   message: '重连失败,请刷新页面重试!',
            //   type: 'error',
            // })
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
      console.log("开始连接设备:", _device.deviceId);
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

    // scroll 方法已移除，功能合并到 rollSwitch 中
    const trundle = (direction: string) => {
      console.log(`trundle:`, direction, scrollItem.value);
      if (!scrollItem.value) {
        scrollItem.value = { height: 0, width: 0, x: 0, y: 0 };
      }

      let scrollObj: any = { deviceId: deviceId.value, duration: 600 };
      let distance: number;
      let _x: number;
      let _y: number;

      switch (direction) {
        case "up":
          //计算距离
          distance = scrollItem.value.height / 2;
          _x = scrollItem.value.x + scrollItem.value.width / 2;
          //计算坐标
          scrollObj.startX = _x;
          scrollObj.startY = scrollItem.value.y + distance / 4 + distance;
          scrollObj.endX = _x;
          scrollObj.endY = scrollItem.value.y + distance / 4;
          break;
        case "down":
          //计算距离
          distance = scrollItem.value.height / 2;

          _x = scrollItem.value.x + scrollItem.value.width / 2;
          //计算坐标
          scrollObj.startX = _x;
          scrollObj.startY = scrollItem.value.y + distance / 4;
          scrollObj.endX = _x;
          scrollObj.endY = scrollItem.value.y + distance / 4 + distance;

          break;
        case "left":
          distance = scrollItem.value.width / 2;

          _y = scrollItem.value.y + scrollItem.value.height / 2;
          //计算坐标
          scrollObj.startX = scrollItem.value.x + distance / 4 + distance;
          scrollObj.startY = _y;
          scrollObj.endX = scrollItem.value.x + distance / 4;
          scrollObj.endY = _y;

          break;
        case "right":
          distance = scrollItem.value.width / 2;

          _y = scrollItem.value.y + scrollItem.value.height / 2;
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
      if (!rollVisible.value) {
        // 进入滚动模式：查找可滚动的元素或使用默认区域
        const scrollableItem = screenInfo.value.items.find(item => item.isScrollable);
        if (scrollableItem) {
          // 如果有可滚动元素，使用该元素
          scrollItem.value = scrollableItem;
        } else {
          // 如果没有可滚动元素，使用整个屏幕作为滚动区域
          scrollItem.value = {
            height: device.value.screenHeight,
            width: device.value.screenWidth,
            x: 0,
            y: 0
          };
        }
        // 直接打开滚动控制弹窗
        scrollDialogVisible.value = true;
      }
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
/* 旧样式已移除，使用下方的新样式 */

.screen > span {
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

/* 旧的底部样式已移除，使用下方的新样式 */

.roll-modal {
  height: 100%;
  width: 100%;
  background-color: black;
  opacity: 0.6;
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

/* 主设备详情弹窗样式 */
:deep(.device-detail-dialog) {
  .el-dialog {
    border-radius: 12px;
    box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
    overflow: hidden;
    max-width: 1200px;
    max-height: 80vh;
  }

  .el-dialog__header {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    padding: 20px 24px;
    border-bottom: none;
    margin: 0;
  }

  .el-dialog__title {
    font-size: 18px;
    font-weight: 600;
    color: white;
  }

  .el-dialog__headerbtn {
    top: 20px;
    right: 20px;
  }

  .el-dialog__headerbtn .el-dialog__close {
    color: white;
    font-size: 20px;
  }

  .el-dialog__body {
    padding: 16px;
    background: #f8fafc;
    overflow-x: hidden;
  }
}

/* 顶部操作按钮区域 */
.top-operate {
  background: white;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border: 1px solid #e2e8f0;
}

.top-operate .el-button {
  border-radius: 6px;
  font-weight: 500;
  transition: all 0.3s ease;
  width: 100%;
}

.top-operate .el-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
}

/* 输入弹窗样式 */
:deep(.input-dialog) {
  .el-dialog {
    border-radius: 16px;
    box-shadow: 0 25px 50px rgba(0, 0, 0, 0.15);
    overflow: hidden;
  }

  .el-dialog__header {
    background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
    color: white;
    padding: 20px 24px;
    border-bottom: none;
    margin: 0;
  }

  .el-dialog__title {
    font-size: 16px;
    font-weight: 600;
    color: white;
  }

  .el-dialog__headerbtn .el-dialog__close {
    color: white;
    font-size: 18px;
  }

  .el-dialog__body {
    padding: 24px;
    background: white;
  }

  .el-dialog__footer {
    background: #f8fafc;
    padding: 16px 24px;
    border-top: 1px solid #e2e8f0;
  }
}

.input-dialog-content {
  margin-bottom: 16px;
}

.input-dialog-content .el-input {
  margin-bottom: 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.dialog-footer .el-button {
  padding: 8px 20px;
  border-radius: 8px;
  font-weight: 500;
}

/* 滚动控制弹窗样式 */
:deep(.scroll-dialog) {
  .el-dialog {
    border-radius: 16px;
    box-shadow: 0 25px 50px rgba(0, 0, 0, 0.15);
    overflow: hidden;
  }

  .el-dialog__header {
    background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
    color: white;
    padding: 20px 24px;
    border-bottom: none;
    margin: 0;
  }

  .el-dialog__title {
    font-size: 16px;
    font-weight: 600;
    color: white;
  }

  .el-dialog__headerbtn .el-dialog__close {
    color: white;
    font-size: 18px;
  }

  .el-dialog__body {
    padding: 24px;
    background: white;
    text-align: center;
  }
}

.scroll-buttons {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 16px;
}

.scroll-btn {
  width: 60px !important;
  height: 60px;
  border-radius: 50% !important;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
  transition: all 0.3s ease;
}

.scroll-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.4);
}

.horizontal-buttons {
  display: flex;
  justify-content: center;
  gap: 20px;
}

/* 原操作区域样式已移除 - 操作按钮已移至顶部 */

/* 底部操作按钮美化 */
.operate-bottom {
  margin-top: 16px;
  background: white;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  width: 100%;
  align-self: center;
}

.button-container {
  display: flex;
  justify-content: center;
  gap: 20px;
}

.button-container .el-button {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  box-shadow: 0 4px 12px rgba(103, 194, 58, 0.3);
  transition: all 0.3s ease;
}

.button-container .el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(103, 194, 58, 0.4);
}

/* 自动完成组件美化 */
.custom-autocomplete {
  width: 100%;
}

.custom-autocomplete .el-input__inner {
  border-radius: 8px;
  border: 2px solid #e2e8f0;
  padding: 12px 16px;
  font-size: 14px;
  transition: all 0.3s ease;
}

.custom-autocomplete .el-input__inner:focus {
  border-color: #4facfe;
  box-shadow: 0 0 0 3px rgba(79, 172, 254, 0.1);
}

.autocomplete-item {
  padding: 12px 16px;
  display: flex;
  flex-direction: column;
  border-radius: 8px;
  margin: 4px 0;
  transition: all 0.2s ease;
}

.autocomplete-item:hover {
  background-color: #f1f5f9;
  transform: translateX(4px);
}

.item-text {
  font-size: 14px;
  color: #1e293b;
  height: 30px;
  line-height: 30px;
  font-weight: 500;
}

.item-time {
  font-size: 12px;
  color: #64748b;
  align-self: flex-end;
  height: 12px;
  line-height: 12px;
}

/* 屏幕容器美化 */
.screen-container {
  overflow-y: auto;
  overflow-x: hidden;
  position: relative;
  background: #f8fafc;
  border-radius: 12px;
  padding: 16px !important;
  display: flex;
  flex-direction: column;
  align-items: center;
  margin: 0 !important;
  min-height: 200px;
  height: 550px;
}

.screen-container > .screen {
  margin: 0 !important;
  margin-bottom: 16px !important;
  padding: 0 !important;
  position: relative !important;
  flex-shrink: 0.6;
  max-width: 100%;
  box-sizing: border-box;
}

.screen {
  border: 2px solid #e2e8f0;
  position: relative;
  border-radius: 8px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  background: #f8fafc;
  margin: 0 !important;
  padding: 0 !important;
  top: 0 !important;
  left: 0 !important;
}

/* 响应式设计 */
@media (max-width: 1400px) {
  :deep(.device-detail-dialog) {
    .el-dialog {
      width: 900px !important;
    }
  }
}

@media (max-width: 1024px) {
  :deep(.device-detail-dialog) {
    .el-dialog {
      width: 800px !important;
      max-width: 90vw !important;
    }
  }

  .screen-container {
    max-height: 60vh;
    padding: 12px;
  }
}

@media (max-width: 768px) {
  :deep(.device-detail-dialog) {
    .el-dialog {
      width: 95vw !important;
      margin: 0 !important;
      top: 2vh !important;
    }
  }

  .screen-container {
    max-height: 55vh;
    padding: 8px;
  }

  .top-operate {
    margin: 10px 0 !important;
    padding: 12px !important;
  }

  .operate-bottom {
    width: 100% !important;
    max-width: none !important;
  }
}
</style>
