package com.example.utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.*;

public class TextToJSON {
    public static void main(String[] args) {
        // 读取文本文件并转换为JSON格式字符串
        JSONObject jsonGraph = convertTextToJson("E:\\jjm\\work\\佐剂\\gene-miRNA.txt");

        // 输出JSON格式字符串到文件
        writeJsonToFile(jsonGraph, "E:\\jjm\\work\\佐剂\\gene-miRNA.json");
    }

    private static JSONObject convertTextToJson(String filename) {
        JSONObject jsonGraph = new JSONObject();
        JSONArray nodes = new JSONArray();
        JSONArray links = new JSONArray();
        Map<String, Integer> nodeCountMap = new HashMap<>(); // 用于统计每个节点连接数量的映射
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            reader.readLine(); // 跳过标题行
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split("\t");
                String geneAName = fields[0].replaceAll("\"", "");
                String geneBName = fields[1].replaceAll("\"", "");

                // 检查节点是否已存在，若不存在则添加到节点数组中
                JSONObject geneANode = getNode(nodes, geneAName);
                if (geneANode == null) {
                    geneANode = new JSONObject();
                    geneANode.put("name", geneAName);
                    geneANode.put("value", 1); // 初始化连接数量为1
                    nodes.put(geneANode);
                } else {
                    geneANode.put("value", geneANode.getInt("value") + 1); // 更新连接数量
                }

                JSONObject geneBNode = getNode(nodes, geneBName);
                if (geneBNode == null) {
                    geneBNode = new JSONObject();
                    geneBNode.put("name", geneBName);
                    geneBNode.put("value", 1); // 初始化连接数量为1
                    nodes.put(geneBNode);
                } else {
                    geneBNode.put("value", geneBNode.getInt("value") + 1); // 更新连接数量
                }

                JSONObject link = new JSONObject();
                link.put("source", geneAName);
                link.put("target", geneBName);
                links.put(link);
                // 添加第二个链接，将源节点和目标节点交换位置
//                JSONObject link2 = new JSONObject();
//                link2.put("source", geneBName);
//                link2.put("target", geneAName);
//                links.put(link2);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 对节点按照连接数量进行排序
        nodes = sortNodes(nodes);
        double size = 1;
        // 根据节点排序的位置为节点赋予大小值
        for (int i = 0; i < nodes.length(); i++) {
            JSONObject node = nodes.getJSONObject(i);
            // 如果当前节点的连接数量与前一个节点相同，则不增加大小值
            if (i > 0 && node.getInt("value") == nodes.getJSONObject(i - 1).getInt("value")) {
                size = nodes.getJSONObject(i - 1).getDouble("size");
            } else {
                size += 0.2; // 根据位置逐渐增加大小值
            }
            node.put("size", size);
        }

        // 设置每个节点的位置
        setNodePositions(nodes);

        jsonGraph.put("nodes", nodes);
        jsonGraph.put("links", links);
        return jsonGraph;
    }

    private static JSONObject getNode(JSONArray nodes, String nodeName) {
        for (int i = 0; i < nodes.length(); i++) {
            JSONObject node = nodes.getJSONObject(i);
            if (node.getString("name").equals(nodeName)) {
                return node;
            }
        }
        return null;
    }

    private static JSONArray sortNodes(JSONArray nodes) {
        JSONObject[] nodesArray = new JSONObject[nodes.length()];
        for (int i = 0; i < nodes.length(); i++) {
            nodesArray[i] = nodes.getJSONObject(i);
        }
        Arrays.sort(nodesArray, Comparator.comparingInt(a -> a.getInt("value")));
        JSONArray sortedNodes = new JSONArray();
        for (JSONObject node : nodesArray) {
            sortedNodes.put(node);
        }
        return sortedNodes;
    }

    private static void writeJsonToFile(JSONObject jsonObject, String filename) {
        try (Writer writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write(jsonObject.toString(4));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static void setNodePositions(JSONArray nodes) {
        double centerX = 500; // 圆心 x 坐标
        double centerY = 500; // 圆心 y 坐标
        double radius = 500; // 圆半径

        Random random = new Random();

        for (int i = 0; i < nodes.length(); i++) {
            JSONObject node = nodes.getJSONObject(i);

            // 生成随机极坐标角度和半径
            double theta = random.nextDouble() * 2 * Math.PI;
            double r = Math.sqrt(random.nextDouble()) * radius; // 使用平方根可以使节点更密集地分布在圆心附近

            // 计算节点在圆上的坐标
            double x = centerX + r * Math.cos(theta);
            double y = centerY + r * Math.sin(theta);

            // 将节点位置设置为计算得到的坐标
            node.put("x", x);
            node.put("y", y);
        }
    }


}
