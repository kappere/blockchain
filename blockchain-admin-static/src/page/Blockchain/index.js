import React from 'react'
import chain_api from '@/api/chain_api'
import {
    Link
} from 'react-router-dom'
import { Spin, List, Card, Descriptions, Collapse, Space, Divider, Tag } from 'antd'

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
                            <Descriptions.Item label="长度">{blockchain.chain.length}</Descriptions.Item>
                            <Descriptions.Item label="未确认交易数">{blockchain.unconfirmedTransactions && blockchain.unconfirmedTransactions.length}</Descriptions.Item>
                        </Descriptions>
                    </Card>
                </div>
                <div className="ma-4">
                    <Collapse>
                        {blockchain.chain.map(item => (
                            <Collapse.Panel header={
                                <Space>
                                    <Tag color="#039BE5">区块#{item.index + 1}</Tag>
                                    <span><b>{item.hash}</b></span>
                                </Space>
                            } key={item.index}>
                                <List
                                    className="px-6"
                                    grid={{ gutter: 16, column: 1 }}
                                    dataSource={item.transactions}
                                    renderItem={(transaction, index1) => (
                                        <List.Item>
                                            {index1 > 0 ? <Divider></Divider> : ''}
                                            <Space direction="vertical">
                                                <Space>
                                                    <Tag color="#4CAF50">交易#{index1 + 1}</Tag>
                                                    <span><b>{transaction.hash}</b></span>
                                                </Space>
                                                <Descriptions bordered>
                                                    <Descriptions.Item label="输入" span={3}>
                                                        {transaction.inputs.map((input, index) => (
                                                            <div key={index}>
                                                                {index > 0 ? <Divider></Divider> : ''}
                                                                <div>引用交易：<Link to={'#' + input.prevout.hash}>{input.prevout.hash}</Link></div>
                                                                <div>索引：{input.prevout.vout}</div>
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
                                            </Space>
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