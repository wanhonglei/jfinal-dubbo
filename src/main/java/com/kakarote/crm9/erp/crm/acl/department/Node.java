package com.kakarote.crm9.erp.crm.acl.department;

import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Tree Node
 *
 * @author hao.fu
 * @since 2019/11/20 15:09
 */
public class Node<T, E> {

    private Map<T, E> data = null;

    /**
     * direct children of the node
     */
    private List<Node<T, E>> children = Lists.newArrayList();

    /**
     * all leaves of the node
     */
    private List<Node<T, E>> allLeaves = Lists.newArrayList();

    private Node<T, E> parent = null;

    private int nodeDepth;

    private int nodeId;

    /**
     * Constructor.
     * When create a new node, you should setNodeDepth manually.
     *
     * @param data data of the node
     */
    public Node(Map<T, E> data) {
        this.data = data;
    }

    /**
     * Add one child to the node.
     *
     * @param child child node
     * @return child node
     */
    public Node<T, E> addChild(Node<T, E> child) {
        child.setParent(this);
        this.children.add(child);
        return child;
    }

    /**
     * Add a list of children to the node.
     *
     * @param children children of the node
     */
    public void addChildren(List<Node<T, E>> children) {
        children.forEach(each -> each.setParent(this));
        this.children.addAll(children);
    }

    /**
     * Return direct children.
     *
     * @return direct children list
     */
    public List<Node<T, E>> getChildren() {
        return children;
    }

    public List<Node<T, E>> getAllLeaves() {
        if (CollectionUtils.isEmpty(allLeaves)) {
            allLeaves = calculateAllLeaves();
        }
        return allLeaves;
    }

    /**
     * Return all leaves under this node.
     *
     * @return all leaves under this node
     */
    private List<Node<T, E>> calculateAllLeaves() {
        List<Node<T, E>> leaves = Lists.newArrayList();
        for (Node<T, E> node : children) {
            leaves.add(node);
            if (CollectionUtils.isNotEmpty(node.getChildren()) && node.getChildren().size() > 0) {
                leaves.addAll(node.calculateAllLeaves());
            }
        }
        return leaves;
    }

    public Map<T, E> getData() {
        return data;
    }

    public void setData(Map<T, E> data) {
        this.data = data;
    }

    private void setParent(Node<T, E> parent) {
        this.parent = parent;
    }

    public Node<T, E> getParent() {
        return parent;
    }

    public void setChildren(List<Node<T, E>> children) {
        this.children = children;
    }

    public int getNodeDepth() {
        return nodeDepth;
    }

    public void setNodeDepth(int nodeDepth) {
        this.nodeDepth = nodeDepth;
    }

    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public T getNodeDataKey() {
        if (getData() != null && getData().size() == 1) {
            return getData().keySet().iterator().next();
        }
        return null;
    }

    public E getNodeDataValue() {
        if (getData() != null && getData().size() == 1) {
            return getData().values().iterator().next();
        }
        return null;
    }

    /**
     * Get all direct children id of the node.
     *
     * @return children id list
     */
    public List<Integer> getDirectChildrenId() {
        return getChildren().stream().map(Node::getNodeId).collect(Collectors.toList());
    }

    /**
     * Get all leaves id of the node.
     *
     * @return all leaves id list
     */
    public List<Integer> getAllLeavesId() {
        return getAllLeaves().stream().map(Node::getNodeId).collect(Collectors.toList());
    }

    /**
     * Check whether a node is a leaf of current node.
     *
     * @param nodeId node id
     * @return true if a node is a leaf of current node
     */
    public boolean isLeafNode(Integer nodeId) {
        if (CollectionUtils.isEmpty(getAllLeaves())) {
            return false;
        } else {
            return getAllLeavesId().contains(nodeId);
        }
    }

    /**
     * Get a leaf node of current node by node id
     *
     * @param nodeId node id
     * @return leaf node
     */
    public Node<T, E> getLeafNodeById(Integer nodeId) {
        if (isLeafNode(nodeId)) {
            Optional<Node<T, E>> opt = getAllLeaves().stream().filter(item -> item.getNodeId() == nodeId).findFirst();
            if (opt.isPresent()) {
                return opt.get();
            }
        }
        return null;
    }
}
