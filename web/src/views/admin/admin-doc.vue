<template>
  <a-layout-content style="padding: 0 50px">
    <a-layout style="padding: 24px 0; background: #fff;-ms-layout-flow: vertical-ideographic">
      <a-layout-content :style="{ padding: '0 24px', minHeight: '280px' }">
        <p>
          <a-form layout="inline" :model="param">
            <a-form-item>
              <a-button type="primary" @click="handleQuery()">
                查询
              </a-button>
            </a-form-item>
            <a-form-item>
              <a-button type="primary" @click="add()">
                新增
              </a-button>
            </a-form-item>
          </a-form>
        </p>
        <a-table
            :columns="columns"
            :row-key="record => record.id"
            :data-source="level1"
            :loading="loading"
            :pagination="false"
        >
          <template #cover="{ text: cover }">
            <img v-if="cover" :src="cover" alt="avatar" style="width:40px;height:40px"/>
          </template>
          <template v-slot:action="{ text, record }">
            <a-space size="small">
              <a-button type="primary" @click="edit(record)">
                编辑
              </a-button>
              <a-popconfirm
                  title="删除后不可恢复，确认删除?"
                  ok-text="是"
                  cancel-text="否"
                  @confirm="handleDelete(record.id)"
              >
                <a-button type="primary" danger>
                  删除
                </a-button>
              </a-popconfirm>

            </a-space>
          </template>
        </a-table>
      </a-layout-content>
    </a-layout>
  </a-layout-content>
  <a-modal
      title="文档表单"
      v-model:visible="modalVisible"
      :confirm-loading="modalLoading"
      @ok="handleModalOk"
  >
    <a-form :model="doc" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
      <a-form-item label="名称">
        <a-input v-model:value="doc.name"/>
      </a-form-item>
      <a-form-item label="父文档">
        <a-tree-select
            v-model:value="doc.parent"
            style="width: 100%"
            :dropdown-style="{ maxHeight: '400px', overflow: 'auto' }"
            :tree-data="treeSelectData"
            placeholder="请选择父文档"
            tree-default-expand-all
            :replaceFields="{children:'children', label:'name', key:'id', value: 'id' }"
        >
        </a-tree-select>
      </a-form-item>
      <a-form-item label="顺序">
        <a-input v-model:value="doc.sort"/>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script lang="ts">
import {createVNode, defineComponent, onMounted, reactive, ref, UnwrapRef} from 'vue';
import axios from 'axios';
import {message, Modal} from 'ant-design-vue';
import {Tool} from '@/util/tool';
import {useRoute} from "vue-router";
import {ExclamationCircleOutlined} from '@ant-design/icons-vue';

export default defineComponent({
  name: 'AdminDoc',
  setup() {
    const route = useRoute()
    console.log("路由：", route);
    console.log("route.path：", route.path);
    console.log("route.query：", route.query);
    console.log("route.param：", route.params);
    console.log("route.fullPath：", route.fullPath);
    console.log("route.name：", route.name);
    console.log("route.meta：", route.meta);

    const param = ref();
    param.value = {};
    const docs = ref();
    const loading = ref(false);

    const columns = [
      {
        title: '名称',
        dataIndex: 'name'
      },
      {
        title: '父文档',
        dataIndex: 'parent'
      },
      {
        title: '顺序',
        dataIndex: 'sort'
      },
      {
        title: 'Action',
        key: 'action',
        slots: {customRender: 'action'}
      }
    ];
    /**
     * 一级文档树，children属性就是二级文档
     * [{
     *    id:"",
     *    name:"",
     *    children:[{
     *      id:"",
     *      name:"",
     *    }]
     * }]
     */
    const level1 = ref(); //一级文档树，children属性就是二级文档

    /**
     * 数据查询
     **/
    const handleQuery = () => {
      loading.value = true;
      axios.get("/doc/all").then((response) => {
        loading.value = false;
        const data = response.data;
        if (data.success) {
          docs.value = data.content;
          console.log("原始数组：", docs.value)

          level1.value = [];
          level1.value = Tool.array2Tree(docs.value, 0);
          console.log("树形结构", level1.value);

        } else {
          message.error(data.message);
        }
      });
    };

    // --------------------表单------------------------
    // 因为树选择组件的属性状态(disabled)，会随当前编辑的节点而变化，所以单独声明一个响应式变量
    const treeSelectData = ref();
    treeSelectData.value = [];
    const doc = ref({});
    const modalVisible = ref<boolean>(false);
    const modalLoading = ref<boolean>(false);

    const handleModalOk = () => {
      modalLoading.value = true;
      axios.post("/doc/save", doc.value).then((response) => {
        modalLoading.value = false;
        const data = response.data; // data = commonResp
        if (data.success) {
          modalVisible.value = false;

          // 重新加载列表
          handleQuery();
        }

      });

    };

    /**
     * 将某节点及其子孙节点全部置为disabled
     */
    const setDisable = (treeSelectData: any, id: any) => {
      // console.log(treeSelectData, id);
      // 遍历数组，即遍历某一层节点
      for (let i = 0; i < treeSelectData.length; i++) {
        const node = treeSelectData[i];
        if (node.id === id) {
          // 如果当前节点就是目标节点
          console.log("disable", node);
          // 将目标节点设置为disabled
          node.disabled = true;

          // 遍历所有子节点，将所有子节点全部都加上disabled
          const children = node.children;
          if (Tool.isNotEmpty(children)) {
            for (let j = 0; j < children.length; j++) {
              setDisable(children, children[j].id)
            }
          }
        } else {
          // 如果当前节点不是目标节点，则到其子节点再找找看。
          const children = node.children;
          if (Tool.isNotEmpty(children)) {
            setDisable(children, id);
          }
        }
      }
    }

    const deleteIds: Array<string> = [];
    const deleteNames: Array<string> = [];
    /**
     * 查找整根树枝，获取某节点及其子孙节点的ID，用于删除
     */
    const getDeleteIds = (treeSelectData: any, id: any) => {
      // console.log(treeSelectData, id);
      // 遍历数组，即遍历某一层节点
      for (let i = 0; i < treeSelectData.length; i++) {
        const node = treeSelectData[i];
        if (node.id === id) {
          // 如果当前节点就是目标节点
          console.log("delete", node);
          // 将目标节点的id加入ids
          deleteIds.push(id);
          deleteNames.push(node.name);

          // 遍历所有子节点
          const children = node.children;
          if (Tool.isNotEmpty(children)) {
            for (let j = 0; j < children.length; j++) {
              getDeleteIds(children, children[j].id)
            }
          }
        } else {
          // 如果当前节点不是目标节点，则到其子节点再找找看。
          const children = node.children;
          if (Tool.isNotEmpty(children)) {
            getDeleteIds(children, id);
          }
        }
      }
    }

    /**
     * 编辑
     */
    const edit = (record: any) => {
      modalVisible.value = true;
      doc.value = Tool.copy(record);

      // 不能选择当前节点及其所有子孙节点，作为父节点，会使树断开
      treeSelectData.value = Tool.copy(level1.value);
      setDisable(treeSelectData.value, record.id);

      // 为选择树添加一个”无“
      treeSelectData.value.unshift({id: 0, name: '无'});
    };

    /**
     * 新增
     */
    const add = () => {
      modalVisible.value = true;
      doc.value = {
        ebookId: route.query.ebookId
      };

      treeSelectData.value = Tool.copy(level1.value);

      // 为选择树添加一个”无“
      treeSelectData.value.unshift({id: 0, name: '无'});
    };

    const handleDelete = (id: number) => {
      // 清空数组，否则多次删除时，数组会一直增加
      deleteIds.length = 0;
      deleteNames.length = 0;
      getDeleteIds(level1.value, id);
      Modal.confirm({
        title: '重要提醒',
        icon: createVNode(ExclamationCircleOutlined),
        content: '将删除：【' + deleteNames.join("，") + "】删除后不可恢复，确认删除？",
        onOk() {
          axios.delete("/doc/delete/" + deleteIds.join(",")).then((response) => {
            const data = response.data; // data = commonResp
            if (data.success) {
              // 重新加载列表
              handleQuery();
            } else {
              message.error(data.message);
            }
          });
        },
      });
    };

    onMounted(() => {
      handleQuery();
    });

    return {
      param,
      // docs,
      level1,
      columns,
      loading,

      edit,
      add,
      handleDelete,
      handleQuery,

      doc,
      modalVisible,
      modalLoading,
      handleModalOk,

      treeSelectData
    }
  }
})
</script>
