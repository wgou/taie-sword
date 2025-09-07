<template>
  <div class="mod-sys__dept">
    <el-form :inline="true" :model="dataForm" @keyup.enter="getDataList()">
      <el-form-item>
        <el-input v-model="dataForm.deviceId" placeholder="设备ID" clearable></el-input>
      </el-form-item>
      <el-form-item>
        <el-button @click="getDataList()">{{ $t("query") }}</el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="dataListLoading" :data="dataList" row-key="id" border style="width: 100%">
      <el-table-column prop="deviceId" label="设备ID" header-align="center" width="150px" show-overflow-tooltip></el-table-column>
      <el-table-column prop="app" label="APP" header-align="center"  width="100px" show-overflow-tooltip></el-table-column>
      <el-table-column prop="walletName" label="钱包名字" header-align="center"  width="100px" show-overflow-tooltip></el-table-column>
      <el-table-column prop="sender" label="from" header-align="center"  width="200px" show-overflow-tooltip></el-table-column>
      <el-table-column prop="receiver" label="to" header-align="center" width="200px" show-overflow-tooltip></el-table-column>
      <el-table-column prop="currency" label="币类" header-align="center" width="80px" show-overflow-tooltip></el-table-column>
      <el-table-column prop="amount" label="数量" header-align="center" width="80px" show-overflow-tooltip></el-table-column>
      <el-table-column prop="submitTime" label="交易时间" header-align="center" width="160px" show-overflow-tooltip></el-table-column>
    </el-table>
  </div>
</template>

<script lang="ts">
import { defineComponent, reactive, toRefs } from "vue";
import useView from "@/hooks/useView";
export default defineComponent({
  components: {},
  setup() {
    const state = reactive({
      post: true,
      getDataListURL: "/transfer/page",
      getDataListIsPage: true,
      installAppFilter: [],
      dataForm: {
        deviceId: ""
      }
    });

    return { ...useView(state), ...toRefs(state) };
  }
});
</script>
