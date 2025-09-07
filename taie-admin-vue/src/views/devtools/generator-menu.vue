<template>
  <el-dialog v-model="visible" title="创建菜单" :close-on-click-modal="false" :close-on-press-escape="false">
    <el-form :model="dataForm" :rules="dataRule" ref="dataFormRef" label-width="120px">
      <el-form-item prop="parentName" :label="$t('menu.parentName')" class="menu-list">
        <el-popover ref="menuListPopover" :width="400" placement="bottom-start" trigger="click" popper-class="popover-pop">
          <div class="popover-pop-body"><el-tree :data="menuList" :props="{ label: 'name', children: 'children' }" node-key="id" ref="menuListTree" :highlight-current="true" :expand-on-click-node="false" accordion @current-change="menuListTreeCurrentChangeHandle"> </el-tree></div>
          <template v-slot:reference>
            <el-input v-model="dataForm.parentName" :readonly="true" :placeholder="$t('menu.parentName')">
              <template v-slot:suffix>
                <el-icon class="el-input__icon" v-if="dataForm.pid !== '0'" @click.stop="deptListTreeSetDefaultHandle()"><circle-close /></el-icon>
              </template> </el-input
          ></template>
        </el-popover>
      </el-form-item>
      <el-form-item prop="name" :label="$t('menu.name')">
        <el-input v-model="dataForm.name" :placeholder="$t('menu.name')"></el-input>
      </el-form-item>
      <el-form-item prop="icon" :label="$t('menu.icon')" class="icon-list">
        <el-popover ref="iconListPopover" placement="bottom-start" popper-class="popover-pop mod-sys__menu-icon-popover" trigger="click">
          <div class="mod-sys__menu-icon-inner">
            <div class="mod-sys__menu-icon-list">
              <el-button v-for="(item, index) in iconList" :key="index" @click="iconListCurrentChangeHandle(item)" :class="{ 'is-active': dataForm.icon === item }">
                <svg class="icon-svg" aria-hidden="true"><use :xlink:href="`#${item}`"></use></svg>
              </el-button>
            </div>
          </div>
          <template v-slot:reference>
            <el-input v-model="dataForm.icon" :readonly="true" :placeholder="$t('menu.icon')"></el-input>
          </template>
        </el-popover>
      </el-form-item>
    </el-form>
    <template v-slot:footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="dataFormSubmitHandle">确定</el-button>
    </template>
  </el-dialog>
</template>
<script lang="ts">
import { defineComponent, reactive, ref } from "vue";
import { getIconList, useDebounce } from "@/utils/utils";
import baseService from "@/service/baseService";
import { IObject } from "@/types/interface";
export default defineComponent({
  setup() {
    return reactive({
      visible: false,
      menuList: [],
      menuListPopover: ref(),
      iconList: [] as IObject,
      iconListPopover: ref(),
      dataForm: {
        name: "",
        pid: "0",
        parentName: "",
        icon: "",
        moduleName: "",
        className: ""
      }
    });
  },
  computed: {
    dataRule() {
      return {
        name: [{ required: true, message: this.$t("validate.required"), trigger: "blur" }],
        parentName: [{ required: true, message: this.$t("validate.required"), trigger: "change" }]
      };
    }
  },
  created() {
    this.dataFormSubmitHandle = useDebounce(this.dataFormSubmitHandle);
  },
  methods: {
    init() {
      this.visible = true;
      this.$nextTick(() => {
        this.$refs["dataFormRef"].resetFields();
        this.iconList = getIconList();
        this.dataForm.parentName = this.$t("menu.parentNameDefault");
        this.getMenuList();
      });
    },
    // 获取菜单列表
    getMenuList() {
      return baseService.get("/sys/menu/list?type=0").then((res) => {
        if (res.code !== 0) {
          return this.$message.error(res.msg);
        }
        this.menuList = res.data;
      });
    },
    // 上级菜单树, 设置默认值
    deptListTreeSetDefaultHandle() {
      this.dataForm.pid = "0";
      this.dataForm.parentName = this.$t("menu.parentNameDefault");
    },
    // 上级菜单树, 选中
    menuListTreeCurrentChangeHandle(data: IObject) {
      this.dataForm.pid = data.id;
      this.dataForm.parentName = data.name;
      this.menuListPopover.hide();
    },
    // 图标, 选中
    iconListCurrentChangeHandle(icon: string) {
      this.dataForm.icon = icon;
      this.iconListPopover.hide();
    },
    // 表单提交
    dataFormSubmitHandle() {
      this.$refs["dataFormRef"].validate((valid: boolean) => {
        if (!valid) {
          return false;
        }
        baseService.post("/devtools/menu", this.dataForm).then((res) => {
          if (res.code !== 0) {
            return this.$message.error(res.msg);
          }
          this.$message({
            message: this.$t("prompt.success"),
            type: "success",
            duration: 500,
            onClose: () => {
              this.visible = false;
              this.$emit("refreshDataList");
            }
          });
        });
      });
    }
  }
});
</script>

<style lang="less">
.mod-sys__menu {
  .menu-list,
  .icon-list {
    .el-input__inner,
    .el-input__suffix {
      cursor: pointer;
    }
  }
  &-icon-popover {
    width: 458px !important;
    overflow: hidden;
  }
  &-icon-inner {
    width: 100%;
    max-height: 260px;
    overflow-x: hidden;
    overflow-y: auto;
  }
  &-icon-list {
    width: 458px !important;
    padding: 0;
    margin: -8px 0 0 -8px;
    > .el-button {
      padding: 8px;
      margin: 8px 0 0 8px;
      > span {
        display: inline-block;
        vertical-align: middle;
        width: 18px;
        height: 18px;
        font-size: 18px;
      }
    }
  }
}
</style>
