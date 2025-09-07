<template>
  <div class="mod-${moduleName}__${classname}">
    <el-form :inline="true" :model="dataForm" @keyup.enter="getDataList()">
    <#list columnList as column>
    <#if column.query>
      <el-form-item>
        <#if column.formType == 'text' || column.formType == 'textarea' || column.formType == 'editor'>
        <el-input v-model="dataForm.${column.attrName}" placeholder="${column.comment!}" clearable></el-input>
        <#elseif column.formType == 'select'>
          <#if column.dictName??>
        <ren-select v-model="dataForm.${column.attrName}" dict-type="${column.dictName}" placeholder="${column.comment!}"></ren-select>
          <#else>
        <el-select v-model="dataForm.${column.attrName}" placeholder="${column.comment!}">
          <el-option label="选择" value="0"></el-option>
        </el-select>
          </#if>
        <#elseif column.formType == 'radio'>
          <#if column.dictName??>
            <ren-radio-group v-model="dataForm.${column.attrName}" dict-type="${column.dictName}"></ren-radio-group>
          <#else>
            <el-radio-group v-model="dataForm.${column.attrName}">
              <el-radio :label="0">单选</el-radio>
            </el-radio-group>
          </#if>
        <#elseif column.formType == 'date'>
        <el-date-picker v-model="daterange" type="daterange" value-format="YYYY-MM-DD" :range-separator="$t('datePicker.range')" :start-placeholder="$t('datePicker.start')" :end-placeholder="$t('datePicker.end')"></el-date-picker>
        <#elseif column.formType == 'datetime'>
        <el-date-picker v-model="datetimerange" type="datetimerange" value-format="YYYY-MM-DD HH:mm:ss" :range-separator="$t('datePicker.range')" :start-placeholder="$t('datePicker.start')" :end-placeholder="$t('datePicker.end')"></el-date-picker>
        <#else>
        <el-input v-model="dataForm.${column.attrName}" placeholder="${column.comment!}" clearable></el-input>
        </#if>
      </el-form-item>
    </#if>
    </#list>
      <el-form-item>
        <el-button @click="getDataList()">{{ $t("query") }}</el-button>
      </el-form-item>
      <el-form-item>
        <el-button type="info" @click="exportHandle()">{{ $t("export") }}</el-button>
      </el-form-item>
      <el-form-item>
        <el-button v-if="hasPermission('${moduleName}:${classname}:save')" type="primary" @click="addOrUpdateHandle()">{{ $t("add") }}</el-button>
      </el-form-item>
      <el-form-item>
        <el-button v-if="hasPermission('${moduleName}:${classname}:delete')" type="danger" @click="deleteHandle()">{{ $t("deleteBatch") }}</el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="dataListLoading" :data="dataList" border @selection-change="dataListSelectionChangeHandle" style="width: 100%">
      <el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
<#list columnList as column>
  <#if column.list>
    <#if column.dictName??>
      <el-table-column prop="${column.attrName}" label="${column.comment!}" header-align="center" align="center">
        <template v-slot="scope">
          {{ getDictLabel("${column.dictName}", scope.row.${column.attrName}) }}
        </template>
      </el-table-column>
    <#else>
      <el-table-column prop="${column.attrName}" label="${column.comment!}" header-align="center" align="center"></el-table-column>
    </#if>
  </#if>
</#list>
      <el-table-column :label="$t('handle')" fixed="right" header-align="center" align="center" width="150">
        <template v-slot="scope">
          <el-button v-if="hasPermission('${moduleName}:${classname}:update')" type="primary" link @click="addOrUpdateHandle(scope.row.id)">{{ $t("update") }}</el-button>
          <el-button v-if="hasPermission('${moduleName}:${classname}:delete')" type="primary" link @click="deleteHandle(scope.row.id)">{{ $t("delete") }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination :current-page="page" :page-sizes="[10, 20, 50, 100]" :page-size="limit" :total="total" layout="total, sizes, prev, pager, next, jumper" @size-change="pageSizeChangeHandle" @current-change="pageCurrentChangeHandle"> </el-pagination>
    <!-- 弹窗, 新增 / 修改 -->
    <add-or-update v-if="addOrUpdateVisible" ref="addOrUpdate" @refreshDataList="getDataList"></add-or-update>
  </div>
</template>

<script lang="ts">
import useView from "@/hooks/useView";
import { defineComponent, reactive, toRefs } from "vue";
import AddOrUpdate from "./${classname}-add-or-update.vue";
export default defineComponent({
  components: {
    AddOrUpdate
  },
  setup() {
    const state = reactive({
      getDataListURL: "/${moduleName}/${classname}/page",
      getDataListIsPage: true,
      exportURL: "/${moduleName}/${classname}/export",
      deleteURL: "/${moduleName}/${classname}",
      deleteIsBatch: true,
      <#list columnList as column>
      <#if column.query>
      <#if column.formType == 'date'>
      daterange: null,
      <#elseif column.formType == 'datetime'>
      datetimerange: null,
      </#if>
      </#if>
      </#list>
      dataForm: {
        <#list columnList as column>
        <#if column.query>
        <#if column.formType == 'date'>
        startDate: null as number | null,
        endDate: null as number | null,
        <#elseif column.formType == 'datetime'>
        startDateTime: null as number | null,
        endDateTime: null as number | null,
        <#else>
        ${column.attrName}: "",
        </#if>
        </#if>
        </#list>
      }
    });
    return { ...useView(state), ...toRefs(state) };
  },
  watch: {
    <#list columnList as column>
    <#if column.query>
    <#if column.formType == 'date'>
    daterange (val: number[] | null) {
      if (val === null) {
        this.dataForm.startDate = null
        this.dataForm.endDate = null
      } else {
        this.dataForm.startDate = val[0]
        this.dataForm.endDate = val[1]
      }
    },
    <#elseif column.formType == 'datetime'>
    datetimerange (val: number[] | null) {
      if (val === null) {
        this.dataForm.startDateTime = null
        this.dataForm.endDateTime = null
      } else {
        this.dataForm.startDateTime = val[0]
        this.dataForm.endDateTime = val[1]
      }
    },
    </#if>
    </#if>
    </#list>
  }
});
</script>