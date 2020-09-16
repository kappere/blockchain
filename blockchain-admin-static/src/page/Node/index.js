import React from 'react'
import node_api from '@/api/node_api'
import { Card, List, Spin } from 'antd'
export default function Node() {
    let [nodeList, setNodeList] = React.useState([])
    let [loading, setLoading] = React.useState(true)
    React.useEffect(() => {
        node_api.nodeList().then(data => {
            setNodeList(data.nodes)
            setLoading(false)
        }).catch(err => null)
    }, [setNodeList])
    return (
        <div className="ma-4">
            <Spin spinning={loading}>
                <List
                    grid={{ gutter: 16, column: 4 }}
                    dataSource={nodeList}
                    renderItem={item => (
                        <List.Item>
                            <Card hoverable title={`${item.ip}:${item.port}`}>Expire at {new Date(item.expireTime).toLocaleString()}</Card>
                        </List.Item>
                    )}
                />
            </Spin>
        </div>
    )
}