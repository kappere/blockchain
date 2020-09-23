import React from 'react'
import chain_api from '@/api/chain_api'
import { Spin, List, Avatar, Card, Descriptions } from 'antd'

export default function Blockchain() {
    let [blockchain, setBlockchain] = React.useState({chain:[]})
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
                    <List
                        itemLayout="horizontal"
                        dataSource={blockchain.chain}
                        renderItem={item => (
                            <List.Item>
                                <Card hoverable style={{ width: '100%' }}>
                                    <List.Item.Meta
                                        avatar={<Avatar className="light-blue darken-1">{item.index + 1}</Avatar>}
                                        title={item.hash}
                                        description={`交易数量：${item.transactions.length}`}
                                    />
                                </Card>
                            </List.Item>
                        )}
                    />
                </div>
            </Spin>
        </div>
    )
}