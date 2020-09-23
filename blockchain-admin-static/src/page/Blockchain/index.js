import React from 'react'
import chain_api from '@/api/chain_api'
import {
    Link
} from 'react-router-dom'
import { Spin, List, Avatar, Card, Descriptions, Collapse, Space, Divider, Tag } from 'antd'

export default function Blockchain() {
    let [blockchain, setBlockchain] = React.useState({ chain: [] })
    let [loading, setLoading] = React.useState(true)
    React.useEffect(() => {
        chain_api.chainDetail().then(data => {
            setBlockchain(data)
            setLoading(false)
        }).catch(err => null)
    }, [])
    return (
        <div>
            <Spin spinning={loading}>
                <div>
                    <Card>
                        <Descriptions title="详情" column={1}>
                            <Descriptions.Item label="难度">{blockchain.difficulty}</Descriptions.Item>
                            <Descriptions.Item label="长度">{blockchain.chain && blockchain.chain.length}</Descriptions.Item>
                        </Descriptions>
                    </Card>
                </div>
                <div className="ma-4">
                    <Collapse>
                        {blockchain.chain.map(item => (
                            <Collapse.Panel header={
                                <Space>
                                    <Avatar size={25} className="light-blue darken-1">{item.index + 1}</Avatar>
                                    <span>区块 {item.hash}</span>
                                </Space>
                            } key={item.index}>
                                <List
                                    grid={{ gutter: 16, column: 1 }}
                                    dataSource={item.transactions}
                                    renderItem={(transaction, index1) => (
                                        <List.Item>
                                            {index1 > 0 ? <Divider></Divider> : ''}
                                            <Descriptions title={
                                                <Space>
                                                    <span><Tag color="#4CAF50">交易#{index1 + 1}</Tag> {transaction.transactionId}</span>
                                                </Space>
                                            } bordered>
                                                <Descriptions.Item label="输入" span={3}>
                                                    {transaction.inputs.map((input, index) => (
                                                        <div key={index}>
                                                            {index > 0 ? <Divider></Divider> : ''}
                                                            <div>引用交易：<Link to={'#' + input.transactionId}>{input.transactionId}</Link></div>
                                                            <div>索引：{input.vout}</div>
                                                            <div className="mb-0">解锁脚本：{input.scriptSig}</div>
                                                            <div>序列号：{input.sequence}</div>
                                                        </div>
                                                    ))}
                                                </Descriptions.Item>
                                                <Descriptions.Item label="输出" span={3}>
                                                    {transaction.outputs.map((output, index) => (
                                                        <div key={index}>
                                                            {index > 0 ? <Divider></Divider> : ''}
                                                            <div>金额：{output.value}</div>
                                                            <div className="mb-0">锁定脚本：{output.scriptPubKey}</div>
                                                        </div>
                                                    ))}
                                                </Descriptions.Item>
                                            </Descriptions>
                                        </List.Item>
                                    )}>
                                </List>
                            </Collapse.Panel>
                        ))}
                    </Collapse>
                </div>
            </Spin>
        </div>
    )
}