import React from 'react'
import transaction_api from '@/api/transaction_api'
import { Card, List, Spin, Button, Modal, Form, Input, PageHeader, Tabs, Typography, Statistic, Row, Col } from 'antd'
export default function Wallet() {
    let [wallets, setWallets] = React.useState([])
    let [loading, setLoading] = React.useState(false)
    let [tabKey, setTabKey] = React.useState('1')
    let [currentWallet, setCurrentWallet] = React.useState(null)
    let [showNewTransactionModal, setShowNewTransactionModal] = React.useState(false)
    let [form] = Form.useForm()
    const getWalletInfo = () => {
        setLoading(true)
        transaction_api.wallets().then(data => {
            setWallets(data)
            setLoading(false)
        }).catch(err => null)
    }
    React.useEffect(() => {
        getWalletInfo()
    }, [])
    return (
        <div>
            <PageHeader
                title="钱包"
                className="white"
                style={{ marginTop: '1px' }}
                footer={
                    <Tabs defaultActiveKey="1" onChange={(activeKey) => setTabKey(activeKey)}>
                        <Tabs.TabPane tab="单地址钱包" key="1">

                        </Tabs.TabPane>
                        <Tabs.TabPane tab="种子钱包" key="2">

                        </Tabs.TabPane>
                    </Tabs>
                }>
            </PageHeader>
            {
                tabKey === '1' ?
                    <div className="ma-4">
                        <Spin spinning={loading}>
                            <List
                                dataSource={wallets}
                                grid={{ gutter: 16, column: 1 }}
                                renderItem={item => (
                                    <List.Item>
                                        <Card hoverable title={
                                            <div>
                                                <span>{item.name}</span>
                                                {/* <Button type="primary" className="float-right" onClick={() => {
                                                    setCurrentWallet(item)
                                                    form.resetFields()
                                                    setShowNewTransactionModal(true)
                                                }} icon={<PlusOutlined />}>Transfer</Button> */}
                                            </div>
                                        }>
                                            <div>
                                                <Row gutter={16}>
                                                    <Col span={8}>
                                                        <Statistic title="余额 (Satochi)" value={item.balance} />
                                                        <Button className="mt-4" type="primary" onClick={() => {
                                                            setCurrentWallet(item)
                                                            form.resetFields()
                                                            setShowNewTransactionModal(true)
                                                        }}>
                                                            转账
                                                        </Button>
                                                    </Col>
                                                    <Col span={16}>
                                                        <Typography.Paragraph copyable={{ text: item.address }}>
                                                            <Statistic title="地址" value={item.address} />
                                                        </Typography.Paragraph>
                                                    </Col>
                                                </Row>
                                            </div>
                                        </Card>
                                    </List.Item>
                                )}
                            />
                        </Spin>
                    </div>
                    : tabKey === '2' ?
                        <></>
                        :
                        <></>
            }

            <Modal
                title={currentWallet && currentWallet.name}
                visible={showNewTransactionModal}
                onOk={async () => {
                    let fields = await form.validateFields()
                    transaction_api.createTransaction(currentWallet.name, fields.address, fields.value).then(data => {
                        console.log(data)
                        setShowNewTransactionModal(false)
                        getWalletInfo()
                    }).catch(err => { })
                }}
                onCancel={() => setShowNewTransactionModal(false)}>
                <Form form={form}>
                    <Form.Item label="地址" name="address">
                        <Input></Input>
                    </Form.Item>
                    <Form.Item label="金额" name="value">
                        <Input></Input>
                    </Form.Item>
                </Form>
            </Modal>
        </div>
    )
}