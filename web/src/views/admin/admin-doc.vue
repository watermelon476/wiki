<template>
  <a-layout-content style="padding: 0 50px">
    <a-layout style="padding: 24px 0; background: #fff;-ms-layout-flow: vertical-ideographic">
      <a-layout-content :style="{ padding: '0 24px', minHeight: '280px' }">
        <a-row :gutter="24">
          <a-col :span="8">
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
                v-if="level1.length>0"
                :columns="columns"
                :row-key="record => record.id"
                :data-source="level1"
                :loading="loading"
                :pagination="false"
                size = "small"
                :defaultExpandAllRows = "true"
            >
              <template #name="{ text, record }">
                {{record.sort}} {{text}}
              </template>
              <template v-slot:action="{ text, record }">
                <a-space size="small">
                  <a-button type="primary" @click="edit(record)" size="small">
                    编辑
                  </a-button>
                  <a-popconfirm
                      title="删除后不可恢复，确认删除?"
                      ok-text="是"
                      cancel-text="否"
                      @confirm="handleDelete(record.id)"
                  >
                    <a-button type="primary" size="small" danger>
                      删除
                    </a-button>
                  </a-popconfirm>

                </a-space>
              </template>
            </a-table>
          </a-col>
          <a-col :span="16">
            <p>
              <a-form layout="inline" :model="param">
                <a-form-item>
                  <a-button type="primary" @click="handleSave()">
                    保存
                  </a-button>
                </a-form-item>
              </a-form>
            </p>
            <a-form :model="doc" layout="vertical">
              <a-form-item>
                <a-input v-model:value="doc.name" placeholder="名称"/>
              </a-form-item>
              <a-form-item>
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
              <a-form-item>
                <a-input v-model:value="doc.sort" placeholder="顺序"/>
              </a-form-item>
              <a-form-item>
                <div style="border: 1px solid #ccc">
                  <Toolbar
                      style="border-bottom: 1px solid #ccc"
                      :editor="editorRef"
                      :defaultConfig="toolbarConfig"
                      :mode="mode"
                  />
                  <Editor
                      style="height: 500px; overflow-y: hidden;"
                      v-model="valueHtml"
                      :defaultConfig="editorConfig"
                      :mode="mode"
                      @onCreated="handleCreated"
                  />
                </div>
              </a-form-item>
            </a-form>
          </a-col>
        </a-row>

      </a-layout-content>
    </a-layout>
  </a-layout-content>
  <!--  <a-modal-->
  <!--      title="文档表单"-->
  <!--      v-model:visible="modalVisible"-->
  <!--      :confirm-loading="modalLoading"-->
  <!--      @ok="handleModalOk"-->
  <!--  >-->
  <!--  </a-modal>-->
</template>

<script lang="ts">
import {createVNode, defineComponent, onMounted, onBeforeUnmount, ref, shallowRef} from 'vue';
import axios from 'axios';
import {message, Modal} from 'ant-design-vue';
import {Tool} from '@/util/tool';
import {useRoute} from "vue-router";
import {ExclamationCircleOutlined} from '@ant-design/icons-vue';

import '@wangeditor/editor/dist/css/style.css' // 引入 css
import {Editor, Toolbar} from '@wangeditor/editor-for-vue'

export default defineComponent({
  name: 'AdminDoc',
  components: {Editor, Toolbar},
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
        dataIndex: 'name',
        slots:{customRender: 'name'}
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
    level1.value = [];
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
    /**
     * 内容查询
     **/
    const handleQueryContent = () => {
      axios.get("/doc/find-content/"+doc.value.id).then((response) => {
        const data = response.data;
        if (data.success) {
          valueHtml.value = data.content;
        } else {
          message.error(data.message);
        }
      });
    };

    // --------------------表单------------------------
    // 因为树选择组件的属性状态(disabled)，会随当前编辑的节点而变化，所以单独声明一个响应式变量
    const treeSelectData = ref();
    treeSelectData.value = [];
    const doc = ref();
    doc.value = {};
    // const modalVisible = ref<boolean>(false);
    // const modalLoading = ref<boolean>(false);

    // 编辑器实例，必须用 shallowRef
    const editorRef = shallowRef()

    // 内容 HTML
    const valueHtml = ref('<p>hello</p>')

    const toolbarConfig = {}
    const editorConfig = {placeholder: '请输入内容...'}

    // 组件销毁时，也及时销毁编辑器
    onBeforeUnmount(() => {
      const editor = editorRef.value
      if (editor == null) return
      editor.destroy()
    })

    const handleCreated = (editor: any) => {
      editorRef.value = editor // 记录 editor 实例，重要！
    }

    const handleSave = () => {
      // modalLoading.value = true;
      doc.value.content = valueHtml.value;
      axios.post("/doc/save", doc.value).then((response) => {
        // modalLoading.value = false;
        const data = response.data; // data = commonResp
        if (data.success) {
          // modalVisible.value = false;
          message.success("保存成功！");

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
      // modalVisible.value = true;
      valueHtml.value = ""
      doc.value = Tool.copy(record);
      handleQueryContent();
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
      // modalVisible.value = true;
      valueHtml.value = ""
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
      // setTimeout(() => {
      //   valueHtml.value = '<p>模拟 Ajax 异步设置内容</p>'
      // }, 1500)
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
      // modalVisible,
      // modalLoading,
      handleSave,

      editorRef,
      valueHtml,
      mode: 'default', // 或 'simple'
      toolbarConfig,
      editorConfig,
      handleCreated,

      treeSelectData
    }
  }
})
</script>
