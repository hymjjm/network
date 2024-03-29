<template>
  <div>
    <div style="display: flex; justify-content: center;margin-top: 5px;">
      <div id="network" style="height: 600px;width: 640px;margin-right: 2px " ></div>
      <span style="width: 1px; background-color: rgba(145,136,136,0.65)"></span>
      <div id="networkmiRNA"  style="height: 600px;width: 640px;margin-left: 2px"></div>
    </div>


    <el-dialog
      title="Related Gene Information"
      :visible.sync="centerDialogVisible"
      width="50%"
      center>
      <div v-if="selectedNode">
        <p><strong>Node Name:</strong> {{ selectedNode.name }}</p>
        <p><strong>Number of Related Genes:</strong> {{ relatedGeneNames.length }}</p>
        <p v-if="relatedGeneNames.length > 0"><strong>Related Genes List:</strong></p>

        <ul v-if="relatedGeneNames.length > 0">
          <li v-for="(gene, index) in relatedGeneNames" :key="index">{{ gene }}</li>
        </ul>
        <p v-else>No related genes found.</p>
      </div>
      <div v-else>
        <p>No node selected.</p>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import * as echarts from 'echarts';

var ROOT_PATH = 'http://tmliang.cn/adjuvaredb/experimental-sl-zj.json';
var ROOT_PATH2 = 'http://tmliang.cn/adjuvaredb/gene-miRNA.json';

export default {
  name: "network",
  data() {
    return {
      centerDialogVisible: false,
      selectedNode: null,
      relatedGeneNames: [],
      myChart1: null, // 保存 echarts 实例
      myChart: null // 保存 echarts 实例
    };
  },
  mounted() {
    this.network1();
    this.network2();

  },
  methods: {
    network1(){
      var chartDom = document.getElementById('network');
      this.myChart1 = echarts.init(chartDom); // 保存 echarts 实例

      this.myChart1.showLoading();

      this.$http.get(ROOT_PATH).then(response => {
        this.myChart1.hideLoading();
        var option = {
          series: [{
            type: 'graph',
            layout: 'force',
            color:'rgba(22,89,7,0.71)',
            animation: false,
            label: {
              show: true,
              position: 'right',
              formatter: function (params) {
                return params.data.name; // 始终返回节点的名称
              },
              textStyle: {
                fontSize: 14, // 设置字号
                // fontWeight: 'bold', // 设置粗体
                // color: 'rgba(122,10,10,0.71)' // 设置颜色
              },
              z: 10 // 设置层级
            },
            draggable: true,
            data: response.data.nodes.map(node => {
              // let symbolSize = 12; // 默认节点大小
              // if (node.color === 'green') {
              //   symbolSize = 16; // 如果节点颜色是绿色，则节点大小放大两倍
              // }
              return {
                name: node.name,
                // x: node.x,
                // y: node.y,
                symbolSize: Math.log(node.value + 1) * 5 + 3,// 使用节点的连接数量作为节点的大小
                // symbolSize: symbolSize ,// 设置节点大小
                itemStyle: {
                  borderWidth: 1, // 设置节点边框宽度
                  borderColor: '#ccc', // 设置节点边框颜色
                  color: node.color, // 从数据中获取节点颜色

                }
              };
            }),
            force: {
              repulsion: 20,
              edgeLength: 150,
              layoutAnimation: true,
              draggable: false,
              gravity: 0 // 设置引力
            },
            edges: response.data.links.map(link => {
              return {
                source: link.source,
                target: link.target,
                lineStyle: {
                  // color: 'rgba(61,74,94,0.29)' // 将线的颜色设置为绿色
                }
              };
            }),
            roam: true,
            emphasis: {
              focus: 'adjacency', // 设置高亮关联节点
              lineStyle: {
                opacity: 0.1 // 设置关联节点的透明度
              }
            },
            lineStyle: {
              opacity: 0.5 // 恢复非关联节点的透明度
            },
          }],

          graphic: [
            {
              type: 'text',
              left: 'center',
              top: 'center',
              style: {
                text: 'Genetic interactions based on synthetic lethality',
                font: 'bold 22px "Microsoft YaHei", sans-serif',
                fill: 'rgba(80,222,9,0.2)' // 设置文本颜色为白色，透明度为0.1

              }
            },
            {
              type: 'text',
              left: 'center',
              top: 'bottom', // 设置为底部
              style: {
                text: 'Green nodes are adjuvant-associated genes', // 添加第二行文本内容
                font: 'normal 20px "Microsoft YaHei", sans-serif', // 设置字体样式
                fill: 'rgba(5,44,96,0.69)' // 设置文本颜色
              }
            }
          ],

        };

        this.myChart1.setOption(option);

        // 监听节点点击事件
        this.myChart1.on('click', params => {
          if (params.dataType === 'node') {
            this.handleNodeClick1(params); // 调用组件中的方法处理节点点击事件
          }
        });
        this.myChart1.on('mouseover', params => {
          const data = this.myChart1.getOption().series[0].data;
          const links = this.myChart1.getOption().series[0].edges;

          const relatedNodes = new Set();
          links.forEach(link => {
            if (link.source === params.dataIndex) {
              relatedNodes.add(link.target);
            } else if (link.target === params.dataIndex) {
              relatedNodes.add(link.source);
            }
          });

          data.forEach((node, index) => {
            if (!relatedNodes.has(index)) {
              this.myChart1.dispatchAction({
                type: 'emphasis',
                seriesIndex: 0,
                dataIndex: index
              });
            }
          });
        });

        this.myChart1.on('mouseout', () => {
          this.myChart1.dispatchAction({
            type: 'normal',
            seriesIndex: 0
          });
        });
      }).catch(error => {
        console.error("HTTP 请求失败:", error);
      });
    },
    network2(){
      var chartDom = document.getElementById('networkmiRNA');
      this.myChart = echarts.init(chartDom); // 保存 echarts 实例

      this.myChart.showLoading();

      this.$http.get(ROOT_PATH2).then(response => {
        this.myChart.hideLoading();
        var option = {
          series: [{
            type: 'graph',
            layout: 'force',
            color:'rgba(22,89,7,0.71)',
            animation: false,
            label: {
              show: true,
              position: 'right',
              formatter: function (params) {
                return params.data.name; // 始终返回节点的名称
              },
              textStyle: {
                fontSize: 8, // 设置字号
                // fontWeight: 'bold', // 设置粗体
                // color: 'rgba(122,10,10,0.71)' // 设置颜色
              },
              z: 10 // 设置层级
            },
            draggable: true,
            data: response.data.nodes.map(node => {
              let symbolSize = 16; // 默认节点大小
              let color = 'rgba(22,89,7,0.71)'; // 默认节点颜色
              if (node.name.substring(0,3) === 'miR') {
                symbolSize = 12; // 如果节点类型为 source，则节点大小为 16
                color = 'rgba(250,117,9,0.85)'; // 如果节点类型为 source，则节点颜色为红色
              }
              return {
                name: node.name,
                // x: node.x,
                // y: node.y,
                // symbolSize: Math.log(node.value + 1) * 5 + 3,// 使用节点的连接数量作为节点的大小
                symbolSize: symbolSize ,// 设置节点大小
                itemStyle: {
                  borderWidth: 1, // 设置节点边框宽度
                  borderColor: '#ccc', // 设置节点边框颜色
                  color: color, // 从数据中获取节点颜色

                }
              };
            }),
            force: {
              repulsion: 110,
              edgeLength: 110,
              layoutAnimation: true,
              draggable: false,
              gravity: 0 // 设置引力
            },
            edges: response.data.links.map(link => {
              return {
                source: link.source,
                target: link.target,
                lineStyle: {
                  // color: 'rgba(61,74,94,0.29)' // 将线的颜色设置为绿色
                }
              };
            }),
            roam: true,
            emphasis: {
              focus: 'adjacency', // 设置高亮关联节点
              lineStyle: {
                opacity: 0.1 // 设置关联节点的透明度
              }
            },
            lineStyle: {
              opacity: 0.5 // 恢复非关联节点的透明度
            },
          }],

          graphic: [
            {
              type: 'text',
              left: 'center',
              top: 'center',
              style: {
                text: 'miRNA-mRNA regulatory network',
                font: 'bold 22px "Microsoft YaHei", sans-serif',
                fill: 'rgba(80,222,9,0.2)' // 设置文本颜色为白色，透明度为0.1

              }
            },

          ],

        };

        this.myChart.setOption(option);

        // 监听节点点击事件
        this.myChart.on('click', params => {
          if (params.dataType === 'node') {
            this.handleNodeClick(params); // 调用组件中的方法处理节点点击事件
          }
        });
        this.myChart.on('mouseover', params => {
          const data = this.myChart.getOption().series[0].data;
          const links = this.myChart.getOption().series[0].edges;

          const relatedNodes = new Set();
          links.forEach(link => {
            if (link.source === params.dataIndex) {
              relatedNodes.add(link.target);
            } else if (link.target === params.dataIndex) {
              relatedNodes.add(link.source);
            }
          });

          data.forEach((node, index) => {
            if (!relatedNodes.has(index)) {
              this.myChart.dispatchAction({
                type: 'emphasis',
                seriesIndex: 0,
                dataIndex: index
              });
            }
          });
        });

        this.myChart.on('mouseout', () => {
          this.myChart.dispatchAction({
            type: 'normal',
            seriesIndex: 0
          });
        });
      }).catch(error => {
        console.error("HTTP 请求失败:", error);
      });

    },
    handleNodeClick1(params) {
      const data = this.myChart1.getOption().series[0].data;
      const links = this.myChart1.getOption().series[0].edges;
      const clickedNodeName = data[params.dataIndex].name;
      const relatedNodeNames = new Set();

      // 找到所有与点击节点相关的节点名
      links.forEach(link => {
        if (link.source === clickedNodeName) {
          relatedNodeNames.add(link.target);
        } else if (link.target === clickedNodeName) {
          relatedNodeNames.add(link.source);
        }
      });

      // 更新相关基因数据
      this.selectedNode = data[params.dataIndex];
      this.relatedGeneNames = Array.from(relatedNodeNames);

      // 打开对话框
      this.centerDialogVisible = true;
    },
    handleNodeClick(params) {
      const data = this.myChart.getOption().series[0].data;
      const links = this.myChart.getOption().series[0].edges;
      const clickedNodeName = data[params.dataIndex].name;
      const relatedNodeNames = new Set();

      // 找到所有与点击节点相关的节点名
      links.forEach(link => {
        if (link.source === clickedNodeName) {
          relatedNodeNames.add(link.target);
        } else if (link.target === clickedNodeName) {
          relatedNodeNames.add(link.source);
        }
      });

      // 更新相关基因数据
      this.selectedNode = data[params.dataIndex];
      this.relatedGeneNames = Array.from(relatedNodeNames);

      // 打开对话框
      this.centerDialogVisible = true;
    }
  }
}
</script>

<style scoped>

</style>
