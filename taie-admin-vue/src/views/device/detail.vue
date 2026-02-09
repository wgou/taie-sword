<template>
  <el-dialog v-model="detailDialogVisible" :width="isPageMode ? '100%' : '1200px'" :top="isPageMode ? '0' : '2vh'"
    :fullscreen="isPageMode" :modal="!isPageMode" :show-close="!isPageMode" :close-on-click-modal="false"
    :teleport="!isPageMode" @close="onClose" :class="['device-detail-dialog', { 'is-page': isPageMode }]"
    custom-class="device-detail-dialog">

    <template #header>
      <div class="dialog-header">
        <div class="dialog-title">
          <div class="device-meta">
            <el-tag size="small" effect="light" type="info">设备ID: {{ deviceId || "-" }}</el-tag>
            <el-tag size="small" effect="light" type="info">当前位置: {{ screenInfo?.appName || "未知" }}</el-tag>
            <el-tag size="small" effect="light" type="info">包名: {{ device?.pkg || "-" }}</el-tag>
            <el-tag size="small" effect="light" type="info">品牌/型号: {{ [device?.brand,
            device?.model].filter(Boolean).join(" ") || "-" }}</el-tag>
            <el-tag size="small" effect="light" type="info">系统/SDK: {{ device?.systemVersion || "-" }} / {{
              device?.sdkVersion ?? "-" }}</el-tag>
            <el-tag size="small" effect="light" type="info">分辨率: {{ device?.screenWidth || "-" }}×{{
              device?.screenHeight || "-" }}</el-tag>
            <el-tag size="small" effect="light" type="info">城市/IP: {{ device?.addr || "-" }} / {{ device?.ip || "-"
            }}</el-tag>

            <el-tag size="small" effect="light" :type="screenStatusTagType">屏幕: {{ screenStatusText }}</el-tag>

            <el-tooltip v-if="device?.lockScreen" class="box-item" effect="dark"
              :content="formatUnlockTips(device.lockScreen)" placement="top" :show-after="300">
              <el-tag size="small" effect="light" type="success">可解锁</el-tag>
            </el-tooltip>
            <el-tag v-else size="small" effect="light" type="danger">不可解锁</el-tag>
          </div>
        </div>
      </div>
    </template>


    <!-- 主内容区域：左侧手机操作界面 + 右侧日志终端 -->
    <div class="main-content-wrapper">
      <!-- 左侧：完整的手机操作界面 -->
      <div class="device-control-panel">
        <div class="device-main-row">
          <div class="screen-container">
            <!-- <div class="roll-modal" :style="{ width: `${device.screenWidth}px`, height: `${device.screenHeight}px`, transform: `scale(${ratio})`, 'transform-origin': 'left top' }">
          </div> -->
            <div class="screen-sizer" style="width: 400px; height: 650px">
              <div class="screen" ref="screenRef" :class="{ 'scroll-mode': rollVisible }"
                @click="(event) => handleGlobalClick(event, false, screenRef)"
                v-longpress:500="(event) => handleGlobalClick(event, true, screenRef)" :style="{
                  width: `${device.screenWidth}px`,
                  height: `${device.screenHeight}px`,
                  transform: `scale(${ratioHeight})`,
                  'transform-origin': 'left top',
                  'margin-top': '0px',
                  left: `${Math.max(0, (400 - device.screenWidth * ratioHeight) / 2)}px`
                }">

                <!-- 屏幕边界框 - 始终显示黄色边框代表手机屏幕边界 -->

                <canvas ref="screenshotCanvas" :style="{
                  width: `${device.screenWidth}px`,
                  height: `${device.screenHeight}px`,
                }"></canvas>
                <span v-show="rollVisible" class="roll-modal" ref="trackArea" @mousedown="startTracking"
                  @mousemove="onMouseMove" @mouseup="stopTracking" @mouseleave="stopTracking"
                  @click="(event) => handleGlobalClick(event, false, screenRef)"
                  :style="{ width: `${device.screenWidth}px`, height: `${device.screenHeight}px` }">
                  <!-- 显示鼠标拖动轨迹 -->
                  <svg class="track-svg">
                    <polyline :points="trackPoints" fill="none" stroke="red" stroke-width="2" />
                  </svg>
                </span>

              </div> <!-- close .screen -->
            </div> <!-- close .screen-sizer -->
          </div> <!-- close .screen-container -->

          <!-- 功能按钮栏（放在 device-control-panel 内部，红框区域） -->
          <div class="side-controls">
            <div class="side-controls-inner">

              <!-- <div class="side-control-item">
                <el-select v-model="config.frameMode" class="side-select" size="small" placeholder="">
                  <el-option label="画面" :value="1"></el-option>
                  <el-option label="线条" :value="0"></el-option>
                </el-select>
              </div> -->


              <!-- <div class="side-control-item">
                <el-button type="success" @click="screenReq" size="small">刷新</el-button>
              </div> -->

              <div class="side-control-item">
                <el-popover v-model:visible="unlockPopoverVisible" v-if="!isConnected" trigger="click" placement="right"
                  :width="420" :teleported="false">
                  <template #reference>
                    <el-button type="success" @click="wakeup" size="small"><el-icon>
                        <Connection />
                      </el-icon>连接手机</el-button>

                  </template>

                  <div class="unlock-popover">
                    <div class="unlock-popover-title">选择解锁密码</div>
                    <el-select :teleported="false" v-model="selectedUnlockId" placeholder="请选择解锁密码" filterable
                      style="width: 100%">
                      <el-option v-for="item in unlockOptions" :key="item.id" :label="formatUnlockTips(item)"
                        :value="item.id" />
                    </el-select>
                    <div class="unlock-popover-actions">
                      <el-button size="small" @click="unlockPopoverVisible = false">取消</el-button>
                      <el-button size="small" type="primary" :loading="unlocking" :disabled="!selectedUnlockId"
                        @click="confirmWakeup">确定</el-button>
                    </div>
                  </div>
                </el-popover>

                <el-dropdown v-else style="width: 100%;">
                  <el-button type="danger" size="small" @click="disconnect">
                    断开链接<el-icon class="el-icon--right"><arrow-down /></el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item @click="disconnectAndLockScreen">断开链接并锁屏</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>

              <div class="side-control-item">
                <el-button type="success" @click="input" size="small"><el-icon>
                    <Edit />
                  </el-icon>输入</el-button>
              </div>


              <div class="side-control-item">
                <el-button type="success" v-if="!config.lines" @click="openLines" size="small"><el-icon>
                    <DataLine />
                  </el-icon>打开线条</el-button>
                <el-button type="danger" v-else @click="closeLines" size="small"><el-icon>
                    <DataLine />
                  </el-icon>关闭线条</el-button>



                <el-dialog v-model="linesVisible" @close="closeLines" :modal="false" modal-penetrable
                  :close-on-click-modal="false" title="线条" :style="{
                    width: `${device.screenWidth * ratioHeight + 42}px`,
                  }" draggable>

                  <div :style="{
                    width: `${device.screenWidth * ratioHeight}px`,
                    height: `${device.screenHeight * ratioHeight + 2}px`,
                    position: 'relative',
                  }">



                    <div class="screen" @click="(event) => handleGlobalClick(event, false, screen2Ref)" ref="screen2Ref"
                      :style="{
                        width: `${device.screenWidth}px`,
                        height: `${device.screenHeight}px`,
                        transform: `scale(${ratioHeight})`,
                        'transform-origin': 'left top',
                        'margin-top': '0px',
                      }">
                      <div class="screen-boundary" :class="{ 'block-mode': config.screenOff }"
                        :style="{ width: `${device.screenWidth}px`, height: `${device.screenHeight}px` }"></div>

                      <template v-for="item in screenInfo.items" :key="item.uniqueId">
                        <span :item-data="JSON.stringify(item)"
                          v-show="(item.text && item.text.length > 0) || item.isClickable" class="label rect"
                          :class="{ 'ui-selected': item.isSelected }" v-longpress:500="() => click(item, true)"
                          :style="{ top: `${item.y}px`, left: `${item.x}px`, height: `${item.height}px`, width: `${item.width}px` }">{{
                            foldingText(item.text) }}</span>

                        <span :class="{ 'ui-selected': item.isSelected }" v-if="item.isCheckable" class="checkable"
                          :style="{ top: `${item.y}px`, left: `${item.x}px` }">
                          {{ item.isChecked ? "✓" : "✕" }}
                        </span>

                        <span :class="{
                          'ui-selected': item.isSelected
                        }" @click.stop="input(item)" v-else-if="item.isEditable && item.isFocusable" class="editable"
                          :style="{ top: `${item.y}px`, left: `${item.x}px`, height: `${item.height}px`, width: `${item.width}px` }">
                        </span>
                      </template>

                      <!-- 最后渲染可滚动区域，确保在最上层 -->
                      <template v-for="item in screenInfo.items" :key="`scrollable-${item.uniqueId}`">
                        <span v-if="item.isScrollable" :class="{ 'ui-selected': item.isSelected }" class="scrollable"
                          :style="{ top: `${item.y}px`, left: `${item.x}px`, height: `${item.height}px`, width: `${item.width}px` }">
                          <el-button @click.stop="rollSwitch(item)" class="scroll-button" type="info"
                            size="small">滚动</el-button>
                        </span>
                      </template>

                      <!-- 滚动遮罩层 - 放在最后确保在所有元素之上 -->
                      <span v-show="rollVisible" class="roll-modal" ref="trackArea" @mousedown="startTracking"
                        @mousemove="onMouseMove" @mouseup="stopTracking" @mouseleave="stopTracking"
                        @click="handleGlobalClick"
                        :style="{ width: `${device.screenWidth}px`, height: `${device.screenHeight}px` }">
                        <!-- 显示鼠标拖动轨迹 -->
                        <svg class="track-svg">
                          <polyline :points="trackPoints" fill="none" stroke="red" stroke-width="2" />
                        </svg>
                      </span>

                    </div>
                  </div>
                </el-dialog>



              </div>




              <div class="side-control-item">
                <el-button :type="config.screenOff ? 'danger' : 'success'" @click="screenOff" size="small">
                  <el-icon>
                    <Monitor />
                  </el-icon>{{ config.screenOff ? "退出遮挡" : "遮挡屏幕" }}
                </el-button>
              </div>

              <div class="side-control-item">
                <el-button :type="rollVisible ? 'danger' : 'success'" @click="toggleScrollMode" size="small">
                  <el-icon>
                    <Sort />
                  </el-icon> {{ rollVisible ? "退出滚动" : "进入滚动" }}
                </el-button>
              </div>

              <div class="side-control-item">
                <el-button type="success" @click="setRingerMode" size="small">
                  <el-icon>
                    <MuteNotification />
                  </el-icon>勿扰模式
                </el-button>
              </div>


              <div class="side-control-item">
                <el-button type="success" v-if="!config.camera" @click="openCamera" size="small">
                  <el-icon>
                    <Camera />
                  </el-icon> 打开摄像
                </el-button>



                <el-button type="danger" v-else @click="closeCamera" size="small">
                  <el-icon>
                    <Camera />
                  </el-icon> 关闭摄像
                </el-button>

                <el-dialog @close="closeCamera" v-model="cameraVisible" :modal="false" modal-penetrable
                  :close-on-click-modal="false" title="摄像头" :style="{
                    width: `${device.screenWidth * ratioHeight + 40}px`,
                  }" draggable>

                  <canvas ref="cameraScreenshotCanvas" :style="{
                    width: `${device.screenWidth * ratioHeight}px`,
                    height: `${device.screenHeight * ratioHeight}px`,
                    'transform-origin': 'center center',
                    'margin-top': '0px',
                    transform: `rotate(${cameraRotation}deg)`,
                  }"></canvas>


                  <template #footer>
                    <div class="dialog-footer">
                      <el-button @click="rotateCamera">旋转</el-button>
                      <el-button type="primary" @click="switchCamera">
                        切换摄像头
                      </el-button>
                    </div>
                  </template>
                </el-dialog>



              </div>


            </div>
          </div>
        </div>

        <div class="operate-bottom">
          <div class="button-container">
            <el-tooltip class="box-item" effect="dark" content="正在运行的APP" placement="top">
              <el-button type="success" size="small" @click="recents">
                <el-icon>
                  <Menu />
                </el-icon>
              </el-button>
            </el-tooltip>
            <el-tooltip class="box-item" effect="dark" content="主页" placement="top">
              <el-button type="success" size="small" @click="home">
                <el-icon>
                  <House />
                </el-icon>
              </el-button>
            </el-tooltip>
            <el-tooltip class="box-item" effect="dark" content="回退" placement="top">
              <el-button type="success" size="small" @click="back">
                <el-icon>
                  <ArrowLeftBold />
                </el-icon>
              </el-button>
            </el-tooltip>
          </div>
        </div>
      </div> <!-- Close device-control-panel -->

      <!-- 右侧：日志终端区域 -->
      <div class="terminal-container">
        <div class="terminal-header">
          <div class="terminal-signal">
            <div class="bars">
              <span v-for="n in 4" :key="n" class="bar" :class="[signalLevel, { active: n <= signalBars }]"></span>
            </div>
          </div>
          <div class="terminal-title">Device Log Terminal</div>
          <div class="terminal-actions">
            <el-button @click="clearLogs" size="small" type="text" class="clear-btn">
              <el-icon>
                <Delete />
              </el-icon>
            </el-button>
          </div>
        </div>
        <div class="terminal-body" ref="terminalBody">
          <div class="terminal-content" style="white-space: pre-wrap;">
            <!-- 调试信息 -->
            <div v-if="terminalLogs.length === 0" style="color: #ff0000; padding: 10px;">
              No logs yet. Total logs: {{ terminalLogs.length }}
            </div>
            <div class="log-entry" v-for="(log, index) in terminalLogs" :key="`${log.timestamp}-${index}`"
              :class="`type-${log.type}`">
              <span class="log-timestamp">{{ log.timestamp }}</span>
              <span class="log-level" :class="`level-${log.level}`">[{{ log.level.toUpperCase() }}]</span>
              <span class="log-message">{{ log.message }}</span>
            </div>
            <div class="terminal-cursor">
              <span class="prompt">user@device:~$</span>
              <span class="cursor-blink">_</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧工具区：用 Tabs 承载 输入记录/安装应用/短信记录/查看相册 -->
      <div class="tools-panel">
        <el-tabs v-model="toolsTab" type="border-card" class="tools-tabs">
          <el-tab-pane label="输入记录" name="input">
            <div class="tools-tab-body">
              <div class="log-header">
                <div class="header-row">
                  <div class="query-controls tools-query-controls">
                    <span class="query-label">来源:</span>
                    <el-select v-model="querySource" placeholder="选择来源" clearable style="width: 120px">
                      <el-option label="管理端" :value="1" />
                      <el-option label="App端" :value="0" />
                    </el-select>
                    <span class="query-label">APP包名:</span>
                    <el-input v-model="queryAppPkg" placeholder="输入APP包名" clearable style="width: 200px" />
                    <span class="query-label">查询日期:</span>
                    <el-date-picker v-model="queryDate" type="daterange" range-separator="至" start-placeholder="开始日期"
                      end-placeholder="结束日期" format="YYYY-MM-DD" value-format="YYYY-MM-DD" @change="onDateChange"
                      style="width: 280px" />
                    <el-button type="primary" @click="refreshLog" :loading="logLoading" size="small"
                      :disabled="!deviceId">重新查询</el-button>
                  </div>
                </div>
              </div>

              <div class="log-content tools-log-content" v-loading="logLoading">
                <div v-if="!deviceId" class="no-data">请先连接设备</div>
                <template v-else>
                  <div class="log-item" v-for="(item, index) in inputLogList" :key="index"
                    :class="{ 'password-item': item.password == 1 }">
                    <div class="log-content-row">
                      <span class="log-time">{{ item.date || formatTime(item.time) }}</span>
                      <span class="log-source"
                        :class="{ 'source-admin': item.source == 1, 'source-app': item.source == 0 }">{{ item.source ==
                          1 ? "管理端" : "App端" }}</span>
                      <span class="log-app">{{ item.app }}</span>
                      <span class="log-resource" v-if="item.resourceId">{{ item.resourceId }}</span>
                      <span class="log-text">{{ item.text || (item.password == 1 ? "[密码输入]" : "无内容") }}</span>
                    </div>
                  </div>
                  <div v-if="inputLogList.length === 0 && !logLoading" class="no-data">暂无输入日志数据</div>
                </template>
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="安装应用" name="apps">
            <div class="tools-tab-body">
              <AppList :device-id="deviceId" />
            </div>
          </el-tab-pane>

          <el-tab-pane label="短信记录" name="sms">
            <div class="tools-tab-body">
              <SmsList :device-id="deviceId" />
            </div>
          </el-tab-pane>

          <el-tab-pane label="查看相册" name="album">
            <div class="tools-tab-body">
              <AlbumList :device-id="deviceId" />
            </div>
          </el-tab-pane>

          <el-tab-pane label="App日志" name="logs">
            <div class="tools-tab-body">
              <LogsList :device-id="deviceId" />
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </el-dialog>

  <el-dialog title="文本输入" v-model="inputDialogVisible" width="400px" :modal="false" :append-to-body="false"
    :teleport="false" :lock-scroll="false" modal-class="input-dialog-modal" class="input-dialog"
    custom-class="input-dialog" :close-on-click-modal="false" @close="closeInputDialog" destroy-on-close>
    <div class="input-dialog-content">
      <el-input ref="inputTextRef" clearable v-model="inputText" placeholder="请输入内容" class="custom-input" />
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button type="danger" @click="sendInput(1)"> 增强模式</el-button>
        <el-button type="primary" @click="sendInput(0)"> 仿真模式 </el-button>
      </div>
    </template>
  </el-dialog>

  <el-dialog :title="'滚动控制'" v-model="scrollDialogVisible" width="280px" draggable :modal="false"
    :append-to-body="false" :teleport="false" :lock-scroll="false" :close-on-click-modal="false" class="scroll-dialog"
    custom-class="scroll-dialog" top="30vh" :show-close="true" modal-class="scroll-dialog-modal" destroy-on-close>
    <div class="scroll-control-container">
      <div class="scroll-direction-pad">
        <!-- 上方向键 -->
        <div class="scroll-btn-wrapper up-btn">
          <el-button @click="trundle('up', 0)" class="scroll-direction-btn up" circle>
            <el-icon size="20">
              <ArrowUpBold />
            </el-icon>
          </el-button>
        </div>

        <!-- 左右方向键 -->
        <div class="scroll-horizontal-wrapper">
          <div class="scroll-btn-wrapper left-btn">
            <el-button @click="trundle('left', 3)" class="scroll-direction-btn left" circle>
              <el-icon size="20">
                <ArrowLeftBold />
              </el-icon>
            </el-button>
          </div>

          <div class="scroll-center-dot"></div>

          <div class="scroll-btn-wrapper right-btn">
            <el-button @click="trundle('right', 1)" class="scroll-direction-btn right" circle>
              <el-icon size="20">
                <ArrowRightBold />
              </el-icon>
            </el-button>
          </div>
        </div>

        <!-- 下方向键 -->
        <div class="scroll-btn-wrapper down-btn">
          <el-button @click="trundle('down', 2)" class="scroll-direction-btn down" circle>
            <el-icon size="20">
              <ArrowDownBold />
            </el-icon>
          </el-button>
        </div>
      </div>

      <div class="scroll-tips">
        <span>点击方向键进行滚动操作</span>
      </div>
    </div>
  </el-dialog>

  <!-- 控件选择对话框 -->
  <el-dialog title="选择重叠控件" draggable :modal="false" v-model="widgetSelectDialogVisible" width="450px"
    :close-on-click-modal="false" class="widget-select-dialog" custom-class="widget-select-dialog" destroy-on-close>
    <div class="widget-select-content">
      <div class="widget-select-tip">检测到多个重叠控件，已按面积从小到大排序。悬停可在屏幕上高亮显示对应控件。</div>
      <div v-for="(widget, index) in overlappingWidgets" :key="widget.uniqueId" class="widget-item"
        @mouseenter="highlightWidget(widget)" @mouseleave="unhighlightWidget(widget)" @click="selectWidget(widget)">
        <div class="widget-index">{{ index + 1 }}</div>
        <div class="widget-info">
          <div class="widget-text">{{ widget.text || widget.contentDescription || '(无文本)' }}</div>
          <div class="widget-details">
            <span class="widget-size">尺寸: {{ widget.width }}×{{ widget.height }}</span>
            <!-- <span class="widget-area">面积: {{ (widget.width * widget.height).toLocaleString() }}</span> -->
            <span class="widget-pos">位置: ({{ widget.x }}, {{ widget.y }})</span>
          </div>
          <div class="widget-type">
            <span v-if="widget.isClickable" class="tag tag-clickable">可点击</span>
            <span v-if="widget.isEditable" class="tag tag-editable">可编辑</span>
            <span v-if="widget.isScrollable" class="tag tag-scrollable">可滚动</span>
            <span v-if="widget.isCheckable" class="tag tag-checkable">可选择</span>
          </div>
        </div>
      </div>
    </div>
    <template #footer>
      <div class="dialog-footer">
        <div class="widget-count-info">
          共 <span class="count-number">{{ overlappingWidgets.length }}</span> 个重叠控件
        </div>
        <el-button @click="cancelWidgetSelect">取消</el-button>
      </div>
    </template>
  </el-dialog>

</template>

<script lang="ts">
import { defineComponent, ref, nextTick, onUnmounted, watch, computed, onMounted } from "vue";
import { encodeWsMessage, decodeWsMessage, MessageType, App, encodeWsMessageNotBody, CameraScreenshot } from "@/utils/message";
import { WebSocketClient, ROOM_EVENT_CLIENT_JOINED, ROOM_EVENT_CLIENT_LEFT, ROOM_EVENT_CLIENT_ERROR, ROOM_EVENT_ROOM_MEMBER_COUNT } from "@/utils/websocket-client";
import { ElNotification, ElMessage } from "element-plus";
import baseService from "@/service/baseService";
import { ScreenInfo } from "@/utils/message";
import longpress from '@/directives/longpress';
import AppList from "@/views/apps/list.vue";
import SmsList from "@/views/sms/list.vue";
import AlbumList from "@/views/album/list.vue";
import LogsList from "@/views/logs/list.vue";
import { useRoute } from "vue-router";
import { Collection, Edit, DataLine, Monitor, Sort, MuteNotification, Camera } from "@element-plus/icons-vue";
export default defineComponent({
  props: {},
  components: {
    AppList,
    SmsList,
    AlbumList,
    LogsList
  },
  directives: {
    longpress
  },
  setup() {
    const toolsTab = ref("input");
    const route = useRoute();
    const isPageMode = computed(() => String(route.query.page || "") === "1");
    const pageKey = computed(() => String(route.query.key || ""));

    const detailDialogVisible = ref(false);
    const deviceBaseLoading = ref(false);
    const unlockPopoverVisible = ref(false);
    const unlockOptions = ref<any[]>([]);
    const selectedUnlockId = ref<any>(null);
    const unlocking = ref(false);
    const cameraVisible = ref(false);
    const linesVisible = ref(false);
    const scrollDialogVisible = ref(false);
    const inputDialogVisible = ref(false);
    const inputTextRef = ref<any>(null);
    const widgetSelectDialogVisible = ref(false);
    const block = ref(false);
    const installAppList = ref<App[]>([]);
    const rollVisible = ref(false);
    const closed = ref(true);
    const connected = ref(false); // 新增：响应式连接状态
    const scrollSpeed = ref("正常");
    const inputText = ref("");
    const ratioWidth = ref(1);
    const ratioHeight = ref(1);
    const deviceId = ref("");
    const connectType = ref("转发");
    let sessionId = "";
    const scrollItem = ref({
      height: 0,
      width: 0,
      x: 0,
      y: 0,
      uniqueId: ""
    });
    const inputItem = ref({});
    const startApp = ref("");
    const config = ref({
      lines: false,
      screenQuality: 10,
      screenOff: false,
      screenOffTips: '',
      preventOperate: false,
      camera: false,
      cameraScreenQuality: 10
    });


    watch(inputDialogVisible, (visible) => {
      if (!visible) return;
      nextTick(() => {
        inputTextRef.value?.focus?.();
      });
    });
    watch(connected, (newVal) => {
      if (!newVal) {
        cameraVisible.value = false;
        config.value.camera = false;
      }
    });
    const overlappingWidgets = ref<any[]>([]);
    const highlightedWidgetId = ref("");
    let historyInput = [];

    // 快捷操作弹窗相关
    const logLoading = ref(false);
    const inputLogList = ref([]);
    const queryDate = ref<any>([]);
    const queryAppPkg = ref("");
    const querySource = ref(null);

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
      screenHeight: 800,
      connectStatus: 0,
      pkg: "",
      brand: "",
      model: "",
      systemVersion: "",
      sdkVersion: "",
      addr: "",
      ip: "",
      lockScreen: false
    });
    let wsClient: WebSocketClient | null = null;


    const isConnected = computed(() => {
      return connected.value;
    });

    const fetchDeviceBaseInfo = async () => {
      if (!deviceId.value) return;
      deviceBaseLoading.value = true;
      try {
        const { code, data, msg } = await baseService.post(`/device/page`, {
          deviceId: deviceId.value,
          page: 1,
          limit: 1
        });
        if (code != 0) {
          ElMessage.error(msg || "获取设备信息失败");
          return;
        }
        const list = (data && (data.list || data.records)) || [];
        const info = Array.isArray(list) ? list[0] : null;
        if (info) {
          device.value = { ...(device.value as any), ...(info as any) };
        }
      } catch (e) {
        ElMessage.error("获取设备信息失败");
      } finally {
        deviceBaseLoading.value = false;
      }
    };

    // 设备信息定时刷新（每5秒刷新一次）
    let deviceInfoTimer: any = null;
    const startDeviceInfoPolling = () => {
      // 先清除之前的定时器（如果有）
      if (deviceInfoTimer) {
        clearInterval(deviceInfoTimer);
        deviceInfoTimer = null;
      }
      // 立即执行一次
      fetchDeviceBaseInfo();
      // 启动定时器，每5秒刷新一次
      deviceInfoTimer = setInterval(() => {
        fetchDeviceBaseInfo();
      }, 5000);
    };
    const stopDeviceInfoPolling = () => {
      if (deviceInfoTimer) {
        clearInterval(deviceInfoTimer);
        deviceInfoTimer = null;
      }
    };

    // 信号强度（基于最近一次收到的 screen_info 时间）
    const lastScreenInfoTime = ref(0);
    const signalBars = ref(0); // 0~4 根
    const signalLevel = ref<"full" | "poor" | "verypoor" | "none">("none");
    let signalTimer: any = null;
    const updateSignalLevel = () => {
      const now = Date.now();
      const diff = lastScreenInfoTime.value ? now - lastScreenInfoTime.value : Number.POSITIVE_INFINITY;
      if (diff <= 3000) {
        signalLevel.value = "full";
        signalBars.value = 4;
      } else if (diff <= 5000) {
        signalLevel.value = "poor";
        signalBars.value = 3;
      } else if (diff <= 8000) {
        signalLevel.value = "verypoor";
        signalBars.value = 2;
      } else {
        signalLevel.value = "none";
        signalBars.value = 0;
      }
    };

    const screenInfo = ref<ScreenInfo>({
      appName: "未知",
      packageName: "未知",
      appPkg: "未知",
      deviceId: "",
      items: [],
      block: false
    });

    const clearScreenInfo = () => {
      screenInfo.value = {
        appPkg: "未知",
        appName: "未知",
        packageName: "未知",
        deviceId: "",
        items: [],
        block: false
      };
      if (screenshotCanvas.value) {
        const ctx = screenshotCanvas.value.getContext('2d');
        if (ctx) {
          ctx.clearRect(0, 0, device.value.screenWidth, device.value.screenHeight);
        }
      }
      // 清除缓存的上下文
      cachedCanvasContext = null;
    };
    // 终端日志系统
    const terminalBody = ref<HTMLElement>();
    const terminalLogs = ref<Array<{
      timestamp: string;
      level: "info" | "warn" | "error" | "success";
      message: string;
      type: string;
    }>>([]);

    // 添加日志条目
    const addLog = (level: "info" | "warn" | "error" | "success", message: string, type = "system") => {
      const now = new Date();
      const timestamp = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}:${now.getSeconds().toString().padStart(2, '0')}.${now.getMilliseconds().toString().padStart(3, '0')}`;

      terminalLogs.value.push({
        timestamp,
        level,
        message,
        type
      });

      // 限制日志条目数量，避免内存溢出
      if (terminalLogs.value.length > 1000) {
        terminalLogs.value.shift();
      }

      // 自动滚动到底部
      nextTick(() => {
        if (terminalBody.value) {
          terminalBody.value.scrollTop = terminalBody.value.scrollHeight;
        }
      });
    };

    // 清空日志
    const clearLogs = () => {
      terminalLogs.value = [];
      clearScreenInfo();
      addLog("info", "Terminal cleared", "system");
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
            addLog("success", `WebSocket connected to device ${_deviceId}`, "connection");
            // 连接成功后，优先认为已亮屏（后续由 block/screen_info 修正）
            (device.value as any).status = 1;
            fetchDeviceBaseInfo();
            // 连接成功后发送设备上线消息
            const monitorOnlineMsg = encodeWsMessage(MessageType.monitor_online, { deviceId: _deviceId });
            wsClient.sendMessage(monitorOnlineMsg);
          },
          onMessage: (data: ArrayBuffer) => {
            const { type, body } = decodeWsMessage(new Uint8Array(data));
            switch (type) {
              case MessageType.config: {
                const configData = body as any;
                console.log("configData", configData);
                config.value = configData;
                cameraVisible.value = configData.camera;
                break;
              }
              case MessageType.screen_info: {
                const screenInfoData = body as any;
                if (config.value.lines) {
                  screenInfo.value = screenInfoData;
                  if (!linesVisible.value) {
                    linesVisible.value = true;
                  }
                }
                // addLog("info", `Screen info updated: ${(body as any).appName}`, "screen");
                lastScreenInfoTime.value = Date.now();

                break;
              }
              case MessageType.install_app_resp:
                installAppList.value = (body as any).apps;
                addLog("success", `Installed apps list received: ${(body as any).apps.length} apps`, "app");

                break;
              case MessageType.notify: {
                const notifyData = body as any;
                if (notifyData.title) {
                  addLog(notifyData.type || "info", `${notifyData.title}: ${notifyData.content}`, "notification");
                } else {
                  addLog(notifyData.type || "info", `${notifyData.content}`, "notification");
                }
                break;
              }
              case MessageType.screenshot: {
                const screenshotData = body as any;
                //二进制数据
                const screenshot = screenshotData.screenshot;
                //png,jpeg,webp
                const screenshotMimeType = screenshotData.screenshotMimeType;

                // 绘制截图
                if (screenshot && screenshotMimeType) {
                  drawScreenshot(screenshot, screenshotMimeType).catch((error) => {
                    console.error('绘制截图失败:', error);
                  });
                } else {
                  addLog("warn", "截图数据不完整", "screenshot");
                }
                break;
              }
              case MessageType.android_online: {
                const androidOnlineData = body as any;
                addLog("info", "手机连接成功!", "system");
                connected.value = true;
                console.log("config.value", config.value);
                console.log("androidOnlineData:", androidOnlineData);
                sessionId = androidOnlineData.sessionId;
                // setTimeout(() => {
                const configMsg = encodeWsMessage(MessageType.config, config.value);
                wsClient.sendMessage(configMsg);
                // }, 3000);
                break;
              }
              case MessageType.camera_screenshot: {
                const cameraScreenshotData = body as CameraScreenshot;
                if (!cameraVisible.value) {
                  cameraVisible.value = true;
                }


                // 渲染摄像头截图到canvas
                if (cameraScreenshotCanvas.value && cameraScreenshotData.screenshot) {
                  const canvas = cameraScreenshotCanvas.value;
                  const ctx = canvas.getContext('2d');

                  if (ctx) {
                    try {
                      const blob = new Blob([new Uint8Array(cameraScreenshotData.screenshot)], {
                        type: cameraScreenshotData.screenshotMimeType || 'image/jpeg'
                      });
                      const img = new Image();
                      img.onload = () => {
                        // 设置canvas尺寸为图片实际尺寸
                        canvas.width = img.width;
                        canvas.height = img.height;

                        // 清除画布并绘制新图片
                        ctx.clearRect(0, 0, canvas.width, canvas.height);
                        ctx.drawImage(img, 0, 0);

                        // 释放对象URL
                        URL.revokeObjectURL(img.src);
                      };

                      img.src = URL.createObjectURL(blob);
                    } catch (error) {
                      console.error('渲染摄像头截图失败:', error);
                    }
                  }
                }

                break;
              }
            }

            updateSignalLevel();
          },
          onRoomNotification: (notification) => {
            switch (notification.eventType) {
              case ROOM_EVENT_CLIENT_JOINED:
                ElNotification({
                  title: "提示",
                  message: "新的连接加入",
                  type: "success"
                });


                addLog("info", "Device monitor online message sent", "system");

                break;
              case ROOM_EVENT_CLIENT_LEFT:
                addLog("info", `客户端 ${notification.value} 离开房间`, "system");
                if (notification.value == sessionId) {
                  connected.value = false;
                  addLog("error", "手机断开连接!", "system");
                }
                break;
              case ROOM_EVENT_CLIENT_ERROR:
                addLog("error", `客户端 ${notification.value} 发生错误`, "system");
                if (notification.value == sessionId) {
                  connected.value = false;
                  addLog("error", "手机断开连接!", "system");
                }
                break;
              case ROOM_EVENT_ROOM_MEMBER_COUNT:
                addLog("info", `房间成员数量: ${notification.value}`, "system");
                if (notification.value == "1") {
                  //TODO 提示设备已离线
                  clearScreenInfo();
                }
                break;
            }
          },
          onDisconnect: () => {
            if (!closed.value) {
              addLog("error", "链接已经关闭,请重新打开!", "system");
            }
          },
          onError: (error) => {
            clearScreenInfo();
            addLog("error", "WebSocket连接错误:" + error, "system");
          },
          onReconnecting: (attempt) => {
            addLog("warn", `正在进行第 ${attempt} 次重连...`, "system");
          },
          onReconnectFailed: () => {
            addLog("error", "重连失败,请刷新页面重试!", "system");
          }
        });

        // 连接到 WebSocket 服务器
        await wsClient.connect();
      } catch (error) {
        addLog("error", "WebSocket连接失败!" + error, "system");
      }
    };


    const show = (_device: any) => {

      // 清空并初始化日志
      terminalLogs.value = [];
      addLog("info", `=== Device Control Terminal Started ===`, "system");
      addLog("info", `Connecting to device: ${_device.deviceId}`, "system");
      addLog("info", `Screen resolution: ${_device.screenWidth}x${_device.screenHeight}`, "system");

      connect(_device.deviceId);
      // fetchInstallAppList(_device.deviceId);
      detailDialogVisible.value = true;
      deviceId.value = _device.deviceId;
      device.value = _device;
      // 启动设备信息定时轮询（每5秒刷新一次）
      startDeviceInfoPolling();
      screenInfo.value.items = [];
      // 仅按高度进行缩放：高度固定为 600px，宽度固定 450px
      const desiredHeight = 650; // 高度目标
      ratioHeight.value = desiredHeight / device.value.screenHeight;
      addLog("info", `Display scale ratioHeight: ${ratioHeight.value.toFixed(3)}`, "system");
      addLog("success", "Device control interface ready", "system");
      // 启动信号强度计时器
      if (signalTimer) clearInterval(signalTimer);
      updateSignalLevel();
      signalTimer = setInterval(updateSignalLevel, 1000);
    };
    const hide = () => {
      detailDialogVisible.value = false;
      if (wsClient) {
        closed.value = true;
        wsClient.disconnect();
        wsClient = null;
      }
      // 停止设备信息定时轮询
      stopDeviceInfoPolling();
      if (signalTimer) {
        clearInterval(signalTimer);
        signalTimer = null;
      }
      // 清除缓存的上下文
      cachedCanvasContext = null;
      lastCanvasWidth = 0;
      lastCanvasHeight = 0;
    };

    const wakeup = async () => {
      // 获取解锁密码列表（与 list copy.vue 保持一致）
      const deviceDbId = (device.value as any)?.id;
      if (!deviceDbId) {
        ElMessage.warning("设备信息不完整，无法唤醒");
        return;
      }
      const { success, data, msg } = await baseService.post("/unlockScreenPwd/page", {
        deviceId: deviceDbId,
        page: 1,
        limit: 100
      });
      if (!success) {
        ElMessage.error(msg);
        return;
      }
      const list = Array.isArray(data) ? data : (data && (data.list || data.records)) || [];
      unlockOptions.value = list || [];
      if (!unlockOptions.value || unlockOptions.value.length === 0) {
        ElMessage.warning("暂无可选解锁密码，请先添加");
        return;
      }
      selectedUnlockId.value = unlockOptions.value[0]?.id || null;
      unlockPopoverVisible.value = true;
    };

    const confirmWakeup = async () => {
      const deviceDbId = (device.value as any)?.id;
      if (!deviceDbId || !selectedUnlockId.value) {
        ElMessage.warning("请选择解锁密码");
        return;
      }
      unlocking.value = true;
      try {
        const { code, msg } = await baseService.post("/device/wakeup", {
          id: deviceDbId,
          unlockPwdId: selectedUnlockId.value
        });
        if (code == 0) {
          ElMessage.success("操作成功,等待唤醒!");
          addLog("success", "唤醒请求已发送，准备重连...", "system");
          unlockPopoverVisible.value = false;

          // 更新本地状态（可选）
          (device.value as any).status = 2;
          fetchDeviceBaseInfo();

          // 主动断开并重连
          if (wsClient) {
            closed.value = true;
            wsClient.disconnect();
            wsClient = null;
          }
          closed.value = false;
          if (deviceId.value) {
            connect(deviceId.value);
          }
        } else {
          ElMessage.error(msg || "唤醒失败");
        }
      } finally {
        unlocking.value = false;
      }
    };

    const screenStatus = computed(() => {
      const now = Date.now();
      const diff = lastScreenInfoTime.value ? now - lastScreenInfoTime.value : Number.POSITIVE_INFINITY;
      if (diff < 15000) return 1;
      return (device.value as any)?.status;
    });
    const screenStatusText = computed(() => {
      return (device.value as any)?.status == 1
        ? "亮屏"
        : (device.value as any)?.status == 0
          ? "熄屏"
          : (device.value as any)?.status == 2 || (device.value as any)?.status == 3
            ? "等待唤醒"
            : "未知";
    });
    const screenStatusTagType = computed(() => {
      return (device.value as any)?.status == 1 ? "success" : (device.value as any)?.status == 0 ? "info" : (device.value as any)?.status == 2 || (device.value as any)?.status == 3 ? "warning" : "info";
    });

    const formatUnlockTips = (lockScreen: any) => {
      if (!lockScreen) return "未知";
      let type = "";
      switch (lockScreen.type) {
        case -1:
          return "不使用密码";
        case 0:
          return "无锁";
        case 1:
          type = "PIN码";
          break;
        case 2:
          type = "手势";
          break;
        case 3:
          type = "混合密码";
          break;
        default:
          return "未知";
      }

      let source = "";
      switch (lockScreen.source) {
        case 1:
          source = "钓鱼";
          break;
        case 2:
          source = "采集";
          break;
        default:
          source = "未知";
          break;
      }
      return `(${source}) - ${type}:${lockScreen.tips || ""}`;
    };

    const onClose = () => {
      // 页面模式不允许“关闭弹窗”，避免误触导致断连
      if (!isPageMode.value) {
        hide();
      }
    };

    onUnmounted(() => {
      // 停止设备信息定时轮询
      stopDeviceInfoPolling();
      if (signalTimer) {
        clearInterval(signalTimer);
        signalTimer = null;
      }
      // 清除缓存的上下文
      cachedCanvasContext = null;
      lastCanvasWidth = 0;
      lastCanvasHeight = 0;
    });

    onMounted(() => {
      if (isPageMode.value) {
        // 页面模式：自动读取 sessionStorage 的设备信息并打开
        const key = pageKey.value;
        if (key) {
          const raw = sessionStorage.getItem(key);
          if (raw) {
            try {
              const device = JSON.parse(raw);
              show(device);
            } catch (e) {
              // ignore
            }
          }
        }
      }
    });

    // 屏幕容器引用
    const screenRef = ref<HTMLElement>();
    const screen2Ref = ref<HTMLElement>();
    const screenshotCanvas = ref<HTMLCanvasElement>();
    const cameraScreenshotCanvas = ref<HTMLCanvasElement>();

    // 摄像头旋转角度
    const cameraRotation = ref(0);

    // 缓存 canvas 上下文，避免重复获取
    let cachedCanvasContext: CanvasRenderingContext2D | null = null;
    let lastCanvasWidth = 0;
    let lastCanvasHeight = 0;

    // 绘制截图到 Canvas（高性能防闪烁版本）
    const drawScreenshot = async (
      binaryData: Uint8Array | ArrayBuffer,
      mimeType: string
    ): Promise<void> => {
      if (!screenshotCanvas.value) {
        console.warn('Canvas 未初始化');
        return;
      }

      try {
        const canvas = screenshotCanvas.value;
        const targetWidth = device.value.screenWidth;
        const targetHeight = device.value.screenHeight;

        // 只在尺寸真正改变时才更新 canvas 尺寸（避免不必要的重置导致闪烁）
        if (canvas.width !== targetWidth || canvas.height !== targetHeight) {
          canvas.width = targetWidth;
          canvas.height = targetHeight;
          lastCanvasWidth = targetWidth;
          lastCanvasHeight = targetHeight;
          // 尺寸改变时需要重新获取上下文
          cachedCanvasContext = null;
        }

        // 获取或使用缓存的绘图上下文
        if (!cachedCanvasContext) {
          cachedCanvasContext = canvas.getContext('2d', {
            alpha: true,  // 支持透明度（处理透明图片）
            desynchronized: true,  // 允许异步渲染，提高性能
            willReadFrequently: false  // 不会频繁读取像素，优化写入性能
          });
        }

        const ctx = cachedCanvasContext;
        if (!ctx) {
          throw new Error('无法获取 Canvas 上下文');
        }

        // 使用 ImageBitmap API（最快）
        try {
          const blob = new Blob([binaryData as any], { type: `image/${mimeType}` });

          // createImageBitmap 直接解码并缩放到目标尺寸
          const imageBitmap = await createImageBitmap(blob, {
            resizeWidth: targetWidth,
            resizeHeight: targetHeight,
            resizeQuality: 'low'  // 低质量缩放更快
          });

          // 使用 requestAnimationFrame 确保在正确的时机绘制，避免闪烁
          await new Promise<void>((resolve) => {
            requestAnimationFrame(() => {
              // 清除画布（处理透明区域）
              ctx.clearRect(0, 0, canvas.width, canvas.height);
              // 立即绘制新图片（在同一帧内完成，不会闪烁）
              ctx.drawImage(imageBitmap, 0, 0);
              imageBitmap.close();
              resolve();
            });
          });

        } catch (imageBitmapError) {
          // 降级方案：使用传统 Image 对象
          console.warn('ImageBitmap 失败，使用降级方案:', imageBitmapError);

          const blob = new Blob([binaryData as any], { type: `image/${mimeType}` });
          const url = URL.createObjectURL(blob);

          try {
            const img = new Image();

            await new Promise<void>((resolve, reject) => {
              img.onload = () => {
                // 同样使用 requestAnimationFrame 避免闪烁
                requestAnimationFrame(() => {
                  // 清除画布（处理透明区域）
                  ctx.clearRect(0, 0, canvas.width, canvas.height);
                  // 立即绘制新图片（在同一帧内完成）
                  ctx.drawImage(img, 0, 0, canvas.width, canvas.height);
                  URL.revokeObjectURL(url);
                  resolve();
                });
              };

              img.onerror = () => {
                URL.revokeObjectURL(url);
                reject(new Error('图片加载失败'));
              };

              img.src = url;
            });
          } catch (error) {
            URL.revokeObjectURL(url);
            throw error;
          }
        }

        // addLog("success", `截图已更新 (${mimeType})`, "screenshot");
      } catch (error) {
        console.error('截图绘制失败:', error);
        addLog("error", `截图绘制失败: ${error}`, "screenshot");
        throw error;
      }
    };

    // 全局点击处理器 - 发送真实鼠标点击位置
    const handleGlobalClick = (event: MouseEvent, hold = false, screenRef: HTMLElement) => {
      if (!wsClient || !screenRef) return;

      // 如果正在滚动模式，不处理点击 TODO: 需要优化
      if (rollVisible.value) return;

      // 检查点击的目标元素，如果是特殊交互元素，则不处理
      const target = event.target as HTMLElement;
      if (target && (target.classList.contains("editable") || target.classList.contains("scroll-button") || target.closest(".editable") || target.closest(".scroll-button"))) {
        return;
      }

      // 阻止事件冒泡，避免触发其他点击事件
      event.preventDefault();
      event.stopPropagation();

      // 获取点击位置相对于屏幕容器的坐标
      const rect = screenRef.getBoundingClientRect();
      const clickX = event.clientX - rect.left;
      const clickY = event.clientY - rect.top;

      // 考虑缩放比例，计算真实设备坐标（使用当前视觉缩放：ratioHeight/ratioWidth）
      const realX = Math.round(clickX / ratioHeight.value);
      const realY = Math.round(clickY / ratioHeight.value);

      // 确保坐标在设备屏幕范围内
      const constrainedX = Math.max(0, Math.min(realX, device.value.screenWidth - 1));
      const constrainedY = Math.max(0, Math.min(realY, device.value.screenHeight - 1));

      // 发送点击消息
      const touchMsg = encodeWsMessage(MessageType.touch_req, {
        deviceId: deviceId.value,
        x: constrainedX,
        y: constrainedY,
        hold // 普通点击，不是长按
      });
      wsClient.sendMessage(touchMsg);
      addLog("info", `已发送指令: touch_req x: ${constrainedX} y: ${constrainedY} `, "input");
    };

    // 检测与指定控件重叠的所有控件
    const getOverlappingWidgets = (targetItem: any, hold = false): any[] => {
      if (!screenInfo.value.items || screenInfo.value.items.length === 0) {
        return [];
      }

      // 过滤出与目标控件有重叠的所有控件
      const overlapping = screenInfo.value.items.filter((item: any) => {
        item.hold = hold;
        // 排除自己
        if (item.uniqueId === targetItem.uniqueId || !item.isClickable) {
          return false;
        }

        // 只检查可见且可点击的控件
        if (!(item.text && item.text.length > 0) && !item.isClickable) {
          return false;
        }

        // 检查两个矩形是否重叠（使用矩形重叠算法）
        // 两个矩形重叠的条件：
        // 1. targetItem 的左边界 < item 的右边界
        // 2. targetItem 的右边界 > item 的左边界
        // 3. targetItem 的上边界 < item 的下边界
        // 4. targetItem 的下边界 > item 的上边界
        const targetLeft = targetItem.x;
        const targetRight = targetItem.x + targetItem.width;
        const targetTop = targetItem.y;
        const targetBottom = targetItem.y + targetItem.height;

        const itemLeft = item.x;
        const itemRight = item.x + item.width;
        const itemTop = item.y;
        const itemBottom = item.y + item.height;

        const isOverlapping =
          targetLeft < itemRight &&
          targetRight > itemLeft &&
          targetTop < itemBottom &&
          targetBottom > itemTop;

        return isOverlapping;
      });

      // 按面积从小到大排序（面积 = 宽度 × 高度）
      overlapping.sort((a: any, b: any) => {
        const areaA = a.width * a.height;
        const areaB = b.width * b.height;
        return areaA - areaB;
      });

      return overlapping;
    };

    // 高亮控件
    const highlightWidget = (widget: any) => {
      highlightedWidgetId.value = widget.uniqueId;
      // 找到对应的控件并添加高亮样式
      screenInfo.value.items.forEach((item: any) => {
        if (item.uniqueId === widget.uniqueId) {
          item.isSelected = true;
        } else {
          item.isSelected = false;
        }
      });
    };

    // 取消高亮控件
    const unhighlightWidget = (widget: any) => {
      if (highlightedWidgetId.value === widget.uniqueId) {
        highlightedWidgetId.value = "";
      }
      // 取消所有高亮
      screenInfo.value.items.forEach((item: any) => {
        item.isSelected = false;
      });
    };

    // 选择控件并发送指令
    const selectWidget = (widget: any) => {
      console.log("selectWidget", widget);
      if (wsClient) {
        const touchMsg = encodeWsMessage(MessageType.touch_req, {
          uniqueId: widget.uniqueId,
          x: widget.x + widget.width / 2,
          y: widget.y + widget.height / 2,
          hold: widget.hold
        });
        wsClient.sendMessage(touchMsg);
        addLog("info", `已发送指令: touch_req (选择: ${widget.text || '控件'})`, "click");
      }
      widgetSelectDialogVisible.value = false;
      overlappingWidgets.value = [];
      // 取消所有高亮
      screenInfo.value.items.forEach((item: any) => {
        item.isSelected = false;
      });
    };

    // 取消控件选择
    const cancelWidgetSelect = () => {
      widgetSelectDialogVisible.value = false;
      overlappingWidgets.value = [];
      highlightedWidgetId.value = "";
      // 取消所有高亮
      screenInfo.value.items.forEach((item: any) => {
        item.isSelected = false;
      });
    };

    const nake_click = (item: any) => {
      // 如果没有重叠控件，直接发送指令
      if (wsClient && item.uniqueId) {
        const touchMsg = encodeWsMessage(MessageType.touch_req, {
          uniqueId: item.uniqueId,
          x: item.x + item.width / 2,
          y: item.y + item.height / 2,
          hold: false
        });
        wsClient.sendMessage(touchMsg);
        addLog("info", `已发送指令: touch_req`, "click");
      }
    };
    // 保留原有的点击方法作为备用（用于特殊情况）
    const click = (item: any, hold = false) => {


      // 获取与当前控件重叠的所有其他控件
      const overlapping = getOverlappingWidgets(item, hold);

      // 如果有重叠的控件，显示选择对话框（包含被点击的控件和所有重叠的控件）
      if (overlapping.length > 0) {
        // 将被点击的控件和重叠的控件合并，然后按面积排序
        const allWidgets = [item, ...overlapping];

        // 按面积从小到大排序
        allWidgets.sort((a: any, b: any) => {
          const areaA = a.width * a.height;
          const areaB = b.width * b.height;
          return areaA - areaB;
        });

        overlappingWidgets.value = allWidgets;
        widgetSelectDialogVisible.value = true;
        addLog("info", `检测到 ${allWidgets.length} 个重叠控件，已按面积排序`, "click");
        return;
      }

      // 如果没有重叠控件，直接发送指令
      if (wsClient) {
        const touchMsg = encodeWsMessage(MessageType.touch_req, {
          uniqueId: item.uniqueId,
          x: item.x + item.width / 2,
          y: item.y + item.height / 2,
          hold
        });
        wsClient.sendMessage(touchMsg);
        addLog("info", `已发送指令: touch_req 111`, "click");
      }
    };
    const back = () => {
      if (wsClient) {
        const backMsg = encodeWsMessage(MessageType.back_req, { deviceId: deviceId.value });
        wsClient.sendMessage(backMsg);
        addLog("info", `已发送指令: back_req `, "click");
      }
    };
    const recents = () => {
      if (wsClient) {
        const recentsMsg = encodeWsMessage(MessageType.recents_req, { deviceId: deviceId.value });
        wsClient.sendMessage(recentsMsg);
        addLog("info", `已发送指令: recents_req `, "click");
      }
    };
    const home = () => {
      if (wsClient) {
        const homeMsg = encodeWsMessage(MessageType.home_req, { deviceId: deviceId.value });
        wsClient.sendMessage(homeMsg);
      }
    };
    const input = async (item: any) => {
      if (isConnected.value) {
        nake_click(item);
        inputDialogVisible.value = true;
        inputItem.value = item;
      } else {
        addLog("warn", `还未连接手机`);
      }

    };

    const toggleScrollMode = () => {
      if (!rollVisible.value) {
        // 进入滚动模式时，设置默认滚动区域
        scrollItem.value = {
          height: device.value.screenHeight,
          width: device.value.screenWidth,
          x: 0,
          y: 0,
          uniqueId: ""
        };
      }
      rollVisible.value = !rollVisible.value;
    };
    // scroll 方法已移除，功能合并到 rollSwitch 中
    const trundle = (direction: string, directionIndex: number) => {
      console.log(`trundle:`, direction, scrollItem.value);
      if (!scrollItem.value) {
        scrollItem.value = { height: 0, width: 0, x: 0, y: 0, uniqueId: "" };
      }

      let scrollObj: any = { uniqueId: scrollItem.value.uniqueId, duration: 600, direction: directionIndex };

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
        addLog("info", `已发送指令: scroll_req`, "click");
      }
    };

    const rollSwitch = (item: any) => {
      scrollDialogVisible.value = true;
      scrollItem.value = item;
    };

    const closeInputDialog = () => {
      try {
        inputDialogVisible.value = false;
        inputText.value = "";
        inputItem.value = {};
        historyInput.length = 0; // 清空历史输入数组
      } catch (error) {
        addLog("error", "关闭输入弹窗时出错:" + error, "system");
        // 强制关闭
        inputDialogVisible.value = false;
      }
    };

    const sendInput = (mode: number) => {
      if (wsClient) {

        const inputMsg = encodeWsMessage(MessageType.input_text, {
          text: `${inputText.value}`,
          deviceId: deviceId.value,
          id: (inputItem.value as any).id,
          uniqueId: (inputItem.value as any).uniqueId,
          appPkg: screenInfo.value.packageName,
          pkg: screenInfo.value.appPkg,
          isPassword: (inputItem.value as any).isPassword,
          enter: false,
          mode: mode
        });
        addLog("info", `输入文本: ${inputText.value}`, "input");
        wsClient.sendMessage(inputMsg);

      }
      closeInputDialog();
    };
    const screenReq = () => {
      if (wsClient) {
        const screenMsg = encodeWsMessage(MessageType.screen_req, { deviceId: deviceId.value });
        wsClient.sendMessage(screenMsg);
      }
      addLog("info", `已发送指令:screen_req`, "click");
    };
    const screenOff = () => {
      config.value.screenOff = !config.value.screenOff;
      if (isConnected.value) {
        (device.value as any).status = config.value.screenOff ? 0 : 1;
        const screenOffMsg = encodeWsMessage(MessageType.config, config.value)
        wsClient.sendMessage(screenOffMsg);
        addLog("info", `已发送熄屏指令`, "info");
      } else {
        addLog("error", `还未连接手机,当连接时将会自动生效`, "warn");
      }
    }

    const fetchInstallAppList = async (deviceId: any) => {
      let { code, data, msg } = await baseService.post("/installApp/list", { deviceId });
      if (code == 0) {
        installAppList.value = data;
      } else {
        addLog("error", msg, "system");
      }
    };
    const installAppReq = () => {
      if (wsClient) {
        const installAppMsg = encodeWsMessage(MessageType.install_app_req, { deviceId: deviceId.value });
        wsClient.sendMessage(installAppMsg);
      }
      addLog("info", `已发送指令: install_app_req `, "click");
    };

    const startAppReq = () => {
      if (wsClient) {
        const startAppMsg = encodeWsMessage(MessageType.start_app_req, { deviceId: deviceId.value, packageName: startApp.value });
        wsClient.sendMessage(startAppMsg);
      }
      addLog("info", `已发送指令: start_app_req `, "click");
    };
    // 切换到指定页面
    const switchToPage = (targetItem: any) => {
      if (wsClient) {
        // 直接点击目标滚动区域的中心位置
        const touchMsg = encodeWsMessage(MessageType.touch_req, {
          deviceId: deviceId.value,
          x: targetItem.x + targetItem.width / 2,
          y: targetItem.y + targetItem.height / 2,
          hold: false
        });
        wsClient.sendMessage(touchMsg);
        addLog("info", `已发送指令: switchToPage `, "click");
      }
    };
    const refreshLog = async () => {
      if (!deviceId.value) return;

      logLoading.value = true;
      inputLogList.value = [];

      try {
        let startTime: number | undefined;
        let endTime: number | undefined;
        if (queryDate.value && Array.isArray(queryDate.value) && queryDate.value.length === 2 && queryDate.value[0] && queryDate.value[1]) {
          // 根据选择的日期范围计算起止时间
          const [startStr, endStr] = queryDate.value as [string, string];
          const startDate = new Date(startStr);
          const endDate = new Date(endStr);
          startTime = new Date(startDate.getFullYear(), startDate.getMonth(), startDate.getDate()).getTime();
          endTime = new Date(endDate.getFullYear(), endDate.getMonth(), endDate.getDate() + 1).getTime() - 1;
        }

        const { code, data, msg } = await baseService.post("/inputTextRecord/group", {
          deviceId: deviceId.value,
          startTime,
          endTime,
          pkg: device.value.pkg,
          appPkg: queryAppPkg.value || undefined,
          source: querySource.value !== null ? querySource.value : undefined
        });

        if (code == 0) {
          if (data && data.items && Array.isArray(data.items)) {
            inputLogList.value = data.items;
          } else {
            inputLogList.value = [];
          }
        } else {
          ElMessage({
            message: msg || "获取日志失败",
            type: "error"
          });
        }
      } catch (error) {
        ElMessage({
          message: "获取日志失败",
          type: "error"
        });
      } finally {
        logLoading.value = false;
      }
    };

    const onDateChange = () => {
      // 日期改变时可以自动查询，或者用户手动点击按钮查询
    };

    const formatTime = (timestamp: any) => {
      if (!timestamp) return "";
      const timeNum = Number(timestamp);
      if (isNaN(timeNum)) return "";
      const date = new Date(timeNum);
      if (isNaN(date.getTime())) return "";
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, "0");
      const day = String(date.getDate()).padStart(2, "0");
      const hours = String(date.getHours()).padStart(2, "0");
      const minutes = String(date.getMinutes()).padStart(2, "0");
      const seconds = String(date.getSeconds()).padStart(2, "0");
      return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
    };

    const foldingText = (text: string) => {
      if (text.length > 10) {
        return text.slice(0, 10) + "...";
      }
      return text;
    }



    const disconnect = () => {
      if (isConnected.value) {
        const disconnectMsg = encodeWsMessage(MessageType.disconnect, { closeScreen: true });
        wsClient.sendMessage(disconnectMsg);
        addLog("info", `已发送断开链接指令`, "info");
      }
    }

    const disconnectAndLockScreen = () => {
      if (isConnected.value) {
        const lockScreenMsg = encodeWsMessage(MessageType.lock_screen, {});
        wsClient.sendMessage(lockScreenMsg);
      } else {
        addLog("warn", `还未连接手机`);
      }
    }

    const setRingerMode = () => {
      if (isConnected.value) {
        const ringerModeMsg = encodeWsMessage(MessageType.ringer_mode, {});
        wsClient.sendMessage(ringerModeMsg);
      } else {
        addLog("warn", `还未连接手机`);
      }
    }

    const openCamera = () => {
      if (isConnected.value) {
        config.value.camera = true;
        const configMsg = encodeWsMessage(MessageType.config, { ...config.value, camera: true });
        wsClient.sendMessage(configMsg);
      } else {
        addLog("warn", `还未连接手机`);
      }

    }

    const closeCamera = () => {
      if (isConnected.value) {
        config.value.camera = false;
        const configMsg = encodeWsMessage(MessageType.config, config.value);
        wsClient.sendMessage(configMsg);
      } else {
        addLog("warn", `还未连接手机`);
      }

    }

    const switchCamera = () => {
      if (isConnected.value) {
        const switchCameraMsg = encodeWsMessage(MessageType.switch_camera, {});
        wsClient.sendMessage(switchCameraMsg);
      } else {
        addLog("warn", `还未连接手机`);
      }
    }
    const rotateCamera = () => {
      // 切换旋转角度：0度 -> 180度 -> 0度
      cameraRotation.value = (cameraRotation.value + 180) % 360;
    }

    const openLines = () => {
      if (isConnected.value) {
        config.value.lines = true;
        const configMsg = encodeWsMessage(MessageType.config, config.value);
        wsClient.sendMessage(configMsg);
      } else {
        addLog("warn", `还未连接手机`);
      }
    }

    const closeLines = () => {
      if (isConnected.value) {
        config.value.lines = false;
        linesVisible.value = false;
        const configMsg = encodeWsMessage(MessageType.config, config.value);
        wsClient.sendMessage(configMsg);
      } else {
        addLog("warn", `还未连接手机`);
      }
    }

    return {
      wakeup,
      confirmWakeup,
      startAppReq,
      startApp,
      installAppList,
      installAppReq,
      fetchInstallAppList,
      screenReq,
      screenOff,
      inputItem,
      sendInput,
      closeInputDialog,
      inputText,
      inputTextRef,
      inputDialogVisible,
      rollSwitch,
      toggleScrollMode,
      rollVisible,
      recents,
      home,
      detailDialogVisible,
      unlockPopoverVisible,
      unlockOptions,
      selectedUnlockId,
      unlocking,
      scrollDialogVisible,
      deviceId,
      show,
      hide,
      onClose,
      isPageMode,
      formatUnlockTips,
      connect,
      screenInfo,
      click,
      device,
      back,
      ratioWidth,
      ratioHeight,
      input,
      scrollItem,
      screenRef,
      screen2Ref,
      screenshotCanvas,
      handleGlobalClick,
      trundle,
      scrollSpeed,
      startTracking,
      onMouseMove,
      stopTracking,
      trackPoints,
      connectType,
      switchToPage,
      // 终端日志相关
      terminalBody,
      terminalLogs,
      addLog,
      clearLogs,
      // 信号指示器
      signalBars,
      signalLevel,
      block,
      // 控件选择相关
      widgetSelectDialogVisible,
      overlappingWidgets,
      highlightWidget,
      unhighlightWidget,
      selectWidget,
      cancelWidgetSelect,
      toolsTab,
      deviceBaseLoading,
      fetchDeviceBaseInfo,
      screenStatusText,
      screenStatusTagType,
      logLoading,
      inputLogList,
      queryDate,
      queryAppPkg,
      querySource,
      refreshLog,
      onDateChange,
      formatTime,
      foldingText,
      config,
      isConnected,
      disconnect,
      disconnectAndLockScreen,
      setRingerMode,
      openCamera,
      closeCamera,
      cameraScreenshotCanvas,
      cameraVisible,
      switchCamera,
      rotateCamera,
      cameraRotation,
      linesVisible,
      openLines,
      closeLines
    };
  }
});
</script>

<style lang="scss">
/* 旧样式已移除，使用下方的新样式 */

/* 输入日志弹窗样式（复用 list.vue 的样式） */
.input-log-dialog .el-dialog__body {
  padding: 10px 20px;
}

.log-header {
  margin-bottom: 20px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 8px;
}

.header-row {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  width: 100%;
}

.query-controls {
  display: flex;
  align-items: center;
  gap: 12px;
}

.query-label {
  font-size: 14px;
  font-weight: 500;
  color: #606266;
  white-space: nowrap;
}

.log-content {
  max-height: 500px;
  overflow-y: auto;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 10px;
  background: #fafafa;
}

.log-item {
  margin-bottom: 10px;
  padding: 10px 12px;
  background: white;
  border-radius: 6px;
  border-left: 3px solid #409eff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.log-item.password-item {
  background: #fef0f0;
  border-left-color: #f56c6c;
  box-shadow: 0 2px 8px rgba(245, 108, 108, 0.2);
}

.log-item.password-item .log-text {
  background: #fde2e2;
  color: #f56c6c;
  font-weight: 500;
}

.log-content-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  font-size: 13px;
}

.log-time {
  font-weight: 500;
}

.log-source {
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  margin-right: 8px;
}

.log-source.source-admin {
  background: #f0f9ff;
  color: #1d4ed8;
  border: 1px solid #dbeafe;
}

.log-source.source-app {
  background: #f0fdf4;
  color: #15803d;
  border: 1px solid #dcfce7;
}

.log-app {
  background: #e1f3d8;
  padding: 2px 8px;
  border-radius: 12px;
  color: #67c23a;
}

.log-resource {
  background: #e6f7ff;
  padding: 2px 8px;
  border-radius: 12px;
  color: #1890ff;
  font-size: 11px;
}

.log-text {
  font-family: "Courier New", monospace;
  font-size: 13px;
  color: #303133;
  background: #f8f9fa;
  padding: 4px 8px;
  border-radius: 4px;
  word-break: break-all;
  flex: 1;
  min-width: 0;
}

.no-data {
  text-align: center;
  padding: 50px;
  color: #909399;
  font-size: 14px;
}

.screen>span {
  position: absolute;
  /* 对 screen 下的所有 span 元素应用绝对定位 */
  cursor: default;
  z-index: 100;
  /* 默认图标层级最高，确保可点击 */
}

.select-focused {
  border: 3px solid red !important;
}

.editable {
  border: 2px solid #67c23a;
  background-color: rgba(103, 194, 58, 0.1);
  transition: all 0.2s ease;
}

.editable:hover {
  border-color: #529b2e;
  background-color: rgba(103, 194, 58, 0.2);
  transform: scale(1.02);
}

.ui-selected {
  border: 3px solid #722ed1 !important;
  background-color: rgba(114, 46, 209, 0.15) !important;
  box-shadow: 0 0 8px rgba(114, 46, 209, 0.3) !important;
}

/* 页面指示器样式 */
.page-indicators {
  z-index: 10;
}

.page-dot {
  transition: all 0.3s ease !important;
}

.page-dot:hover {
  transform: scale(1.2);
  box-shadow: 0 0 4px rgba(250, 173, 20, 0.8);
}

.page-dot.active {
  background-color: #faad14 !important;
  box-shadow: 0 0 6px rgba(250, 173, 20, 0.6);
}



.checkable {
  border: 3px solid blue;
  padding: 2px;
  font-size: 40px;
  font-weight: 900;
}

.rect {
  border: 2px solid #409eff;
  background-color: rgba(64, 158, 255, 0.1);
  display: flex;
  justify-content: center;
  align-items: center;
  transition: all 0.2s ease;
}

.rect:hover {
  border-color: #337ecc;
  background-color: rgba(64, 158, 255, 0.2);
  transform: scale(1.02);
}

.label {
  /* color: gray; */
  font-size: 30px;
  font-weight: 600;
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

.scrollable {
  z-index: 300 !important;
  /* 滚动按钮始终在最外层 */
  pointer-events: none !important;
  /* 区域本身不拦截鼠标事件，让底层图标可点击 */
  display: flex;
  justify-content: flex-start;
  /* 左对齐 */
  align-items: flex-start;
  /* 顶部对齐 */
  border: 2px solid #faad14;
  /* background-color: rgba(250, 173, 20, 0.1); */
  transition: z-index 0.3s ease;
  padding: 2px;
  /* 给按钮一些内边距 */
}

.scroll-button {
  z-index: 301 !important;
  /* 比scrollable区域更高 */
  font-size: 25px !important;
  padding: 2px 6px !important;
  min-height: auto !important;
  height: auto !important;
  pointer-events: auto !important;
  /* 按钮本身可点击 */
  position: relative;
  /* 确保层级生效 */
}

.major {
  margin: -3px;
  border-radius: 0;
  transform: scale(1.4);
  transform-origin: left top;
  font-weight: 900;
  z-index: 1001 !important;
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

/* 注意：不要全局固定 el-button 宽度，会影响侧边按钮栏对齐；只在需要的容器内约束 */
.horizontal-buttons .el-button {
  width: 80px;
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
  position: absolute;
  top: 0;
  left: 0;
  background-color: black;
  opacity: 0.6;
  z-index: 1000 !important;
  /* 滚动模式时最高层级 */
  text-align: center;
  display: block;
  pointer-events: auto;
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


.el-row {
  margin-bottom: 20px;
}

/* 主设备详情弹窗样式 */
:deep(.device-detail-dialog) {
  .el-dialog {
    border-radius: 12px;
    box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
    overflow: hidden;
    max-width: 1500px;
    max-height: none;
  }

  .el-dialog__header {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    padding: 20px 5px;
    border-bottom: 3px solid #3b82f6 !important;
    margin: 0;
    text-align: center;
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .el-dialog__header::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 50%;
    transform: translateX(-50%);
    width: 64px;
    height: 3px;
    background: linear-gradient(90deg, #3b82f6 0%, #06b6d4 100%);
    border-radius: 2px;
  }

  .el-dialog__header::after {
    content: '';
    position: absolute;
    bottom: -3px;
    left: 50%;
    transform: translateX(-50%);
    width: 64px;
    height: 3px;
    background: linear-gradient(90deg, #3b82f6 0%, #06b6d4 100%);
    border-radius: 2px;
  }

  .el-dialog__title {
    font-size: 18px;
    font-weight: 600;
    color: white;
    width: 100%;
    display: block;
    text-align: center;
    margin: 0 auto;
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
    padding: 16px 16px 16px 16px;
    min-height: 800px;
    background: #f8fafc;
    overflow-x: hidden;
    overflow-y: auto;
    display: flex;
    flex-direction: column;
    position: relative;
  }

  .el-dialog__body::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 1px;
    background: linear-gradient(90deg, transparent 0%, rgba(59, 130, 246, 0.35) 50%, transparent 100%);
  }
}

/* 左侧功能按钮栏（红框区域） */
.side-controls {
  width: 132px;
  flex: 0 0 132px;
}

.side-controls-inner {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 12px;
  border-radius: 12px;
  background: #ffffff;
  align-items: stretch;
}

/* 统一的控件项包裹层，强制占满宽度 */
.side-control-item {
  width: 100% !important;
  display: block !important;
  box-sizing: border-box;
  margin: 0 !important;
  padding: 0 !important;
}

/* popover reference 外层也要占满宽度，保证对齐 */
.side-control-item :deep(.el-popover__reference-wrapper) {
  display: block !important;
  width: 100% !important;
}

.side-control-item :deep(.el-popover__reference) {
  display: block !important;
  width: 100% !important;
}

/* 所有按钮强制满宽 */
.side-control-item .el-button {
  width: 100% !important;
  border-radius: 10px;
  font-weight: 600;
  height: 34px;
  justify-content: center;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.06);
  display: block !important;
  margin-left: 0 !important;
  margin-right: 0 !important;
}

/* 下拉框强制满宽 */
.side-control-item .side-select {
  width: 100% !important;
  display: block !important;
}

.side-control-item :deep(.el-select) {
  width: 100% !important;
  display: block !important;
}

/* 让下拉框高度与按钮一致 */
.side-control-item :deep(.el-select .el-input__wrapper) {
  min-height: 34px;
  border-radius: 10px;
  width: 100% !important;
}

.side-control-item :deep(.el-select .el-input__inner) {
  height: 34px;
}

.side-controls-inner .el-button:hover {
  transform: translateY(-1px);
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
    min-height: auto;
    background: white;
    overflow-y: auto;
  }

  .el-dialog__footer {
    background: #f8fafc;
    padding: 16px 24px;
    border-top: 1px solid #e2e8f0;
  }
}

.input-dialog-modal {
  /* modal=false 时仍可能存在全屏容器，确保不挡页面点击/不铺底色 */
  pointer-events: none !important;
  background: transparent !important;
}

.input-dialog-modal .el-dialog {
  /* 只让弹窗本体可交互 */
  pointer-events: auto !important;
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
    border-radius: 20px;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.25);
    overflow: hidden;
    backdrop-filter: blur(10px);
    border: 1px solid rgba(255, 255, 255, 0.1);
  }

  .el-dialog__header {
    background: linear-gradient(135deg, #1e293b 0%, #334155 50%, #475569 100%);
    color: white;
    padding: 20px 24px;
    border-bottom: 3px solid #3b82f6;
    margin: 0;
    text-align: center;
    position: relative;
    box-shadow: 0 4px 20px rgba(59, 130, 246, 0.2);
  }

  .el-dialog__header::after {
    content: '';
    position: absolute;
    bottom: -3px;
    left: 50%;
    transform: translateX(-50%);
    width: 60px;
    height: 3px;
    background: linear-gradient(90deg, #3b82f6 0%, #06b6d4 100%);
    border-radius: 2px;
  }

  .el-dialog__title {
    font-size: 18px;
    font-weight: 700;
    color: white;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
    letter-spacing: 0.5px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
  }

  .el-dialog__title::before {
    content: '🎮';
    font-size: 20px;
  }

  .el-dialog__headerbtn {
    top: 18px;
    right: 20px;
    width: 32px;
    height: 32px;
    background: rgba(255, 255, 255, 0.1);
    border-radius: 50%;
    transition: all 0.3s ease;
  }

  .el-dialog__headerbtn:hover {
    background: rgba(255, 255, 255, 0.2);
    transform: scale(1.1);
  }

  .el-dialog__headerbtn .el-dialog__close {
    color: white;
    font-size: 16px;
    font-weight: bold;
    transition: all 0.3s ease;
  }

  .el-dialog__headerbtn:hover .el-dialog__close {
    color: #fbbf24;
  }

  .el-dialog__body {
    padding: 32px 24px 24px 24px;
    min-height: 800px;
    background: linear-gradient(145deg, #f8fafc 0%, #e2e8f0 100%);
    text-align: center;
    position: relative;
    border-top: 1px solid rgba(255, 255, 255, 0.5);
    overflow-y: auto;
  }

  .el-dialog__body::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 1px;
    background: linear-gradient(90deg, transparent 0%, rgba(59, 130, 246, 0.3) 50%, transparent 100%);
  }
}

/* 新的滚动控制容器样式 */
.scroll-control-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.scroll-direction-pad {
  display: grid;
  grid-template-areas:
    ". up ."
    "left center right"
    ". down .";
  grid-template-columns: 1fr 1fr 1fr;
  grid-template-rows: 1fr 1fr 1fr;
  gap: 12px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.scroll-btn-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
}

.scroll-btn-wrapper.up-btn {
  grid-area: up;
}

.scroll-btn-wrapper.left-btn {
  grid-area: left;
}

.scroll-btn-wrapper.right-btn {
  grid-area: right;
}

.scroll-btn-wrapper.down-btn {
  grid-area: down;
}

.scroll-horizontal-wrapper {
  grid-area: left / left / right / right;
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.scroll-center-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
  opacity: 0.6;
}

.scroll-direction-btn {
  width: 48px !important;
  height: 48px !important;
  border: none !important;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
  color: white !important;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.3) !important;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1) !important;
  position: relative;
  overflow: hidden;
}

.scroll-direction-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.2) 0%, rgba(255, 255, 255, 0) 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.scroll-direction-btn:hover {
  transform: translateY(-2px) scale(1.05) !important;
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.4) !important;
}

.scroll-direction-btn:hover::before {
  opacity: 1;
}

.scroll-direction-btn:active {
  transform: translateY(0) scale(0.98) !important;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3) !important;
}

.scroll-direction-btn.up {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%) !important;
}

.scroll-direction-btn.down {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%) !important;
}

.scroll-direction-btn.left {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%) !important;
}

.scroll-direction-btn.right {
  background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%) !important;
}

.scroll-tips {
  margin-top: 8px;
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.7);
  border-radius: 12px;
  backdrop-filter: blur(5px);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.scroll-tips span {
  font-size: 12px;
  color: #64748b;
  font-weight: 500;
}

/* 原操作区域样式已移除 - 操作按钮已移至顶部 */

/* 底部操作按钮美化 */
.operate-bottom {
  margin-top: 12px;
  background: transparent;
  border-top: 1px dashed #e5e7eb;
  border-radius: 0;
  padding: 12px 0;
  /* 与容器留白一致 */
  box-shadow: none;
  width: 100%;
  align-self: center;
}

.button-container {
  display: flex;
  justify-content: center;
  gap: 20px;
}

.button-container .el-button {
  width: 45px;
  height: 45px;
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

/* 输入框组件美化 */
.custom-input {
  width: 100%;
}

.custom-input .el-input__inner {
  border-radius: 8px;
  border: 2px solid #e2e8f0;
  padding: 12px 16px;
  font-size: 14px;
  transition: all 0.3s ease;
}

.custom-input .el-input__inner:focus {
  border-color: #4facfe;
  box-shadow: 0 0 0 3px rgba(79, 172, 254, 0.1);
}


/* 主内容布局 */
.main-content-wrapper {
  display: flex;
  gap: 20px;
  align-items: flex-start;
  flex-wrap: nowrap;
  width: 100%;
}

/* 左侧面板内部：屏幕 + 右侧按钮栏 */
.device-main-row {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  justify-content: center;
  width: 100%;
}

/* 右侧 Tab 工具区（红框区域） */
.tools-panel {
  flex: 1;
  min-width: 360px;
  height: 100%;
}

.tools-tabs {
  height: 100%;
}

.tools-tabs :deep(.el-tabs__content) {
  padding: 12px;
}

.tools-tab-body {
  height: calc(100vh - 220px);
  overflow: auto;
}

.tools-query-controls {
  flex-wrap: wrap;
}

.tools-log-content {
  max-height: none;
}

.dialog-header {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 3px solid #171718 !important;
}

.dialog-title {
  width: 100%;
  text-align: center;
  font-size: 18px;
  font-weight: 600;
  color: #040404;
}

.dialog-title-main {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.device-id {
  font-weight: 700;
}

.device-loc {
  font-weight: 500;
  color: #606266;
  white-space: nowrap;
}

.device-meta {
  margin-top: 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  justify-content: center;
}

.unlock-popover {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.unlock-popover-title {
  font-weight: 600;
  color: #303133;
}

.unlock-popover-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

/* 左侧设备控制面板 */
.device-control-panel {
  width: 550px;
  flex: 0 0 auto;
  /* 宽度由内层内容决定 */
  display: flex;
  flex-direction: column;
  height: 750px;
  /* 固定高度，与终端保持一致 */
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.06);
  padding: 12px;
}

/* 屏幕容器美化 */
.screen-container {
  /* overflow-y: auto; */
  overflow-y: hidden;
  overflow-x: hidden;
  position: relative;
  background: transparent;
  padding: 0px !important;
  display: flex;
  flex-direction: column;
  align-items: center;
  /* 让子元素水平居中 */
  justify-content: flex-start;
  flex: 0 0 auto;
  /* 由内容宽度决定 */
  margin: 0 !important;
  min-height: auto;
}

.screen-container>.screen-sizer {
  margin: 0 auto !important;
  /* 居中 */
  padding: 0 !important;
  position: relative !important;
  max-width: 100%;
  box-sizing: border-box;
}

.screen {
  border: 2px solid #e2e8f0;
  position: absolute;
  border-radius: 8px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  background: #ffffff;
  margin: 0 !important;
  padding: 0 !important;
  top: 0;
}

.screen-sizer {
  /* 仅用于占位限定容器宽度 */
  position: relative;
}

/* 屏幕边界框 - 代表手机屏幕的可滚动区域 */
.screen-boundary {
  position: absolute;
  top: 0;
  left: 0;
  /* border: 3px solid #faad14; */
  /* background-color: rgba(255, 255, 255, 0.4); */
  pointer-events: none;
  /* 不阻止鼠标事件 */
  z-index: 1;
  /* 确保在其他元素之下 */
  box-sizing: border-box;
}

/* 滚动模式下的层级调整 */
.screen.scroll-mode>span:not(.scrollable):not(.roll-modal) {
  z-index: 10 !important;
  /* 滚动模式下图标层级降低 */
  pointer-events: none !important;
  /* 滚动模式下图标不可点击 */
}

.screen.scroll-mode .scrollable {
  z-index: 500 !important;
  /* 滚动模式下scrollable层级提升 */
  pointer-events: none !important;
  /* 区域本身不拦截事件，让底层图标可点击 */
}

.screen-boundary.block-mode {
  border: 10px solid #00ff00;
}

.screen.scroll-mode .scroll-button {
  z-index: 501 !important;
  /* 滚动按钮始终最高层级 */
  pointer-events: auto !important;
  /* 滚动按钮始终可点击 */
}

/* 终端日志样式 */
.terminal-container {
  min-width: 520px;
  /* 稍微加宽默认最小宽度 */
  height: 750px;
  /* 与左侧面板等高 */
  background: #000000;
  border-radius: 8px;
  border: 2px solid #00ff00;
  box-shadow: 0 0 20px rgba(0, 255, 0, 0.3);
  display: flex;
  flex-direction: column;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  overflow: hidden;
  align-self: flex-start;
  /* 确保与左侧对齐 */
}

.terminal-header {
  background: #000000;
  padding: 8px 12px;
  border-bottom: 1px solid #00ff00;
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 40px;
}

/* 信号强度显示 */
.terminal-signal {
  display: flex;
  align-items: center;
  gap: 8px;
}

.terminal-signal .bars {
  display: flex;
  align-items: flex-end;
  gap: 3px;
}

.terminal-signal .bar {
  width: 6px;
  height: 6px;
  background: #333;
  opacity: 0.4;
  transition: all 0.2s ease;
}

.terminal-signal .bar.active.full {
  background: #22c55e;
  /* 绿 */
  opacity: 1;
}

.terminal-signal .bar.active.poor {
  background: #eab308;
  /* 黄 */
  opacity: 1;
}

.terminal-signal .bar.active.verypoor {
  background: #f97316;
  /* 橙 */
  opacity: 1;
}

.terminal-signal .bar.active.none {
  background: #ef4444;
  /* 红 */
  opacity: 1;
}

/* 不同高度模拟信号格子 */
.terminal-signal .bar:nth-child(1) {
  height: 6px;
}

.terminal-signal .bar:nth-child(2) {
  height: 10px;
}

.terminal-signal .bar:nth-child(3) {
  height: 14px;
}

.terminal-signal .bar:nth-child(4) {
  height: 18px;
}

.terminal-signal .signal-label {
  color: #9ca3af;
  font-size: 12px;
}

.terminal-title {
  color: #00ff00;
  font-size: 12px;
  font-weight: 500;
  flex: 1;
  text-align: center;
}

.terminal-actions {
  display: flex;
  align-items: center;
}

.clear-btn {
  color: #888 !important;
  padding: 4px !important;
}

.clear-btn:hover {
  color: #fff !important;
  background: rgba(255, 255, 255, 0.1) !important;
}

.terminal-body {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
  background: #000000;
  color: #00ff00;
  font-size: 12px;
  line-height: 1.4;
}

.terminal-content {
  min-height: 100%;
  display: flex;
  flex-direction: column;
}

.log-entry {
  display: flex;
  margin-bottom: 2px;
  word-wrap: break-word;
  align-items: baseline;
}

.log-timestamp {
  color: #7c3aed;
  margin-right: 8px;
  font-size: 11px;
  flex-shrink: 0;
  min-width: 80px;
}

.log-level {
  margin-right: 8px;
  font-weight: bold;
  font-size: 10px;
  padding: 1px 4px;
  border-radius: 2px;
  flex-shrink: 0;
}

.level-info {
  color: #58a6ff;
  background: rgba(88, 166, 255, 0.1);
}

.level-warn {
  color: #f1e05a;
  background: rgba(241, 224, 90, 0.1);
}

.level-warning {
  color: #f1e05a;
  background: rgba(241, 224, 90, 0.1);
}

.level-error {
  color: #f85149;
  background: rgba(248, 81, 73, 0.1);
}

.level-success {
  color: #3fb950;
  background: rgba(63, 185, 80, 0.1);
}

.log-message {
  flex: 1;
  word-break: break-all;
}

.log-entry.notification .log-message {
  color: #ffa657;
}

.log-entry.connection .log-message {
  color: #3fb950;
}

.log-entry.screen .log-message {
  color: #58a6ff;
}

.log-entry.app .log-message {
  color: #bc8cff;
}

.log-entry.system .log-message {
  color: #7c3aed;
}

.terminal-cursor {
  margin-top: auto;
  padding-top: 8px;
  display: flex;
  align-items: center;
  color: #3fb950;
}

.prompt {
  margin-right: 4px;
  color: #3fb950;
  font-weight: bold;
}

.cursor-blink {
  color: #fff;
  animation: blink 1s infinite;
}

@keyframes blink {

  0%,
  50% {
    opacity: 1;
  }

  51%,
  100% {
    opacity: 0;
  }
}

/* 终端滚动条样式 */
.terminal-body::-webkit-scrollbar {
  width: 6px;
}

.terminal-body::-webkit-scrollbar-track {
  background: #1a1a1a;
}

.terminal-body::-webkit-scrollbar-thumb {
  background: #444;
  border-radius: 3px;
}

.terminal-body::-webkit-scrollbar-thumb:hover {
  background: #666;
}

/* 响应式设计 */
@media (max-width: 1600px) {
  :deep(.device-detail-dialog) {
    .el-dialog {
      width: 1200px !important;
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

  .main-content-wrapper {
    flex-direction: column;
    gap: 15px;
  }

  .device-control-panel {
    width: 100%;
    height: auto;
  }

  .terminal-container {
    height: 400px;
    min-width: 100%;
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

  .operate-bottom {
    width: 100% !important;
    max-width: none !important;
  }
}

/* 强制对话框主体高度设置生效（覆盖 Element 默认限制） */
.device-detail-dialog {
  max-height: none !important;
}

.device-detail-dialog .el-dialog__body {
  min-height: 800px !important;
  overflow-y: auto;
}

/* 页面模式（在新Tab里显示时）让对话框像页面一样铺满且无边距 */
.device-detail-dialog.is-page :deep(.el-dialog) {
  margin: 0 !important;
  border-radius: 0 !important;
  width: 100% !important;
  max-width: none !important;
}

.scroll-dialog-modal {
  /* modal=false 时依然会有全屏容器，确保它不挡住页面点击，也不显示任何底色 */
  pointer-events: none !important;
  background: transparent !important;
}

.scroll-dialog-modal .el-dialog {
  /* 只让弹窗本体可交互 */
  pointer-events: auto !important;
}

/* 控件选择对话框样式 */
:deep(.widget-select-dialog) {
  .el-dialog {
    border-radius: 16px;
    box-shadow: 0 25px 50px rgba(0, 0, 0, 0.15);
    overflow: hidden;
  }

  .el-dialog__header {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
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
    max-height: 500px;
    overflow-y: auto;
  }

  .el-dialog__footer {
    background: #f8fafc;
    padding: 16px 24px;
    border-top: 1px solid #e2e8f0;
  }

  .dialog-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}

.widget-count-info {
  font-size: 13px;
  color: #6b7280;
  font-weight: 500;
}

.count-number {
  display: inline-block;
  padding: 2px 10px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 12px;
  font-weight: 600;
  font-size: 14px;
  margin: 0 4px;
  min-width: 24px;
  text-align: center;
  box-shadow: 0 2px 6px rgba(102, 126, 234, 0.3);
}

.widget-select-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 60vh;
  overflow-y: auto;
  overflow-x: hidden;
  padding-right: 8px;
}

/* 滚动条美化 */
.widget-select-content::-webkit-scrollbar {
  width: 8px;
}

.widget-select-content::-webkit-scrollbar-track {
  background: #f1f5f9;
  border-radius: 4px;
}

.widget-select-content::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 4px;
  transition: background 0.3s ease;
}

.widget-select-content::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(135deg, #5568d3 0%, #653a8b 100%);
}

.widget-select-tip {
  padding: 12px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  border-left: 4px solid #667eea;
  border-radius: 8px;
  font-size: 13px;
  color: #4b5563;
  line-height: 1.5;
  margin-bottom: 8px;
  position: sticky;
  top: 0;
  z-index: 10;
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.widget-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: white;
}

.widget-item:hover {
  border-color: #667eea;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
}

.widget-index {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 50%;
  font-weight: 600;
  font-size: 16px;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
}

.widget-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.widget-text {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  line-height: 1.4;
  word-break: break-word;
}

.widget-details {
  display: flex;
  gap: 8px;
  font-size: 11px;
  color: #6b7280;
  flex-wrap: wrap;
}

.widget-size {
  font-family: 'Monaco', 'Courier New', monospace;
  background: #dbeafe;
  color: #1e40af;
  padding: 2px 6px;
  border-radius: 4px;
  font-weight: 500;
}

.widget-area {
  font-family: 'Monaco', 'Courier New', monospace;
  background: #fef3c7;
  color: #92400e;
  padding: 2px 6px;
  border-radius: 4px;
  font-weight: 500;
}

.widget-pos {
  font-family: 'Monaco', 'Courier New', monospace;
  background: #f3f4f6;
  color: #4b5563;
  padding: 2px 6px;
  border-radius: 4px;
}

.widget-type {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.widget-type .tag {
  display: inline-block;
  padding: 3px 8px;
  font-size: 11px;
  border-radius: 4px;
  font-weight: 500;
  transition: all 0.2s ease;
}

.tag-clickable {
  background: #dbeafe;
  color: #1e40af;
}

.tag-editable {
  background: #d1fae5;
  color: #065f46;
}

.tag-scrollable {
  background: #fef3c7;
  color: #92400e;
}

.tag-checkable {
  background: #e9d5ff;
  color: #6b21a8;
}

.device-detail-dialog .el-dialog__body {
  min-height: auto !important;
}
</style>
